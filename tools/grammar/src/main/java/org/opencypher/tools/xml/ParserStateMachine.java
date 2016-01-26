package org.opencypher.tools.xml;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
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
                ((LocationAware) value).location(
                        locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber() );
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
    }

    private static class BaseNode extends Node
    {
        BaseNode( NodeBuilder builder )
        {
            super( null, builder, null );
        }

        @Override
        Node child( String uri, String name, Locator locator ) throws SAXException
        {
            if ( !builder.uri.equalsIgnoreCase( uri ) || !builder.name.equalsIgnoreCase( name ) )
            {
                throw new SAXException(
                        "Root element must be '" + builder.name + "' in namespace '" + builder.uri +
                        "', but was '" + name + "' in namespace '" + uri + "'" );
            }
            return new RootNode( builder, builder.create( null ) );
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
