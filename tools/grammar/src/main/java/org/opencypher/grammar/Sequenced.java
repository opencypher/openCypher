package org.opencypher.grammar;

import java.util.Objects;

import org.opencypher.tools.xml.Child;

abstract class Sequenced extends Node
{
    private Node term;

    @Child({AlternativesNode.class, SequenceNode.class, LiteralNode.class, CharacterSetNode.class, NonTerminalNode.class,
            OptionalNode.class, RepetitionNode.class})
    final void add( Node node )
    {
        term = SequenceNode.implicit( term, node.replaceWithVerified() );
    }

    @Child
    final void literal( char[] buffer, int start, int length )
    {
        LiteralNode.fromCharacters( buffer, start, length, this::add );
    }

    @Override
    final void resolve( ProductionNode origin, ProductionResolver resolver )
    {
        if ( term != null )
        {
            term.resolve( origin, resolver );
        }
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transform( transformation, param, term() );
    }

    public final Node term()
    {
        return term == null ? epsilon() : term;
    }

    abstract <T, P, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param, Node term )
            throws EX;

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
        return attributeEquals( that ) && Objects.equals( this.term, that.term );
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
