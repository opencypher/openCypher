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
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.opencypher.tools.Functions.map;
import static org.opencypher.tools.Reflection.defaultInvoker;

/**
 * Contains utilities for implementing <i>options interfaces</i>.
 * <p>
 * An <i>options interface</i> is an {@code interface} that only declare methods that take no arguments, and return a
 * <i>value object</i>, typically with default value implementations. Example:
 * <pre><code>
 * class UserOfOptions {
 *     public interface MyOptions {
 *         default Date targetDate() { return new Date(); }
 *         default Font prettyFont() { return new Font( fontName(), Font.PLAIN, fontSize() ); }
 *         default String fontName() { return "Verdana"; }
 *         default int fontSize() { return 11; }
 *     }
 *     private final Date targetDate;
 *     private final Font prettyFont;
 *     public UserOfOptions( MyOptions options ) {
 *         this.targetDate = options.targetDate();
 *         this.prettyFont = options.prettyFont();
 *     }
 * }
 * </code></pre>
 *
 * @param <T> The value type of the <i>options interface</i> that this option customizes.
 */
public interface Option<T> extends Serializable
{
    static <T> Option<T> option( String name, Function<T,?> lookup )
    {
        return new Option<T>()
        {
            @Override
            public String name()
            {
                return name;
            }

            @Override
            public Object value( T options )
            {
                return lookup.apply( options );
            }
        };
    }

    String name();

    Object value( T options );

    /**
     * Create a dynamic implementation of the supplied <i>options interface</i> using the specified options to define
     * values. The specified options must be lambdas where the parameter name of the lambda is the name of the option
     * of the options interface that lambda overrides.
     * When the lambda is invoked, the instance of the options interface is given as the sole parameter, this allows
     * custom options that depend on other options of the options interface.
     * For options that are not overridden the lookup function provided is used to lookup the value, and if the lookup
     * function returns {@code null} the default value is used.
     *
     * @param optionsType the <i>options interface</i> to implement.
     * @param lookup      the function to use to look up values not explicitly overridden.
     * @param options     lambdas that define the overridden options.
     * @param <T>         the type of the <i>options interface</i>.
     * @return an instance of the <i>options interface</i>.
     */
    @SafeVarargs
    static <T> T dynamicOptions( Class<T> optionsType, Function<Method, Object> lookup, Option<? super T>... options )
    {
        return OptionHandler.create( optionsType, requireNonNull( lookup, "lookup" ), options );
    }

    /**
     * Create a dynamic implementation of the supplied <i>options interface</i> using the specified options to define
     * values. The specified options must be lambdas where the parameter name of the lambda is the name of the option
     * of the options interface that lambda overrides.
     * When the lambda is invoked, the instance of the options interface is given as the sole parameter, this allows
     * custom options that depend on other options of the options interface.
     * For options that are not given, the default value is used.
     *
     * @param optionsType the <i>options interface</i> to implement.
     * @param options     lambdas that define the overridden options.
     * @param <T>         the type of the <i>options interface</i>.
     * @return an instance of the <i>options interface</i>.
     */
    @SafeVarargs
    static <T> T options( Class<T> optionsType, Option<? super T>... options )
    {
        return OptionHandler.create( optionsType, null, options );
    }

    /**
     * Implementation detail: the {@link InvocationHandler} used for implementing an <i>options interface</i>.
     *
     * @param <T> the implemented <i>options interface</i>.
     */
    class OptionHandler<T> implements InvocationHandler
    {
        @SafeVarargs
        private static <T> T create( Class<T> iFace, Function<Method, Object> lookup, Option<? super T>... options )
        {
            if ( !iFace.isInterface() )
            {
                throw new IllegalArgumentException( "options must be an interface: " + iFace );
            }
            Map<String, Option<? super T>> optionMap = new HashMap<>( map( asList( options ), Option::name ) );
            Map<String, Method> methods = map( asList( iFace.getMethods() ), ( method ) ->
            {
                if ( method.getDeclaringClass() == Object.class )
                {
                    return null;
                }
                if ( method.getParameterCount() != 0 )
                {
                    if ( method.isDefault() )
                    {
                        return null;
                    }
                    throw new IllegalArgumentException(
                            "Options interface may not have methods with parameters: " + method );
                }
                if ( !(method.isDefault() || optionMap.containsKey( method.getName() )) && lookup == null )
                {
                    throw new IllegalArgumentException( "Missing required option: " + method.getName() );
                }
                return method.getName();
            } );
            optionMap.keySet().forEach( name ->
            {
                Method method = methods.get( name );
                if ( method == null )
                {
                    throw new IllegalArgumentException( "No such option: " + name );
                }
            } );
            return iFace.cast( Proxy.newProxyInstance(
                    iFace.getClassLoader(), new Class[]{iFace},
                    new OptionHandler<T>( lookup == null ? (name -> null) : lookup, optionMap ) ) );
        }

        private final Function<Method, Object> dynamic;
        private final Map<String, Option<? super T>> options;

        private OptionHandler( Function<Method, Object> dynamic, Map<String, Option<? super T>> options )
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
            if ( args != null && args.length > 0 )
            {
                Object[] arguments = new Object[args.length + 1];
                arguments[0] = proxy;
                System.arraycopy( args, 0, arguments, 1, args.length );
                return defaultInvoker( method ).invokeWithArguments( arguments );
            }
            String name = method.getName();
            Option<? super T> option = options.get( name );
            if ( option == null )
            {
                Object value = dynamic.apply( method );
                if ( value != null )
                {
                    options.put( name, option = option( name, value ) );
                }
                else
                {
                    options.put( name, option = option( name, defaultInvoker( method ) ) );
                }
            }
            return invoke( option, proxy );
        }

        @SuppressWarnings("unchecked")
        private static Object invoke( Option option, Object options )
        {
            return option.value( options );
        }

        private static <T> Option<T> option( String name, Object value )
        {
            return Option.option( name, options -> value );
        }

        private static <T> Option<T> option( String name, MethodHandle invoker )
        {
            return Option.option( name, options -> Reflection.invoke( invoker, options ) );
        }
    }
}
