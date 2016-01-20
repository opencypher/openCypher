package org.opencypher.grammar;

import java.util.Collection;
import java.util.List;

import org.opencypher.tools.xml.LocationAware;

import static java.lang.System.identityHashCode;

abstract class Node extends Grammar.Term implements LocationAware
{
    private String path;
    private int lineNumber;
    private int columnNumber;

    @Override
    public final void location( String path, int lineNumber, int columnNumber )
    {
        this.path = path;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    @Override
    final Production addTo( Production production )
    {
        production.add( defensiveCopy() );
        return production;
    }

    @Override
    final Container addTo( Container container )
    {
        container.add( defensiveCopy() );
        return container;
    }

    @Override
    final Sequenced addTo( Sequenced sequenced )
    {
        sequenced.add( defensiveCopy() );
        return sequenced;
    }

    void resolve( Production origin, ProductionResolver resolver )
    {
    }

    Node replaceWithVerified()
    {
        return this;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals( Object obj );

    @Override
    public abstract String toString();

    /**
     * Override to return a defensive copy of this node, to prevent further mutation.
     *
     * Only nodes with publicly mutable state need to override this method.
     *
     * @return a shallow copy of this node.
     */
    Node defensiveCopy()
    {
        return this;
    }

    static Node epsilon()
    {
        return new Node()
        {
            @Override
            public int hashCode()
            {
                return identityHashCode( this );
            }

            @Override
            public boolean equals( Object obj )
            {
                return this == obj;
            }

            @Override
            public String toString()
            {
                return "EPSILON";
            }

            @Override
            public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param )
                    throws EX
            {
                return transformation.transformEpsilon( param );
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <EX extends Exception> TermTransformation<GrammarVisitor<EX>, Void, EX> visit()
    {
        return VISIT;
    }

    private static final TermTransformation VISIT = new TermTransformation<GrammarVisitor, Void, Exception>()
    {
        @Override
        public Void transformAlternatives( GrammarVisitor visitor, Collection<Grammar.Term> alternatives )
                throws Exception
        {
            visitor.visitAlternatives( alternatives );
            return null;
        }

        @Override
        public Void transformSequence( GrammarVisitor visitor, Collection<Grammar.Term> sequence )
                throws Exception
        {
            visitor.visitSequence( sequence );
            return null;
        }

        @Override
        public Void transformLiteral( GrammarVisitor visitor, String value )
                throws Exception
        {
            visitor.visitLiteral( value );
            return null;
        }

        @Override
        public Void transformNonTerminal( GrammarVisitor visitor, String productionName, Grammar.Term productionDef )
                throws Exception
        {
            visitor.visitNonTerminal( productionName, productionDef );
            return null;
        }

        @Override
        public Void transformOptional( GrammarVisitor visitor, Grammar.Term term )
                throws Exception
        {
            visitor.visitOptional( term );
            return null;
        }

        @Override
        public Void transformRepetition( GrammarVisitor visitor, int min, Integer max, Grammar.Term term )
                throws Exception
        {
            visitor.visitRepetition( min, max, term );
            return null;
        }

        @Override
        public Void transformEpsilon( GrammarVisitor visitor )
                throws Exception
        {
            visitor.visitEpsilon();
            return null;
        }

        @Override
        public Void transformCharacters( GrammarVisitor visitor, String wellKnownSetName, List<Exclusion> exclusions )
                throws Exception
        {
            visitor.visitCharacters( wellKnownSetName, exclusions );
            return null;
        }
    };
}
