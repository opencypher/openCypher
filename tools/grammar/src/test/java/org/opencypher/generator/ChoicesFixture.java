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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.opencypher.grammar.BiasedTerms;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Optional;

import static org.opencypher.grammar.Grammar.literal;

public final class ChoicesFixture
{
    private final Choices choices = new Predictable( () -> {
        State state = state();
        ChoicesFixture.this.state = null;
        return state;
    } );

    private State state = new State();

    public Choices random()
    {
        return choices;
    }

    public void pick( String literal )
    {
        pick( literal( literal ) );
    }

    public void pick( Grammar.Term term )
    {
        state().choices.add( term );
    }

    public void pick( int codePoint )
    {
        state().codepoints.add( codePoint );
    }

    public void includeOptional()
    {
        repeat( 1, Repetition.OPTIONAL );
    }

    public void excludeOptional()
    {
        repeat( 0, Repetition.OPTIONAL );
    }

    public void repeat( int times, Repetition invocation )
    {
        state().repetitions.put( invocation, times );
    }

    public static Repetition onRepetition( int min, int max )
    {
        return new Repetition( min, max );
    }

    public static Repetition onRepetition( int min )
    {
        return new Repetition( min, null );
    }

    public static final class Repetition
    {
        static final Repetition OPTIONAL = new Repetition( 0, 1 );
        private final int min;
        private final Integer max;

        private Repetition( int min, Integer max )
        {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            Repetition that = (Repetition) o;
            return this.min == that.min && Objects.equals( this.max, that.max );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( min, max );
        }
    }

    private State state()
    {
        if ( state == null )
        {
            throw new IllegalStateException( Choices.class.getSimpleName() + " has already been initialized." );
        }
        return state;
    }

    private static class State
    {
        final Set<Grammar.Term> choices = new HashSet<>();
        final Map<Repetition, Integer> repetitions = new HashMap<>();
        public Set<Integer> codepoints = new HashSet<>();
    }

    private static class Predictable implements Choices
    {
        private Supplier<State> state;

        Predictable( Supplier<State> state )
        {
            this.state = new Supplier<State>()
            {
                @Override
                public synchronized State get()
                {
                    if ( Predictable.this.state != this )
                    {
                        return Predictable.this.state.get();
                    }
                    State actual = state.get();
                    Predictable.this.state = () -> actual;
                    return actual;
                }
            };
        }

        private int repetitions( Repetition invocation )
        {
            Integer times = state.get().repetitions.get( invocation );
            if ( times == null )
            {
                throw new IllegalStateException();
            }
            return times;
        }

        @Override
        public Grammar.Term choose( Node location, BiasedTerms alternatives )
        {
            Grammar.Term chosen = null;
            for ( Grammar.Term alternative : alternatives )
            {
                if ( state.get().choices.contains( alternative ) )
                {
                    if ( chosen != null )
                    {
                        throw new IllegalStateException();
                    }
                    chosen = alternative;
                }
            }
            if ( chosen == null )
            {
                throw new IllegalStateException();
            }
            return chosen;
        }

        @Override
        public int repetition( Node location, org.opencypher.grammar.Repetition repetition )
        {
            return repetitions( new Repetition(
                    repetition.minTimes(), repetition.limited() ? repetition.maxTimes() : null ) );
        }

        @Override
        public boolean includeOptional( Node location, Optional optional )
        {
            return repetitions( Repetition.OPTIONAL ) > 0;
        }

        @Override
        public int codePoint( Node location, CharacterSet characters )
        {
            return characters.randomCodePoint( null/*since we only expect predictable character sets*/ );
        }
    }
}
