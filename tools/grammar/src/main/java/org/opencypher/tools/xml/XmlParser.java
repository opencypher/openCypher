package org.opencypher.tools.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class XmlParser<Root>
{
    public enum Option
    {
        FAIL_ON_UNKNOWN_ATTRIBUTE
    }

    public static <T> XmlParser<T> xmlParser( Class<T> root )
    {
        return new XmlParser<>( root, NodeBuilder.tree( root ) );
    }

    public Root parse( InputStream input, Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        EnumSet<Option> optionSet = EnumSet.noneOf( Option.class );
        Collections.addAll( optionSet, options );
        ParserStateMachine stateMachine = new ParserStateMachine( builder, optionSet );
        saxParser().parse( input, stateMachine );
        return root.cast( stateMachine.produceRoot() );
    }

    private final Class<Root> root;
    private final NodeBuilder builder;

    private XmlParser( Class<Root> root, NodeBuilder builder )
    {
        this.root = root;
        this.builder = builder;
    }

    @Override
    public String toString()
    {
        return String.format( "XmlParser{%s as %s}", builder, root );
    }

    static SAXParser saxParser() throws ParserConfigurationException, SAXException
    {
        SAXParserFactory sax = SAXParserFactory.newInstance();
        sax.setNamespaceAware( true );
        return sax.newSAXParser();
    }
}
