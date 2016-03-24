/*
 * Copyright (c) 2015-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.tools;

import java.io.Serializable;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.invoke.LambdaMetafactory.metafactory;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Collections.addAll;
import static java.util.stream.Collectors.joining;

import static org.opencypher.tools.TypedArgument.types;
import static org.opencypher.tools.TypedArgument.values;

public class Reflection
{
    public static Method lambdaMethod( Serializable lambda )
    {
        SerializedLambda serialized = serializedLambda( lambda );
        Class<?> implClass = lambdaClass( serialized );
        return Stream.of( implClass.getDeclaredMethods() )
                     .filter( method -> Objects.equals( method.getName(), serialized.getImplMethodName() ) )
                     .reduce( ( l, r ) -> {
                         throw new IllegalArgumentException( "Too many implementation methods." );
                     } )
                     .orElseThrow( () -> new IllegalStateException( "Unable to find implementation method." ) );
    }

    public static String lambdaParameterName( Serializable lambda )
    {
        Parameter[] parameters = lambdaMethod( lambda ).getParameters();
        if ( parameters == null || parameters.length != 1 )
        {
            throw new IllegalArgumentException(
                    "Must have exactly one parameter, not " + (parameters == null ? 0 : parameters.length) );
        }
        Parameter parameter = parameters[0];
        if ( !parameter.isNamePresent() )
        {
            throw new IllegalStateException( "No parameter name present, compile with '-parameters'." );
        }
        return parameter.getName();
    }

    public static <T> T lambda( MethodHandles.Lookup caller, Class<T> type, MethodHandle target,
                                TypedArgument... arguments )
    {
        Method sam = sam( type );
        Class<?>[] samParameters = sam.getParameterTypes();
        List<Class<?>> targetParameters = new ArrayList<>( samParameters.length + arguments.length );
        Class<?>[] argParameters = types( arguments );
        addAll( targetParameters, argParameters );
        addAll( targetParameters, samParameters );
        CallSite site;
        try
        {
            site = metafactory(
                    caller,
                    sam.getName(), // the name of the interface method
                    methodType( type, argParameters ), // the signature of the generated call site (invoked below)
                    methodType( sam.getReturnType(), samParameters ), // the signature of the interface method
                    target, // the method to invoke
                    methodType( sam.getReturnType(), targetParameters ) );// the signature of the method to invoke
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException( String.format(
                    "Cannot create lambda for %s.%s(%s)%s from method (%s)%s [actual signature: %s] by invoking (%s)%s",
                    type.getSimpleName(), sam.getName(),
                    Stream.of( samParameters ).map( Class::getSimpleName ).collect( joining( "," ) ),
                    sam.getReturnType().getSimpleName(),
                    targetParameters.stream().map( Class::getSimpleName ).collect( joining( "," ) ),
                    sam.getReturnType().getSimpleName(),
                    target.type(),
                    Stream.of( argParameters ).map( Class::getSimpleName ).collect( joining( "," ) ),
                    type.getSimpleName() ), e );
        }
        try
        {
            return type.cast( site.dynamicInvoker().invokeWithArguments( values( arguments ) ) );
        }
        catch ( Throwable e )
        {
            throw new IllegalStateException( "Failure when creating lambda.", e );
        }
    }

    public static MethodHandle defaultInvoker( Method method )
    {
        if ( !method.isDefault() )
        {
            throw new IllegalArgumentException( "Not a default method: " + method );
        }
        try
        {
            return LOOKUP.unreflectSpecial( method, method.getDeclaringClass() );
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( "Reflection failed.", e );
        }
    }

    private static Method sam( Class<?> type )
    {
        if ( !type.isInterface() )
        {
            throw new IllegalArgumentException( "Not an interface: " + type.getName() );
        }
        return Stream.of( type.getMethods() )
                     .filter( m -> !(m.isDefault() || Modifier.isStatic( m.getModifiers() )) )
                     .reduce( ( l, r ) -> {
                         throw new IllegalStateException( "Too many methods." );
                     } ).orElseThrow( () -> new IllegalStateException( "No methods." ) );
    }

    public static Class<?> lambdaClass( Serializable lambda )
    {
        return lambdaClass( serializedLambda( lambda ) );
    }

    public static String pathOf( Class<?> cls )
    {
        return cls.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    public static String lambdaImplMethodName( Serializable lambda )
    {
        return serializedLambda( lambda ).getImplMethodName();
    }

    public static MethodHandle methodHandle( MethodHandles.Lookup lookup, Serializable lambda )
    {
        try
        {
            SerializedLambda serialized = serializedLambda( lambda );
            Class<?> impl = lambdaClass( serialized );
            switch ( serialized.getImplMethodKind() )
            {
            case MethodHandleInfo.REF_invokeStatic:
                return lookup.findStatic(
                        impl, serialized.getImplMethodName(), MethodType.fromMethodDescriptorString(
                                serialized.getImplMethodSignature(), impl.getClassLoader() ) );
            case MethodHandleInfo.REF_invokeVirtual:
                return lookup.findVirtual(
                        impl, serialized.getImplMethodName(), MethodType.fromMethodDescriptorString(
                                serialized.getImplMethodSignature(), impl.getClassLoader() ) );
            default:
                throw new UnsupportedOperationException( "only static and virtual methods supported" );
            }
        }
        catch ( RuntimeException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private static SerializedLambda serializedLambda( Serializable lambda )
    {
        try
        {
            Method replaceMethod = lambda.getClass().getDeclaredMethod( "writeReplace" );
            replaceMethod.setAccessible( true );
            return (SerializedLambda) replaceMethod.invoke( lambda );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Reflection failed." );
        }
    }

    private static Class<?> lambdaClass( SerializedLambda serialized )
    {
        try
        {
            return Class.forName( serialized.getImplClass().replaceAll( "/", "." ) );
        }
        catch ( ClassNotFoundException e )
        {
            throw new IllegalStateException( e );
        }
    }

    private static final MethodHandles.Lookup LOOKUP;

    public static <T> Method implementation( T instance, Class<T> base, String methodName )
    {
        Method proto = Stream.of( base.getDeclaredMethods() )
                             .filter( method -> methodName.equals( method.getName() ) &&
                                                isAbstract( method.getModifiers() ) )
                             .reduce( ( l, r ) -> {
                                 throw new IllegalStateException( "Too many methods '" + methodName + "' of " + base );
                             } )
                             .orElseThrow( () -> new IllegalStateException(
                                     "No method '" + methodName + "' of " + base ) );
        for ( Class<?> klass = instance.getClass(); klass != Object.class; klass = klass.getSuperclass() )
        {
            Method actual;
            try
            {
                actual = klass.getDeclaredMethod( proto.getName(), proto.getParameterTypes() );
            }
            catch ( NoSuchMethodException e )
            {
                continue;
            }
            if ( actual.isBridge() )
            {
                return Stream.of( klass.getDeclaredMethods() )
                             .filter( method -> !method.isBridge() &&
                                                methodName.equals( method.getName() ) &&
                                                method.getParameterCount() == proto.getParameterCount() )
                             .reduce( ( l, r ) -> {
                                 throw new IllegalStateException( "Too many methods for bridge: " + actual );
                             } )
                             .orElseThrow( () -> new IllegalStateException( "No method for bridge: " + actual ) );
            }
            return actual;
        }
        throw new IllegalStateException( "Cannot find implementation of " + proto );
    }

    static
    {

        try
        {
            final Field field = MethodHandles.Lookup.class.getDeclaredField( "IMPL_LOOKUP" );
            field.setAccessible( true );
            LOOKUP = (MethodHandles.Lookup) field.get( null );
        }
        catch ( NoSuchFieldException | IllegalAccessException e )
        {
            throw new RuntimeException( "Failed to access default methods." );
        }
    }
}
