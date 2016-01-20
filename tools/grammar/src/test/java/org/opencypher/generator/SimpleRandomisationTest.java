package org.opencypher.generator;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.junit.Test;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingLong;

import static org.junit.Assert.assertEquals;
import static org.opencypher.generator.Randomisation.simpleRandomisation;

public class SimpleRandomisationTest
{
    @Test
    public void oneShouldBeTheMostCommonForRepetitionsWithMinZero() throws Exception
    {
        assertHistogramOrder( ( random ) -> random.repetition( 0 ), 1, 2, 0, 3, 4 );
    }

    @Test
    public void oneShouldBeTheMostCommonForRepetitionsWithMinOne() throws Exception
    {
        assertHistogramOrder( ( random ) -> random.repetition( 1 ), 1, 2, 3, 4, 5 );
    }

    @Test
    public void twoShouldBeTheMostCommonForRepetitionsWithMinTwo() throws Exception
    {
        assertHistogramOrder( ( random ) -> random.repetition( 2 ), 2, 3, 4, 5, 6 );
    }

    static void assertHistogramOrder( ToIntFunction<Randomisation> randomisation, int... expectedOrder )
    {
        Randomisation random = simpleRandomisation();
        Map<Integer, Long> histogram = histogram( 1_000_000, () -> randomisation.applyAsInt( random ) );
        Integer[] actualOrder = sortedArray(
                Integer.class, histogram.keySet(),
                reverseOrder( comparingLong( histogram::get ) ) );
        for ( int i = 0; i < expectedOrder.length && i < actualOrder.length; i++ )
        {
            assertEquals( "position:" + i, expectedOrder[i], actualOrder[i].intValue() );
        }
    }

    static <T extends Comparable<T>> Map<T, Long> histogram( int samples, Supplier<T> sampler )
    {
        Map<T, Long> histogram = new HashMap<>();
        for ( int i = 0; i < samples; i++ )
        {
            T value = sampler.get();
            histogram.compute( value, ( k, v ) -> v == null ? 1 : v + 1 );
        }
        return histogram;
    }

    private static <T> T[] sortedArray( Class<T> type, Collection<T> values, Comparator<T> cmp )
    {
        @SuppressWarnings({"unchecked"})
        T[] array = values.toArray( (T[]) Array.newInstance( type, values.size() ) );
        Arrays.sort( array, cmp );
        return array;
    }
}
