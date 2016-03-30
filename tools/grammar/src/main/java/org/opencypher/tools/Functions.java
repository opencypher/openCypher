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

public class Functions
{
    public static <T> T requireNonNull( Class<T> type, T value )
    {
        return Objects.requireNonNull( value, type::getSimpleName );
    }

    public static <I, O> Supplier<O> map( Supplier<I> source, Function<I, O> map )
    {
        return () -> map.apply( source.get() );
    }

    @SafeVarargs
    public static <K, V> Map<K, V> map( Function<V, K> key, V... values )
    {
        return values == null || values.length == 0 ? emptyMap() : map( asList( values ), key );
    }

    public static <T> T identity( T value )
    {
        return value;
    }

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
