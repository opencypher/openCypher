package org.opencypher.grammar;

import java.util.Map;
import java.util.Objects;

import org.opencypher.tools.xml.Child;

abstract class Sequenced extends Node
{
    private Node term;

    @Child({Alternatives.class, Sequence.class, Literal.class, NonTerminal.class, Optional.class, Repetition.class})
    final void add( Node node )
    {
        term = Sequence.implicit( term, node );
    }

    @Child
    final void literal( char[] buffer, int start, int length )
    {
        Literal.fromCharacters( buffer, start, length, this::add );
    }

    @Override
    final void resolve( String origin, Map<String, Production> productions, LogicalErrors errors )
    {
        if ( term != null )
        {
            term.resolve( origin, productions, errors );
        }
    }

    @Override
    public final <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
    {
        accept( term == null ? EPSILON : term, visitor );
    }

    abstract <EX extends Exception> void accept( Node term, GrammarVisitor<EX> visitor ) throws EX;

    @Override
    public final int hashCode()
    {
        return attributeHash() * 31 + Objects.hashCode( term );
    }

    @Override
    public final boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        Sequenced that = (Sequenced) obj;
        return attributeEquals( that ) && Objects.equals( term, that.term );
    }

    @Override
    public final String toString()
    {
        StringBuilder result = new StringBuilder().append( getClass().getSimpleName() );
        attributeString( result );
        return result.append( '[' ).append( term ).append( ']' ).toString();
    }

    int attributeHash()
    {
        return 0;
    }

    boolean attributeEquals( Sequenced that )
    {
        return true;
    }

    void attributeString( StringBuilder result )
    {
    }
}
