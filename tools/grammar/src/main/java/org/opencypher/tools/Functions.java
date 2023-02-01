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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

/**
 * Utilities for working with Java functions.
 */
public class Functions
{
    /**
     * Require an instance not to be null, using the (simple) name of the required type as an error message if it is.
     *
     * @param type  the required type.
     * @param value the value that must not be null.
     * @param <T>   the type.
     * @return the value, that is guaranteed not to be null.
     * @see Objects#requireNonNull(Object, Supplier)
     */
    public static <T> T requireNonNull( Class<T> type, T value )
    {
        return Objects.requireNonNull( value, type::getSimpleName );
    }

    /**
     * Convert the value supplied by a {@link Supplier} by passing it through a {@link Function}.
     *
     * @param source the source supplier that supplies the original value.
     * @param map    the function that converts the value of the original supplier.
     * @param <I>    the type of the value supplied by the original supplier.
     * @param <O>    the type of the value supplied by the resulting supplier.
     * @return a converting supplier.
     */
    public static <I, O> Supplier<O> map( Supplier<I> source, Function<I, O> map )
    {
        return () -> map.apply( source.get() );
    }

    /**
     * Create a {@link Map} by applying a function that extracts the key from an array of values.
     *
     * @param key    the key extraction function.
     * @param values the values of the map (from which the keys are extracted).
     * @param <K>    the type of the keys.
     * @param <V>    the type of the values.
     * @return a {@link Map} from the keys of the values to the values.
     */
    @SafeVarargs
    public static <K, V> Map<K, V> map( Function<V, K> key, V... values )
    {
        return values == null || values.length == 0 ? emptyMap() : map( asList( values ), key );
    }

    /**
     * A method that is useful as a method reference to implement an identity function.
     *
     * @param value the input (and output) value of the function.
     * @param <T>   the type of the value.
     * @return the value that was passed in.
     */
    public static <T> T identity( T value )
    {
        return value;
    }

    /**
     * Create a {@link Map} by applying a function that extracts the key from a collection of values.
     *
     * @param values the values of the map (from which the keys are extracted).
     * @param key    the key extraction function.
     * @param <K>    the type of the keys.
     * @param <V>    the type of the values.
     * @return a {@link Map} from the keys of the values to the values.
     */
    public static <K, V> Map<K, V> map( Collection<V> values, Function<V, K> key )
    {
        if ( Objects.requireNonNull( values, "values" ).size() == 0 )
        {
            return emptyMap();
        }
        Objects.requireNonNull( key, "key function" );
        Map<K, V> result = new HashMap<>( (int) (values.size() / 0.75) );
        for ( V value : values )
        {
            K k = key.apply( value );
            if ( k != null )
            {
                if ( result.put( k, value ) != null )
                {
                    throw new IllegalArgumentException( "Duplicate key: " + k );
                }
            }
        }
        return result;
    }

    /**
     * A {@link Collector} that collects {@linkplain Optional optional} values to a list.
     * The collector only collects values that are {@linkplain Optional#isPresent() present}.
     *
     * @param <T> the type of values to collect.
     * @return a collector that collects optional values to a list.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collector<Optional<T>, List<T>, List<T>> flatList()
    {
        return (Collector) FLAT_LIST;
    }

    @SuppressWarnings("unchecked")
    private static final Collector<Optional, List, List> FLAT_LIST = Collector.of(
            ArrayList::new, ( result, item ) -> item.ifPresent( result::add ), ( lhs, rhs ) -> {
                List result = new ArrayList<>( lhs.size() + rhs.size() );
                result.addAll( lhs );
                result.addAll( rhs );
                return result;
            } );
}
