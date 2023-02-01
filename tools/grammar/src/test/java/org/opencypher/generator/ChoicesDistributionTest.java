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
package org.opencypher.generator;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

import org.junit.Ignore;
import org.junit.Test;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Repetition;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingLong;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.opencypher.generator.Choices.times;
import static org.opencypher.tools.Assert.assertEquals;

public class ChoicesDistributionTest
{
    @Ignore
    @Test
    public void printHistogram() throws Exception
    {
        printHistograms(
                repetitionHistogram( 0, 0 ),
                repetitionHistogram( 0, 0, 0 ),
                repetitionHistogram( 0, 1 ),
                repetitionHistogram( 0, 1, 1 ),
                repetitionHistogram( 1, 1 ),
                repetitionHistogram( 1, 1, 1 ),
                repetitionHistogram( 0, 7, 7 ) );
    }

    @Test
    public void shouldNotGenerateValuesOutsideOfRange() throws Exception
    {
        repetitionHistogram( 1, 1, 1 ).assertOrderIs( 1 );
    }

    @Test
    public void oneShouldBeTheMostCommonForRepetitionsWithMinZero() throws Exception
    {
        repetitionHistogram( 0, 1 )
                .assertOrderStartsWith( 1, 2, 0, 3, 4 )
                .assertRatio( 1, 2, 0.73, 0.76 )
                .assertRatio( 2, 0, 0.85, 0.92 )
                .assertRatio( 2, 3, 0.45, 0.52 )
                .assertRatio( 3, 4, 0.35, 0.40 );
    }

    @Test
    public void oneShouldBeTheMostCommonForRepetitionsWithMinOne() throws Exception
    {
        repetitionHistogram( 1, 1 )
                .assertOrderStartsWith( 1, 2, 3, 4, 5 )
                .assertRatio( 1, 2, 0.45, 0.52 )
                .assertRatio( 2, 3, 0.22, 0.28 )
                .assertRatio( 3, 4, 0.14, 0.18 )
                .assertRatio( 4, 5, 0.11, 0.14 );
    }

    @Test
    public void twoShouldBeTheMostCommonForRepetitionsWithMinTwo() throws Exception
    {
        repetitionHistogram( 2, 2 )
                .assertOrderStartsWith( 2, 3, 4, 5, 6 )
                .assertRatio( 2, 3, 0.45, 0.52 )
                .assertRatio( 3, 4, 0.22, 0.28 )
                .assertRatio( 4, 5, 0.15, 0.18 )
                .assertRatio( 5, 6, 0.11, 0.14 );
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

    @SafeVarargs
    private final void printHistograms( Histogram<Integer>... histograms )
    {
        int max = 0;
        System.out.print( "key" );
        for ( Histogram<Integer> histogram : histograms )
        {
            for ( Integer key : histogram.keys() )
            {
                max = Math.max( max, key );
            }
            System.out.print( '\t' );
            System.out.print( histogram.name );
        }
        System.out.println();
        for ( int i = 0; i <= max; i++ )
        {
            System.out.print( i );
            for ( Histogram<Integer> histogram : histograms )
            {
                System.out.print( '\t' );
                System.out.print( histogram.get( i ) );
            }
            System.out.println();
        }
    }

    static class Histogram<T>
    {
        final String name;
        final Map<T, Long> histogram;

        Histogram( String name, Map<T, Long> histogram )
        {
            this.name = name;
            this.histogram = histogram;
        }

        public Iterable<? extends T> keys()
        {
            return histogram.keySet();
        }

        public long get( T key )
        {
            return histogram.getOrDefault( key, 0L );
        }

        @SafeVarargs
        public final Histogram<T> assertOrderStartsWith( T... prefix )
        {
            return assertOrder( len -> prefix.length <= len, prefix );
        }

        @SafeVarargs
        public final Histogram<T> assertOrderIs( T... order )
        {
            return assertOrder( len -> order.length == len, order );
        }

        @SafeVarargs
        private final Histogram<T> assertOrder( IntPredicate length, T... order )
        {
            @SuppressWarnings("unchecked")
            Class<T> type = (Class) order.getClass().getComponentType();
            T[] actual = order( type );
            assertTrue( "length was: " + actual.length, length.test( actual.length ) );
            assertSamePrefix( i -> message( order, i ), order, actual );
            return this;
        }

        private T[] order( Class<T> type )
        {
            return sortedArray( type, histogram.keySet(), reverseOrder( comparingLong( histogram::get ) ) );
        }

        private String message( T[] order, int offset )
        {
            StringBuilder result = new StringBuilder();
            for ( int i = 0; i < order.length; i++ )
            {
                result.append( i == 0 ? "{" : ", " );
                if ( i == offset )
                {
                    result.append( "**" );
                }
                result.append( order[i] );
                if ( i == offset )
                {
                    result.append( "**" );
                }
                result.append( ':' ).append( get( order[i] ) );
            }
            return result.append( '}' ).toString();
        }

        public Histogram<T> assertRatio( T key1, T key2, double minRatio, double maxRatio )
        {
            long value1 = get( key1 ), value2 = get( key2 );
            double ratio = ((double) value2) / ((double) value1);
            assertThat(
                    String.format( "Ratio between %s (%d) and %s (%d)", key1, value1, key2, value2 ),
                    ratio, allOf( greaterThanOrEqualTo( minRatio ), lessThanOrEqualTo( maxRatio ) ) );
            return this;
        }
    }

    static <T> void assertSamePrefix( IntFunction<String> message, T[] expected, T[] actual )
    {
        for ( int i = 0; i < expected.length && i < actual.length; i++ )
        {
            assertEquals( message( message, i ), expected[i], actual[i] );
        }
    }

    private static Supplier<String> message( IntFunction<String> message, int i )
    {
        return () -> message.apply( i );
    }

    static Histogram<Integer> repetitionHistogram( int min, int norm )
    {
        return repetitionHistogram( new RepetitionStub( min, norm, null ) );
    }

    static Histogram<Integer> repetitionHistogram( int min, int norm, int max )
    {
        return repetitionHistogram( new RepetitionStub( min, norm, max ) );
    }

    private static Histogram<Integer> repetitionHistogram( Repetition repetition )
    {
        return new Histogram<>(
                repetition.limited() ? String.format(
                        "[%d|%d|%d]", repetition.minTimes(), repetition.norm(), repetition.maxTimes() ) : String.format(
                        "[%d|%d...", repetition.minTimes(), repetition.norm() ),
                histogram( 1_000_000, () -> times( repetition ) ) );
    }

    private static class RepetitionStub implements Repetition
    {
        private final int min, norm;
        private final Integer max;

        private RepetitionStub( int min, int norm, Integer max )
        {
            assert min <= norm;
            assert max == null || norm <= max;
            this.min = min;
            this.norm = norm;
            this.max = max;
        }

        @Override
        public int minTimes()
        {
            return min;
        }

        @Override
        public int norm()
        {
            return norm;
        }

        @Override
        public boolean limited()
        {
            return max != null;
        }

        @Override
        public int maxTimes()
        {
            if ( max != null )
            {
                return max;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public Grammar.Term term()
        {
            throw new UnsupportedOperationException();
        }
    }
}
