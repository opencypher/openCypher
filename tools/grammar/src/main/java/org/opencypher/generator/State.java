package org.opencypher.generator;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.GrammarVisitor;
import org.opencypher.tools.output.Output;

abstract class State
{
    public static State production( String name, Grammar.Term term )
    {
        return new State( new Names( name ) )
        {
            @Override
            public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
            {
                term.accept( visitor );
            }

            @Override
            public State next()
            {
                return null;
            }
        };
    }

    public abstract <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX;

    private final Names names;

    private State( Names names )
    {
        this.names = names;
    }

    public abstract State next();

    public State sequence( Collection<Grammar.Term> sequence )
    {
        return new Sequence( names, next(), sequence );
    }

    public State term( String name, Grammar.Term term )
    {
        return new NonTerminal( names.push( name ), term, next() );
    }

    public State repeat( int times, Grammar.Term term )
    {
        if ( times == 0 )
        {
            return next();
        }
        else
        {
            return new Repetition( names, term, times, next() );
        }
    }

    public ProductionReplacement.Context<Void> context( String production, Consumer<String> target )
    {
        return new ProductionReplacement.Context<Void>()
        {
            @Override
            public Node node()
            {
                throw new UnsupportedOperationException( "not implemented" );
            }

            @Override
            public void generateDefault()
            {
                throw new UnsupportedOperationException( "not implemented" );
            }

            @Override
            public Void context()
            {
                return null;
            }

            @Override
            public void write( CharSequence str )
            {
                target.accept( str.toString() );
            }

            @Override
            public void write( int codePoint )
            {
                throw new UnsupportedOperationException( "not implemented" );
            }

            @Override
            public Output output()
            {
                throw new UnsupportedOperationException( "not implemented" );
            }
        };
    }

    private static class NonTerminal extends State
    {
        private final Grammar.Term term;
        private final State next;

        NonTerminal( Names names, Grammar.Term term, State next )
        {
            super( names );
            this.term = term;
            this.next = next;
        }

        @Override
        public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
        {
            term.accept( visitor );
        }

        @Override
        public State next()
        {
            return next;
        }
    }

    private static class Sequence extends State
    {
        private final Iterator<Grammar.Term> iterator;
        private final State next;

        Sequence( Names names, State next, Iterable<Grammar.Term> sequence )
        {
            super( names );
            this.next = next;
            this.iterator = sequence.iterator();
            assert this.iterator.hasNext();
        }

        @Override
        public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
        {
            iterator.next().accept( visitor );
        }

        @Override
        public State next()
        {
            if ( iterator.hasNext() )
            {
                return this;
            }
            else
            {
                return next;
            }
        }
    }

    private static class Repetition extends State
    {
        private final Grammar.Term term;
        private int times;
        private final State next;

        Repetition( Names names, Grammar.Term term, int times, State next )
        {
            super( names );
            this.term = term;
            this.times = times;
            this.next = next;
        }

        @Override
        public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
        {
            term.accept( visitor );
        }

        @Override
        public State next()
        {
            if ( --times > 0 )
            {
                return this;
            }
            return next;
        }
    }

    private final static class Names
    {
        private final Names parent;
        private final String name;

        public Names( String name )
        {
            this.parent = null;
            this.name = name;
        }

        public Names push( String name )
        {
            return new Names( this, name );
        }

        private Names( Names parent, String name )
        {
            this.parent = parent;
            this.name = name;
        }

        public String get( int requested, int current )
        {
            if ( requested == current )
            {
                return name;
            }
            else if ( parent == null )
            {
                throw new NoSuchElementException( "Too deep: " + requested );
            }
            else
            {
                return parent.get( requested, current + 1 );
            }
        }
    }
}
