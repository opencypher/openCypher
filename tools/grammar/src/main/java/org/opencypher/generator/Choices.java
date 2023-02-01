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

import java.util.concurrent.ThreadLocalRandom;

import org.opencypher.grammar.BiasedTerms;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Repetition;

import static java.lang.Math.sqrt;

public interface Choices
{
    Grammar.Term choose( Node location, BiasedTerms alternatives );

    int repetition( Node location, Repetition repetition );

    boolean includeOptional( Node location, Optional optional );

    int codePoint( Node location, CharacterSet characters );

    Choices SIMPLE = new Choices()
    {
        @Override
        public Grammar.Term choose( Node location, BiasedTerms alternatives )
        {
            return alternatives.term( random( alternatives.bound() ) );
        }

        @Override
        public int repetition( Node location, Repetition rep )
        {
            return times( rep );
        }

        @Override
        public boolean includeOptional( Node location, Optional optional )
        {
            return random() < optional.probability();
        }

        @Override
        public int codePoint( Node location, CharacterSet characters )
        {
            return characters.randomCodePoint( ThreadLocalRandom.current());
        }
    };

    static double random()
    {
        return ThreadLocalRandom.current().nextDouble();
    }

    static double random( double bound )
    {
        return ThreadLocalRandom.current().nextDouble( bound );
    }

    static int random( int min, int max )
    {
        if ( max == Integer.MAX_VALUE )
        {
            return ThreadLocalRandom.current().nextInt( min );
        }
        else
        {
            return ThreadLocalRandom.current().nextInt( min, max + 1 );
        }
    }

    static int normal( int min, int norm, double scale )
    {
        int shift = norm - min;
        double v = normal( scale ) + shift;
        if ( v < 0 )
        {
            v += shift;
            if ( v < 0 )
            {
                v = -v;
            }
        }
        return (int) (v + min);
        /*
        if ( min == 0 && norm == 1 )
        {
            // TODO: plot this and make better sense of it
            return (int) Math.abs( Math.floor( normal( scale ) ) );
        }
        else
        {
            return (int) Math.abs( normal( scale ) + norm - min ) + min;
        }
        */
    }

    static int times( Repetition rep )
    {
        int min = rep.minTimes();
        double norm = rep.norm() - min;
        if ( rep.limited() )
        {
            int n = rep.maxTimes() - min;
            if ( norm == 0 )
            {
                norm = 0.5;
            }
            else if ( n == norm )
            {
                norm -= 0.5 - (sqrt( 0.02 ) / norm);
            }
            return min + binomial( n, norm / n );
        }
        else
        {
            return min + poisson( norm + 0.5 );
        }
    }

    static double normal( double scale )
    {
        return ThreadLocalRandom.current().nextGaussian() * scale;
    }

    static int poisson( double lambda )
    {
        double L = Math.exp( -lambda );
        double p = 1.0;
        int k = 0;

        do
        {
            k++;
            p *= random();
        } while ( p > L );

        return k - 1;
    }

    static int binomial( int n, double p )
    {
        int x = 0;
        for ( int i = 0; i < n; i++ )
        {
            if ( random() < p )
            {
                x++;
            }
        }
        return x;
    }
}
