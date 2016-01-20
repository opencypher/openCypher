package org.opencypher.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

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
            if ( result.put( k, value ) != null )
            {
                throw new IllegalArgumentException( "Duplicate key: " + k );
            }
        }
        return result;
    }
}
