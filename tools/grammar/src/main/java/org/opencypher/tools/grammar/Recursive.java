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
package org.opencypher.tools.grammar;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.grammar.TermTransformation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class Recursive
{
    static List<Recursive> findLeftRecursive( Grammar grammar )
    {
        List<Recursive> result = new ArrayList<>();
        grammar.accept( production ->
        {
            Builder builder = new Builder( true, production );
            production.definition().transform( builder, true );
            builder.addTo( result );
        } );
        return result;
    }

    final Production production;
    private final List<Trace> recursions;

    private Recursive( Production production, List<Trace> recursions )
    {
        this.production = production;
        this.recursions = recursions;
    }

    public void accept( TraceVisitor visitor )
    {
        for ( Trace trace : recursions )
        {
            visitor.trace( production, trace.left, trace.trace );
        }
    }

    interface TraceVisitor
    {
        void trace( Production production, boolean left, Production[] trace );
    }

    private static class Trace
    {
        private final boolean left;
        private final Production[] trace;

        Trace( boolean left, Production[] trace )
        {
            this.left = left;
            this.trace = trace;
        }
    }

    private static class Builder implements TermTransformation<Boolean,Boolean,RuntimeException>
    {
        Builder( boolean leftOnly, Production root )
        {
            this( leftOnly, new Trail( null, root ), root, new ArrayList<>(), new HashSet<>() );
        }

        private final boolean leftOnly;
        private final Production root;
        private final Trail trail;
        private final List<Trace> recursions;
        private final Set<String> seen;

        private Builder( boolean leftOnly, Trail trail, Production root, List<Trace> recursions, Set<String> seen )
        {
            this.leftOnly = leftOnly;
            this.trail = trail;
            this.root = root;
            this.recursions = recursions;
            this.seen = seen;
        }

        void addTo( List<Recursive> result )
        {
            if ( !recursions.isEmpty() )
            {
                result.add( new Recursive( root, recursions ) );
            }
        }

        private void traverse( Production production, Boolean first )
        {
            production.definition().transform( new Builder( leftOnly, new Trail( trail, production ), root, recursions, seen ), first );
        }

        @Override
        public Boolean transformNonTerminal( Boolean first, NonTerminal nonTerminal )
        {
            if ( Objects.equals( root.name(), nonTerminal.productionName() ) )
            {
                recursions.add( trail.build( first ) );
            }
            else if ( /*seen.add( nonTerminal.productionName() ) ||*/ !trail.contains( nonTerminal.production() ) )
            {
                traverse( nonTerminal.production(), first );
            }
            return false;
        }

        @Override
        public Boolean transformAlternatives( Boolean first, Alternatives alternatives )
        {
            boolean stillFirst = first;
            for ( Grammar.Term term : alternatives )
            {
                boolean possiblyEmpty = term.transform( this, first );
                stillFirst |= possiblyEmpty;
            }
            return stillFirst;
        }

        @Override
        public Boolean transformSequence( Boolean first, Sequence sequence )
        {
            for ( Grammar.Term term : sequence )
            {
                first = term.transform( this, first );
                if ( leftOnly && !first )
                {
                    return false;
                }
            }
            return first;
        }

        @Override
        public Boolean transformOptional( Boolean first, Optional optional )
        {
            return first;
        }

        @Override
        public Boolean transformRepetition( Boolean first, Repetition repetition )
        {
            return first && repetition.minTimes() == 0;
        }

        @Override
        public Boolean transformEpsilon( Boolean first )
        {
            return first;
        }

        @Override
        public Boolean transformLiteral( Boolean first, Literal literal )
        {
            return false;
        }

        @Override
        public Boolean transformCharacters( Boolean first, CharacterSet characters )
        {
            return false;
        }
    }

    private static class Trail
    {
        private final Trail trail;
        private final Production production;

        Trail( Trail trail, Production production )
        {
            this.trail = trail;
            this.production = production;
        }

        boolean contains( Production production )
        {
            return this.production == production || (trail != null && trail.contains( production ));
        }

        Trace build( boolean left )
        {
            Production[] result = new Production[size()];
            populate( result, result.length - 1 );
            return new Trace( left, result );
        }

        private int size()
        {
            return trail == null ? 1 : trail.size() + 1;
        }

        private void populate( Production[] result, int pos )
        {
            result[pos] = production;
            if ( trail != null )
            {
                trail.populate( result, pos - 1 );
            }
        }
    }
}
