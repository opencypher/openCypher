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
package org.opencypher.tools.xml;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.filterReturnValue;

import static org.opencypher.tools.Reflection.methodHandle;

/**
 * DSL for getting {@linkplain MethodHandle method handles} from method references.
 */
interface Reference extends Serializable
{
    static <T, R> Function<T, R> function( Function<T, R> reference )
    {
        return reference;
    }

    static <T, U, R> BiFunction<T, U, R> biFunction( BiFunction<T, U, R> reference )
    {
        return reference;
    }

    static <T> ToIntFunction<T> toInt( ToIntFunction<T> reference )
    {
        return reference;
    }

    static <T> ToLongFunction<T> toLong( ToLongFunction<T> reference )
    {
        return reference;
    }

    static <T> ToBoolFunction<T> toBool( ToBoolFunction<T> reference )
    {
        return reference;
    }

    static <T> ToDoubleFunction<T> toDouble( ToDoubleFunction<T> reference )
    {
        return reference;
    }

    interface Function<T, R> extends Reference
    {
        R apply( T t );

        default <V> Function<T, V> then( Function<? super R, ? extends V> after )
        {
            return new Function<T, V>()
            {
                @Override
                public V apply( T value )
                {
                    return after.apply( Function.this.apply( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( Function.this.mh(), after.mh() );
                }
            };
        }
    }

    interface IntFunction<R> extends Reference
    {
        R apply( int value );
    }

    interface ToIntFunction<T> extends Reference
    {
        int applyAsInt( T value );

        default <R> Function<T, R> then( IntFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsInt( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToIntFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface LongFunction<R> extends Reference
    {
        R apply( long value );
    }

    interface ToLongFunction<T> extends Reference
    {
        long applyAsLong( T value );

        default <R> Function<T, R> then( LongFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsLong( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToLongFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface BoolFunction<R> extends Reference
    {
        R apply( boolean value );
    }

    interface ToBoolFunction<T> extends Reference
    {
        boolean applyAsBool( T value );

        default <R> Function<T, R> then( BoolFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsBool( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToBoolFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface DoubleFunction<R> extends Reference
    {
        R apply( double value );
    }

    interface ToDoubleFunction<T> extends Reference
    {
        double applyAsDouble( T value );

        default <R> Function<T, R> then( DoubleFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsDouble( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToDoubleFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface BiFunction<T, U, R> extends Reference
    {
        R apply( T t, U u );
    }

    default MethodHandle mh()
    {
        return methodHandle( MethodHandles.lookup(), this );
    }
}
