/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.grammar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

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
    final boolean resolve( ProductionNode origin, ProductionResolver resolver )
    {
        ArrayList<Node> nodes = new ArrayList<>( this.nodes );
        nodes.stream().filter( node -> !node.resolve( origin, resolver ) ).forEach( this.nodes::remove );
        return !nodes.isEmpty();
    }

    @Child
    final void literal( char[] buffer, int start, int length )
    {
        LiteralNode.fromCharacters( buffer, start, length, this::add );
    }

    final Node addAll( Iterable<? extends Grammar.Term> terms )
    {
        for ( Grammar.Term term : terms )
        {
            term.addTo( this );
        }
        return this;
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
