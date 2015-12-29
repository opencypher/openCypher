package org.opencypher.grammar;

import java.util.function.Function;

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

    void resolve( Production origin, Function<String, Production> productions, Dependencies dependencies )
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
            public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
            {
                visitor.visitEpsilon();
            }
        };
    }
}
