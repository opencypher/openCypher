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
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

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
import org.xml.sax.helpers.AttributesImpl;

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

        private AttributesBuilder( Map<String, String> uri )
        {
            this.uri = uri;
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
        return new AttributesBuilder( uris ).attribute( prefix, name, value );
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
