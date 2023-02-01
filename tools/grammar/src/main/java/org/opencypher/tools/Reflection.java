/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.invoke.LambdaMetafactory.metafactory;
import static java.lang.invoke.MethodHandleInfo.REF_invokeSpecial;
import static java.lang.invoke.MethodHandleInfo.REF_invokeStatic;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Collections.addAll;
import static java.util.stream.Collectors.joining;

import static org.opencypher.tools.TypedArgument.types;
import static org.opencypher.tools.TypedArgument.values;

/**
 * Utilities for working with Java reflection.
 */
public class Reflection
{
    /**
     * Returns a method handle that invoke a default method of an interface.
     *
     * @param method the (default) method to get a method handle for.
     * @return a method handle that invokes the specific default method.
     */
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

    /**
     * Invoke the given method handle with the specified single argument. Handles the exceptions declared by
     * MethodHandle allowing this method to be used in a context where no checked exceptions may be thrown.
     *
     * @param method the method handle to invoke.
     * @param target the argument to pass to the method.
     * @return the result of invoking the method.
     */
    public static Object invoke( MethodHandle method, Object target )
    {
        try
        {
            return method.invokeWithArguments( target );
        }
        catch ( RuntimeException | Error e )
        {
            throw e;
        }
        catch ( Throwable e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Get the class where the method of a ({@linkplain Serializable serializable}) lambda is implemented.
     *
     * @param lambda the ({@linkplain Serializable serializable}) lambda to get the implementing class of.
     * @return the class where the ({@linkplain Serializable serializable}) lambda is implemented.
     */
    public static Class<?> lambdaClass( Serializable lambda )
    {
        return lambdaClass( serializedLambda( lambda ) );
    }

    /**
     * Get the location in the classpath from where the given class is loaded.
     *
     * @param cls the class to find the classpath location of.
     * @return the classpath location of the given class.
     */
    public static String pathOf( Class<?> cls )
    {
        return cls.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    /**
     * Get the name of the method used to implement a ({@linkplain Serializable serializable}) lambda.
     * This is mostly useful for lambdas that are implemented by a method reference.
     *
     * @param lambda the lambda to get the implementing method name of.
     * @return the name of the method that implements the given ({@linkplain Serializable serializable}) lambda.
     */
    public static String lambdaImplMethodName( Serializable lambda )
    {
        return serializedLambda( lambda ).getImplMethodName();
    }

    /**
     * Get a method handle to the underlying method that implements a given ({@linkplain Serializable serializable})
     * lambda.
     *
     * @param lookup a context that has access to looking up the target method.
     * @param lambda the ({@linkplain Serializable serializable}) lambda to find the implementing method of.
     * @return a method handle to the implementing method of the given ({@linkplain Serializable serializable}) lambda.
     */
    public static MethodHandle methodHandle( MethodHandles.Lookup lookup, Serializable lambda )
    {
        try
        {
            SerializedLambda serialized = serializedLambda( lambda );
            Class<?> impl = lambdaClass( serialized );
            switch ( serialized.getImplMethodKind() )
            {
            case REF_invokeStatic:
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

    /**
     * Serialize a ({@linkplain Serializable serializable}) lambda.
     *
     * @param lambda the ({@linkplain Serializable serializable}) lambda to serialize.
     * @return the serialized form of the given lambda.
     */
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

    /**
     * Find that method of a given instance that implements the specified abstract method of a given base class.
     * <p>
     * This is useful for finding the <i>actual</i> type that implements a type parameter of the base class.
     *
     * @param instance   the instance to find the implementation method of.
     * @param base       the base class that declares the abstract method in question.
     * @param methodName the name of the abstract method.
     * @param <T>        the base type of the instance (the type of the base class).
     * @return the method that implements the abstract method of the base class.
     */
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
