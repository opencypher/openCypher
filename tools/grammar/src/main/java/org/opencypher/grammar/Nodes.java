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
