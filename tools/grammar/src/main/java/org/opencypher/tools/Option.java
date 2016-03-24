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
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;

import static org.opencypher.tools.Functions.map;
import static org.opencypher.tools.Reflection.defaultInvoker;

@FunctionalInterface
public interface Option<T> extends Serializable
{
    T value( String name );

    static <T> T dynamicOptions( Class<T> optionsInterface, Function<Method, Object> lookup )
    {
        if ( !optionsInterface.isInterface() )
        {
            throw new IllegalArgumentException( "options must be an interface: " + optionsInterface );
        }
        for ( Method method : optionsInterface.getMethods() )
        {
            if ( method.getDeclaringClass() != Object.class )
            {
                if ( method.getParameterCount() != 0 )
                {
                    throw new IllegalArgumentException(
                            "Options interface may not have methods with parameters: " + method );
                }
            }
        }
        return optionsInterface.cast( Proxy.newProxyInstance(
                optionsInterface.getClassLoader(), new Class[]{optionsInterface},
                new OptionHandler( lookup, new HashMap<>() ) ) );
    }

    static <T> T options( Class<T> optionsInterface, Option<?>... options )
    {
        if ( !optionsInterface.isInterface() )
        {
            throw new IllegalArgumentException( "options must be an interface: " + optionsInterface );
        }
        Map<String, Option<?>> optionMap = new HashMap<>( map( asList( options ), Reflection::lambdaParameterName ) );
        Map<String, Method> methods = map( asList( optionsInterface.getMethods() ), ( method ) -> {
            if ( method.getDeclaringClass() == Object.class )
            {
                return null;
            }
            if ( method.getParameterCount() != 0 )
            {
                throw new IllegalArgumentException(
                        "Options interface may not have methods with parameters: " + method );
            }
            if ( !(method.isDefault() || optionMap.containsKey( method.getName() )) )
            {
                throw new IllegalArgumentException( "Missing required option: " + method.getName() );
            }
            return method.getName();
        } );
        optionMap.keySet().forEach( name -> {
            Method method = methods.get( name );
            if ( method == null )
            {
                throw new IllegalArgumentException( "No such option: " + name );
            }
        } );
        return optionsInterface.cast( Proxy.newProxyInstance(
                optionsInterface.getClassLoader(), new Class[]{optionsInterface},
                new OptionHandler( method -> null, optionMap ) ) );
    }

    class OptionHandler implements InvocationHandler
    {
        private final Function<Method, Object> dynamic;
        private final Map<String, Option<?>> options;

        private OptionHandler( Function<Method, Object> dynamic, Map<String, Option<?>> options )
        {
            this.dynamic = dynamic;
            this.options = options;
        }

        @Override
        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            if ( method.getDeclaringClass() == Object.class )
            {
                switch ( method.getName() )
                {
                case "toString":
                    return proxy.getClass().getName();
                case "hashCode":
                    return System.identityHashCode( proxy );
                case "equals":
                    return proxy == args[0];
                }
            }
            String name = method.getName();
            Object value = dynamic.apply( method );
            if ( value != null )
            {
                return value;
            }
            Option<?> option = options.get( name );
            if ( option == null )
            {
                MethodHandle invoker = defaultInvoker( method );
                options.put( name, option = ( key ) -> {
                    try
                    {
                        return invoker.invokeWithArguments( proxy );
                    }
                    catch ( RuntimeException | Error e )
                    {
                        throw e;
                    }
                    catch ( Throwable e )
                    {
                        throw new RuntimeException( e );
                    }
                } );
            }
            return option.value( name );
        }
    }
}
