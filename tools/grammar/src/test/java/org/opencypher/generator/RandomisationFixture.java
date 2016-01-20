package org.opencypher.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.opencypher.grammar.Grammar;

import static org.opencypher.grammar.Grammar.literal;

public final class RandomisationFixture
{
    private final Randomisation random = new Predictable( () -> {
        State state = state();
        RandomisationFixture.this.state = null;
        return state;
    } );

    private State state = new State();

    public Randomisation random()
    {
        return random;
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
            throw new IllegalStateException( Randomisation.class.getSimpleName() + " has already been initialized." );
        }
        return state;
    }

    private static class State
    {
        final Set<Object> choices = new HashSet<>();
        final Map<Repetition, Integer> repetitions = new HashMap<>();
        public Set<Integer> codepoints = new HashSet<>();
    }

    private static class Predictable implements Randomisation
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
        public int repetition( int min, int max )
        {
            return repetitions( new Repetition( min, max ) );
        }

        @Override
        public int repetition( int min )
        {
            return repetitions( new Repetition( min, null ) );
        }

        @Override
        public <T> T choice( Collection<T> alternatives )
        {
            T chosen = null;
            for ( T alternative : alternatives )
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
        public int choice( int[] codepoints )
        {
            int chosen = -1;
            for ( int codepoint : codepoints )
            {
                if ( state.get().codepoints.contains( codepoint ) )
                {
                    if ( chosen != -1 )
                    {
                        throw new IllegalStateException();
                    }
                    chosen = codepoint;
                }
            }
            if ( chosen == -1 )
            {
                throw new IllegalStateException();
            }
            return chosen;
        }

        @Override
        public int anyChar()
        {
            throw new UnsupportedOperationException( "not implemented" );
        }
    }
}
