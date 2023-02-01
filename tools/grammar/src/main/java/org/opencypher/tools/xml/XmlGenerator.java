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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A utility class for generating XML documents.
 */
// TODO: this is clunky and should probably be replaced with XMLStreamWriter
public abstract class XmlGenerator implements XMLReader
{
    private static final char[] WHITESPACE = new char[1025];

    static
    {
        WHITESPACE[0] = '\n';
        Arrays.fill( WHITESPACE, 1, 1024, ' ' );
    }

    private int level;
    private boolean children;

    protected static void generate( XmlGenerator generator, Writer writer ) throws TransformerException
    {
        generate( generator, new StreamResult( writer ) );
    }

    protected static void generate( XmlGenerator generator, OutputStream stream ) throws TransformerException
    {
        generate( generator, new StreamResult( stream ) );
    }

    protected static Document generate( XmlGenerator generator ) throws TransformerException
    {
        DOMResult dom = new DOMResult();
        generate( generator, dom );
        return (Document) dom.getNode();
    }

    static void generate( XmlGenerator generator, Result result ) throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform( new SAXSource( generator, new InputSource() ), result );
    }

    private EntityResolver resolver;
    private DTDHandler dtdHandler;
    private ContentHandler handler;
    private ErrorHandler errors;
    private Map<String, String> uris;

    @Override
    public boolean getFeature( String name ) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return false;
    }

    @Override
    public void setFeature( String name, boolean value ) throws SAXNotRecognizedException, SAXNotSupportedException
    {
    }

    @Override
    public Object getProperty( String name ) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return null;
    }

    @Override
    public void setProperty( String name, Object value ) throws SAXNotRecognizedException, SAXNotSupportedException
    {
    }

    @Override
    public void setEntityResolver( EntityResolver resolver )
    {
        this.resolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver()
    {
        return resolver;
    }

    @Override
    public void setDTDHandler( DTDHandler handler )
    {
        this.dtdHandler = handler;
    }

    @Override
    public DTDHandler getDTDHandler()
    {
        return dtdHandler;
    }

    @Override
    public void setContentHandler( ContentHandler handler )
    {
        this.handler = handler;
    }

    @Override
    public ContentHandler getContentHandler()
    {
        return handler;
    }

    @Override
    public void setErrorHandler( ErrorHandler handler )
    {
        this.errors = handler;
    }

    @Override
    public ErrorHandler getErrorHandler()
    {
        return errors;
    }

    @Override
    public void parse( InputSource input ) throws IOException, SAXException
    {
        generate();
    }

    @Override
    public void parse( String systemId ) throws IOException, SAXException
    {
        generate();
    }

    protected abstract void generate() throws SAXException;

    protected static class AttributesBuilder extends AttributesImpl
    {
        private static final String CDATA = "CDATA";
        private final Map<String, String> uri;

        protected AttributesBuilder( XmlGenerator generator )
        {
            this.uri = generator.uris;
        }

        public AttributesBuilder attribute( String name, String value )
        {
            return attribute( "", name, value );
        }

        public AttributesBuilder attribute( String prefix, String name, String value )
        {
            addAttribute( uri.get( prefix ), name, qualify( prefix, name ), CDATA, value );
            return this;
        }
    }

    protected final AttributesBuilder attribute( String name, String value )
    {
        return attribute( "", name, value );
    }

    protected final AttributesBuilder attribute( String prefix, String name, String value )
    {
        return new AttributesBuilder( this ).attribute( prefix, name, value );
    }

    protected final void startDocument() throws SAXException
    {
        uris = new HashMap<>();
        handler.startDocument();
    }

    protected final void endDocument() throws SAXException
    {
        handler.endDocument();
        uris = null;
    }

    protected final void startPrefixMapping( String prefix, String uri ) throws SAXException
    {
        uris.put( prefix, uri );
        handler.startPrefixMapping( prefix, uri );
    }

    protected final void endPrefixMapping( String prefix ) throws SAXException
    {
        handler.endPrefixMapping( prefix );
        uris.remove( prefix );
    }

    protected final void startElement( String localName ) throws SAXException
    {
        startElement( "", localName );
    }

    protected final void startElement( String localName, Attributes attributes ) throws SAXException
    {
        startElement( "", localName, attributes );
    }

    protected final void startElement( String prefix, String localName ) throws SAXException
    {
        startElement( prefix, localName, new AttributesImpl() );
    }

    protected final void comment( CharSequence content ) throws SAXException
    {
        char[] chars = new char[content.length()];
        for ( int i = 0; i < chars.length; i++ )
        {
            chars[i] = content.charAt( i );
        }
        ((LexicalHandler) handler).comment( chars, 0, chars.length );
    }

    protected final void startElement( String prefix, String localName, Attributes attributes ) throws SAXException
    {
        newline();
        level++;
        handler.startElement( uris.get( prefix ), localName, qualify( prefix, localName ), attributes );
        children = false;
    }

    protected final void endElement( String localName ) throws SAXException
    {
        endElement( "", localName );
    }

    protected final void endElement( String prefix, String localName ) throws SAXException
    {
        level--;
        if ( children )
        {
            newline();
        }
        children = true;
        handler.endElement( uris.get( prefix ), localName, qualify( prefix, localName ) );
    }
    
    protected final void endElementSameLine( String prefix, String localName ) throws SAXException
    {
        level--;
        children = true;
        handler.endElement( uris.get( prefix ), localName, qualify( prefix, localName ) );
    }
    protected final void println( CharSequence content ) throws SAXException
    {
        newline();
        characters( content );
    }

    private void newline() throws SAXException
    {
        characters( WHITESPACE, 0, level * 2 + 1 );
    }

    protected final void characters( CharSequence content ) throws SAXException
    {
        char[] chars = new char[content.length()];
        for ( int i = 0; i < chars.length; i++ )
        {
            chars[i] = content.charAt( i );
        }
        characters( chars, 0, chars.length );
    }

    protected final void characters( char[] ch, int start, int length ) throws SAXException
    {
        children = true;
        handler.characters( ch, start, length );
    }

    private static String qualify( String prefix, String name )
    {
        return prefix.isEmpty() ? name : prefix + ":" + name;
    }
}
