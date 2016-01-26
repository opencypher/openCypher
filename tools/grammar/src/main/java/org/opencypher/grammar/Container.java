package org.opencypher.grammar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencypher.tools.xml.Child;

abstract class Container extends Node implements Terms
{
    final List<Node> nodes = new ArrayList<>();

    @Child({AlternativesNode.class, SequenceNode.class, LiteralNode.class, CharacterSetNode.class,
            NonTerminalNode.class, OptionalNode.class, RepetitionNode.class})
    final void add( Node node )
    {
        nodes.add( node.replaceWithVerified() );
    }

    @Override
    public Iterator<Grammar.Term> iterator()
    {
        Iterator<Node> iterator = nodes.iterator();
        return new Iterator<Grammar.Term>()
        {
            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public Grammar.Term next()
            {
                return iterator.next();
            }
        };
    }

    @Override
    public int terms()
    {
        return nodes.size();
    }

    @Override
    public Grammar.Term term( int offset )
    {
        return nodes.get( offset );
    }

    @Override
    final void resolve( ProductionNode origin, ProductionResolver resolver )
    {
        for ( Node node : nodes )
        {
            node.resolve( origin, resolver );
        }
    }

    @Child
    final void literal( char[] buffer, int start, int length )
    {
        LiteralNode.fromCharacters( buffer, start, length, this::add );
    }

    final Node addAll( Grammar.Term first, Grammar.Term... more )
    {
        first.addTo( this );
        if ( more != null )
        {
            for ( Grammar.Term term : more )
            {
                term.addTo( this );
            }
        }
        return this;
    }

    @Override
    public final int hashCode()
    {
        int hash = attributeHash();
        for ( Node node : nodes )
        {
            hash = hash * 31 + node.hashCode();
        }
        return hash;
    }

    @Override
    public final boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( this.getClass() != obj.getClass() )
        {
            return false;
        }
        Container that = (Container) obj;
        return attributeEquals( that ) && nodes.equals( that.nodes );
    }

    @Override
    public final String toString()
    {
        StringBuilder result = new StringBuilder().append( getClass().getSimpleName() );
        result.setLength( result.length() - 4 );
        attributeString( result );
        result.append( '[' );
        String sep = "";
        for ( Node child : nodes )
        {
            result.append( sep ).append( child );
            sep = ", ";
        }
        return result.append( ']' ).toString();
    }

    int attributeHash()
    {
        return 0;
    }

    boolean attributeEquals( Container that )
    {
        return true;
    }

    void attributeString( StringBuilder result )
    {
    }
}
