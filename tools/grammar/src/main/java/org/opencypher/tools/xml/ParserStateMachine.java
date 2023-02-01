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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

class ParserStateMachine extends DefaultHandler2
{
    private final Resolver resolver;
    private final Set<XmlParser.Option> options;
    private Node node;
    private Map<String, String> prefixToUri, uriToPrefix;
    private Locator locator;

    ParserStateMachine( Resolver resolver, NodeBuilder builder, Set<XmlParser.Option> options )
    {
        this.resolver = resolver;
        this.options = options;
        node = new BaseNode( builder );
    }

    @Override
    public void setDocumentLocator( Locator locator )
    {
        this.locator = locator;
    }

    public Object produceRoot()
    {
        return node.value;
    }

    @Override
    public void startDocument() throws SAXException
    {
        prefixToUri = new HashMap<>();
        uriToPrefix = new HashMap<>();
    }

    @Override
    public void endDocument() throws SAXException
    {
        prefixToUri = uriToPrefix = null;
    }

    @Override
    public void startPrefixMapping( String prefix, String uri ) throws SAXException
    {
        prefixToUri.put( prefix, uri );
        uriToPrefix.put( uri, prefix );
    }

    @Override
    public void endPrefixMapping( String prefix ) throws SAXException
    {
        String uri = prefixToUri.remove( prefix );
        if ( uri == null )
        {
            throw new SAXParseException( "prefix not mapped: " + prefix, locator );
        }
        String removed = uriToPrefix.remove( uri );
        if ( !prefix.equals( removed ) )
        {
            throw new SAXParseException( "uri not mapped: " + uri, locator );
        }
    }

    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {
        try
        {
            node = node.child( uri, localName, locator );
            BitSet required = node.builder.requiredAttributes();
            for ( int i = 0, len = attributes.getLength(); i < len; i++ )
            {
                attribute( required,
                           attributes.getURI( i ),
                           attributes.getLocalName( i ),
                           attributes.getType( i ),
                           attributes.getValue( i ) );
            }
            node.builder.verifyRequiredAttributes( required );
        }
        catch ( Exception e )
        {
            throw new SAXParseException( e.getMessage(), locator, e );
        }
    }

    private void attribute( BitSet required, String uri, String name, String type, String value ) throws SAXException
    {
        if ( uri.isEmpty() )
        {
            uri = prefixToUri.get( "" );
            if ( uri == null )
            {
                uri = "";
            }
        }
        if ( !node.builder.attribute( required, node.value, resolver, uri, name, type, value ) )
        {
            if ( options.contains( XmlParser.Option.FAIL_ON_UNKNOWN_ATTRIBUTE ) )
            {
                throw new SAXException( "Unknown attribute: " + name + " in namespace " + uri );
            }
        }
    }

    @Override
    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        try
        {
            node = node.pop();
        }
        catch ( Exception e )
        {
            if ( e.getClass() == RuntimeException.class && e.getCause() instanceof SAXException )
            {
                e = (SAXException) e.getCause();
            }
            throw new SAXParseException( e.getMessage(), locator, e );
        }
    }

    @Override
    public void characters( char[] buffer, int start, int length ) throws SAXException
    {
        try
        {
            node.builder.characters( node.value, buffer, start, length );
        }
        catch ( Exception e )
        {
            throw new SAXParseException( e.getMessage(), locator, e );
        }
    }

    @Override
    public void comment( char[] buffer, int start, int length ) throws SAXException
    {
        node.comment( buffer, start, length );
    }

    private static class Node
    {
        final Node parent;
        final NodeBuilder builder;
        final Object value;

        Node( Node parent, NodeBuilder builder, Object value )
        {
            this.parent = parent;
            this.builder = builder;
            this.value = value;
        }

        Node child( String uri, String name, Locator locator ) throws SAXException
        {
            NodeBuilder child = builder.child( uri, name );
            if ( child == null )
            {
                throw new SAXException(
                        "element '" + name + "' in namespace '" + uri + "' is not a valid child of element '" +
                        builder.name + "' in namespace '" + builder.uri + "'" );
            }
            Object value = child.create( this.value );
            if ( value instanceof LocationAware )
            {
                ((LocationAware) value).location( locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber() );
            }
            return new Node( this, child, value );
        }

        void add( Node child )
        {
            child.builder.child( value, child.value );
        }

        Node pop()
        {
            if ( parent != null )
            {
                parent.add( this );
            }
            return parent;
        }

        void comment( char[] buffer, int start, int length )
        {
            builder.comment( value, buffer, start, length );
        }
    }

    private static class BaseNode extends Node
    {
        private Object headers;

        BaseNode( NodeBuilder builder )
        {
            super( null, builder, null );
        }

        @Override
        Node child( String uri, String name, Locator locator ) throws SAXException
        {
            NodeBuilder root;
            if ( builder.name == null )
            {
                root = builder.child( uri, name );
            }
            else
            {
                root = builder;
            }
            if ( !root.uri.equalsIgnoreCase( uri ) || !root.name.equalsIgnoreCase( name ) )
            {
                throw new SAXException(
                        "Root element must be '" + root.name + "' in namespace '" + root.uri +
                        "', but was '" + name + "' in namespace '" + uri + "'" );
            }
            Object value = root.create( null );
            if ( headers instanceof char[] )
            {
                root.header( value, (char[]) headers );
            }
            else if ( headers instanceof List<?> )
            {
                @SuppressWarnings("unchecked")
                List<char[]> comments = (List<char[]>) headers;
                for ( char[] comment : comments )
                {
                    root.header( value, comment );
                }
            }
            if ( value instanceof LocationAware )
            {
                ((LocationAware) value).location( locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber()  );
            }
            return new RootNode( root, value );
        }

        @Override
        void comment( char[] buffer, int start, int length )
        {
            if ( headers == null )
            {
                headers = Arrays.copyOfRange( buffer, start, start + length );
            }
            else if ( headers instanceof char[] )
            {
                List<char[]> list = new ArrayList<>( 2 );
                list.add( (char[]) headers );
                list.add( Arrays.copyOfRange( buffer, start, start + length ) );
                headers = list;
            }
            else
            {
                @SuppressWarnings("unchecked")
                List<char[]> list = (List<char[]>) headers;
                list.add( Arrays.copyOfRange( buffer, start, start + length ) );
            }
        }

        @Override
        Node pop()
        {
            throw new UnsupportedOperationException( "should never be called" );
        }
    }

    private static class RootNode extends Node
    {
        RootNode( NodeBuilder builder, Object value )
        {
            super( null, builder, value );
        }

        @Override
        Node pop()
        {
            return this;
        }
    }
}
