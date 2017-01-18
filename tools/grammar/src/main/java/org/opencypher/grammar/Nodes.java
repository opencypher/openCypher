/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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
package org.opencypher.grammar;

import java.util.Iterator;
import java.util.stream.Stream;

public class Nodes implements BiasedTerms
{
    private final Node[] terms;
    private double bias;

    Nodes( Stream<Node> terms )
    {
        this.terms = terms.peek( node -> bias += node.bias ).toArray( Node[]::new );
    }

    @Override
    public int terms()
    {
        return terms.length;
    }

    @Override
    public Grammar.Term term( int offset )
    {
        return terms[offset];
    }

    @Override
    public double bound()
    {
        return bias;
    }

    @Override
    public Grammar.Term term( double bias )
    {
        double remaining = bias;
        for ( Node term : terms )
        {
            remaining -= term.bias;
            if ( remaining < 0 )
            {
                return term;
            }
        }
        throw new IllegalArgumentException( "Bias not in range: " + bias );
    }

    @Override
    public Iterator<Grammar.Term> iterator()
    {
        return new Iterator<Grammar.Term>()
        {
            int i;

            @Override
            public boolean hasNext()
            {
                return i < terms.length;
            }

            @Override
            public Grammar.Term next()
            {
                return terms[i++];
            }
        };
    }
}
