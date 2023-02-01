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
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.EnumSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class for parsing an XML document into an object graph.
 *
 * @param <Root> The type of object that is constructed by the parser.
 * @see org.opencypher.tools.xml The package documentation for usage and context.
 */
public final class XmlParser<Root>
{
    /**
     * Options that can be passed to an XML parser.
     */
    public enum Option
    {
        /** Fail if an attribute is encountered in the document that cannot be assigned to the corresponding object. */
        FAIL_ON_UNKNOWN_ATTRIBUTE
    }

    /**
     * Create a new {@linkplain XmlParser XML parser} for the given type.
     *
     * @param root the type of object that corresponds to the root element of the XML document the parser should parse.
     * @param <T>  the type of object constructed by the parser.
     * @return a new XML parser for the given type.
     */
    public static <T> XmlParser<T> xmlParser( Class<T> root )
    {
        return new XmlParser<>( root, NodeBuilder.tree( root ) );
    }

    @SafeVarargs
    public static <T> XmlParser<T> combine( Class<T> base, XmlParser<? extends T>... parsers )
    {
        NodeBuilder[] rootBuilders = new NodeBuilder[parsers.length];
        for ( int i = 0; i < parsers.length; i++ )
        {
            rootBuilders[i] = parsers[i].builder;
        }
        return new XmlParser<>( base, NodeBuilder.choice( rootBuilders ) );
    }

    /**
     * Parse the XML document at the given path.
     *
     * @param input   the path at which to find the XML document to parse.
     * @param options configuration for the XML parser.
     * @return the object constructed from parsing the XML document.
     * @throws ParserConfigurationException if an XML (SAX) parser cannot be created.
     * @throws SAXException                 if parsing the XML document failed.
     * @throws IOException                  if reading the XML document failed.
     */
    public Root parse( Path input, Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        Path base = input.getParent();
        return new Resolver( options )
        {
            @Override
            Path path( String path )
            {
                return base.resolve( path );
            }
        }.parse( input, this );
    }

    /**
     * Parse the XML document from the given reader.
     *
     * Note that when parsing from a reader referenced files cannot be resolved, this requires
     * {@linkplain #parse(Path, Option...) parsing from a file}.
     *
     * @param input   the reader to read the XML document from.
     * @param options configuration for the XML parser.
     * @return the object constructed from parsing the XML document.
     * @throws ParserConfigurationException if an XML (SAX) parser cannot be created.
     * @throws SAXException                 if parsing the XML document failed.
     * @throws IOException                  if reading the XML document failed.
     */
    public Root parse( Reader input, Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        return parse( new Resolver( options )
        {
            @Override
            Path path( String path )
            {
                throw new IllegalStateException( "Cannot resolve path in input from reader" );
            }
        }, new InputSource( input ) );
    }

    /**
     * Parse the XML document from the given input stream.
     *
     * Note that when parsing from an input stream referenced files cannot be resolved, this requires
     * {@linkplain #parse(Path, Option...) parsing from a file}.
     *
     * @param input   the stream to read the XML document from.
     * @param options configuration for the XML parser.
     * @return the object constructed from parsing the XML document.
     * @throws ParserConfigurationException if an XML (SAX) parser cannot be created.
     * @throws SAXException                 if parsing the XML document failed.
     * @throws IOException                  if reading the XML document failed.
     */
    public Root parse( InputStream input, Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        return parse( new Resolver( options )
        {
            @Override
            Path path( String path )
            {
                throw new IllegalStateException( "Cannot resolve path in input from stream" );
            }
        }, new InputSource( input ) );
    }

    private Root parse( Resolver resolver, InputSource input )
            throws IOException, SAXException, ParserConfigurationException
    {
        return parse( resolver, input, resolver.options );
    }

    Root parse( Resolver resolver, InputSource input, EnumSet<XmlParser.Option> options )
            throws ParserConfigurationException, SAXException, IOException
    {
        ParserStateMachine stateMachine = new ParserStateMachine( resolver, builder, options );
        SAXParser parser = saxParser( true );
        parser.setProperty( LEXICAL_HANDLER, stateMachine ); // handle XML comments as well
        parser.parse( input, stateMachine );
        return root.cast( stateMachine.produceRoot() );
    }

    private static final String LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";

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

    static SAXParser saxParser( boolean validateDTD ) throws ParserConfigurationException, SAXException
    {
        SAXParserFactory sax = SAXParserFactory.newInstance();
        sax.setNamespaceAware( true );
        if ( !validateDTD )
        {
            sax.setValidating( false );
            sax.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
        }
        return sax.newSAXParser();
    }
}
