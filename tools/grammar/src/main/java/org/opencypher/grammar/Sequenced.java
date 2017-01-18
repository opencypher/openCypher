/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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
 */
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
    final boolean resolve( ProductionNode origin, ProductionResolver resolver )
    {
        return term == null || term.resolve( origin, resolver );
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
