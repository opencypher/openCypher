package org.opencypher.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.opencypher.tools.xml.Child;

import static java.util.Collections.unmodifiableCollection;

abstract class Container extends Node
{
    private final List<Node> nodes = new ArrayList<>();

    @Child({Alternatives.class, Sequence.class, Literal.class, Characters.class, NonTerminal.class, Optional.class, Repetition.class})
    final void add( Node node )
    {
        nodes.add( node.replaceWithVerified() );
    }

    final Collection<Grammar.Term> terms()
    {
        return unmodifiableCollection( nodes );
    }

    @Override
    final void resolve( Production origin, Function<String, Production> productions, Dependencies dependencies )
    {
        for ( Node node : nodes )
        {
            node.resolve( origin, productions, dependencies );
        }
    }

    @Child
    final void literal( char[] buffer, int start, int length )
    {
        Literal.fromCharacters( buffer, start, length, this::add );
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
