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
package org.opencypher.tools.xml;

import java.util.BitSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class NodeBuilder
{
    public static NodeBuilder tree( Class<?> root )
    {
        return Structure.tree( root ).factory( null, ( parent, child ) -> {
            // Add the child (the root root node) to its parent, since there is no parent of the root, do nothing.
        } );
    }

    public static NodeBuilder choice( NodeBuilder... rootBuilders )
    {
        if ( rootBuilders.length == 1 )
        {
            return rootBuilders[0];
        }
        int size = rootBuilders.length;
        // check if we have any combined NodeBuilders in the input already
        for ( NodeBuilder builder : rootBuilders )
        {
            if ( builder.uri == null )
            {
                size = size - 1 + builder.children.length;
            }
        }
        if ( size != rootBuilders.length )
        {
            // Unpack the combined NodeBuilders
            NodeBuilder[] builders = new NodeBuilder[size];
            for ( int i = 0, j = 0; i < rootBuilders.length; i++ )
            {
                NodeBuilder builder = rootBuilders[i];
                if ( builder.uri == null )
                {
                    for ( int k = 0; k < builder.children.length; k++ )
                    {
                        builders[j++] = builder.children[k];
                    }
                }
                else
                {
                    builders[j++] = builder;
                }
            }
        }
        // Create a combined NodeBuilder
        return new NodeBuilder( rootBuilders );
    }

    final String uri, name;
    private final AttributeHandler[] attributes;
    private final CharactersHandler characters, comments, headers;
    private final NodeBuilder[] children;
    private final Function<Object, Object> factory;
    private final BiConsumer<Object, Object> handler;

    NodeBuilder(
            String uri, String name, AttributeHandler[] attributes,
            CharactersHandler characters, CharactersHandler comments, CharactersHandler headers,
            NodeBuilder[] children, Function<Object, Object> factory, BiConsumer<Object, Object> handler )
    {
        this.uri = uri;
        this.name = name;
        this.attributes = attributes;
        this.characters = characters;
        this.comments = comments;
        this.headers = headers;
        this.children = children;
        this.factory = factory;
        this.handler = handler;
    }

    private NodeBuilder( NodeBuilder[] builders )
    {
        this.name = null;
        this.uri = null;
        this.attributes = null;
        this.characters = null;
        this.comments = null;
        this.headers = null;
        this.children = builders;
        this.factory = null;
        this.handler = null;
    }

    @Override
    public String toString()
    {
        return String.format( "Element{uri='%s', name='%s'}", uri, name );
    }

    public Object create( Object parent )
    {
        return factory.apply( parent );
    }

    public void child( Object parent, Object child )
    {
        handler.accept( parent, child );
    }

    public boolean attribute(
            BitSet remaining, Object target, Resolver resolver, String uri, String name, String type, String value )
    {
        for ( int i = 0; i < attributes.length; i++ )
        {
            AttributeHandler attribute = attributes[i];
            if ( attribute.matches( uri, name ) )
            {
                attribute.apply( target, resolver, value );
                remaining.clear( i );
                return true;
            }
        }
        return false;
    }

    public void characters( Object target, char[] buffer, int start, int length )
    {
        characters.characters( target, buffer, start, length );
    }

    public void comment( Object target, char[] buffer, int start, int length )
    {
        comments.characters( target, buffer, start, length );
    }

    public void header( Object target, char[] buffer )
    {
        headers.characters( target, buffer, 0, buffer.length );
    }

    public NodeBuilder child( String uri, String name )
    {
        for ( NodeBuilder child : children )
        {
            if ( child.matches( uri, name ) )
            {
                return child;
            }
        }
        if ( this.name == null )
        {
            throw new IllegalArgumentException( "No such root: '" + name + "' in namespace " + uri );
        }
        else
        {
            throw new IllegalArgumentException(
                    "No such child: '" + name + "' in namespace " + uri +
                            " of '" + this.name + "' in namespace " + this.uri );
        }
    }

    public boolean matches( String uri, String name )
    {
        return this.uri.equalsIgnoreCase( uri ) && this.name.equalsIgnoreCase( name );
    }

    public BitSet requiredAttributes()
    {
        BitSet required = new BitSet( attributes.length );
        for ( int i = 0; i < attributes.length; i++ )
        {
            if ( !attributes[i].optional )
            {
                required.set( i );
            }
        }
        return required;
    }

    public void verifyRequiredAttributes( BitSet required )
    {
        if ( required.cardinality() != 0 )
        {
            throw new IllegalArgumentException( required.stream().mapToObj( ( i ) -> attributes[i].name ).collect(
                    Collectors.joining( ", ", "Missing required attributes: ", "" ) ) );
        }
    }
}
