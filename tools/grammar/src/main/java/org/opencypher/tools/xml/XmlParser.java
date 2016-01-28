/*
 * Copyright (c) 2015-2016 "Neo Technology,"
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
package org.opencypher.tools.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
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

    public Root parse( Path input, Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        Path base = input.getParent();
        return new Resolver()
        {
            @Override
            Path path( String path )
            {
                return base.resolve( path );
            }
        }.parse( input, this, options );
    }

    public Root parse( InputStream input, Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        return parse( new Resolver()
        {
            @Override
            Path path( String path )
            {
                throw new IllegalStateException( "Cannot resolve path in input from stream" );
            }
        }, new InputSource( input ), options );
    }

    Root parse( Resolver resolver, InputSource input, Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        EnumSet<Option> optionSet = EnumSet.noneOf( Option.class );
        if ( options != null )
        {
            Collections.addAll( optionSet, options );
        }
        ParserStateMachine stateMachine = new ParserStateMachine( resolver, builder, optionSet );
        SAXParser parser = saxParser();
        parser.setProperty( LEXICAL_HANDLER, stateMachine );
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

    static SAXParser saxParser() throws ParserConfigurationException, SAXException
    {
        SAXParserFactory sax = SAXParserFactory.newInstance();
        sax.setNamespaceAware( true );
        return sax.newSAXParser();
    }
}
