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
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * An XML attribute value that corresponds to an XML file that can be {@linkplain #parse(XmlParser) parsed}.
 */
public final class XmlFile
{
    private final Resolver resolver;
    private final Path path;

    XmlFile( Resolver resolver, Path path )
    {
        this.resolver = resolver;
        this.path = path;
    }

    /**
     * Parse this XML document with the given parser.
     *
     * @param parser the parser to parse the document with.
     * @param <T>    the type of the element produced by parsing the XML document.
     * @return the object parsed from the XML document.
     * @throws ParserConfigurationException if an XML (SAX) parser cannot be created.
     * @throws SAXException                 if parsing the XML document failed.
     * @throws IOException                  if reading the XML document failed.
     */
    public <T> T parse( XmlParser<T> parser )
            throws ParserConfigurationException, SAXException, IOException
    {
        return resolver.parse( path, parser );
    }

    /**
     * Parse this XML document with the given parser if it has not already been parsed.
     *
     * If this XML document has not been parsed already, it is parsed and the result returned. If it has been parsed it
     * is not parsed again, instead {@link Optional#empty() nothing} is returned.
     *
     * @param parser the parser to parse the document with.
     * @param <T>    the type of the element produced by parsing the XML document.
     * @return the object parsed from the XML document, or {@link Optional#empty() nothing} if the document has been
     * parsed before.
     * @throws ParserConfigurationException if an XML (SAX) parser cannot be created.
     * @throws SAXException                 if parsing the XML document failed.
     * @throws IOException                  if reading the XML document failed.
     */
    public <T> Optional<T> parseOnce( XmlParser<? extends T> parser )
            throws IOException, SAXException, ParserConfigurationException
    {
        return resolver.parsed( path ) ? Optional.empty() : Optional.of( parse( parser ) );
    }

    /**
     * The path at which this document is located.
     *
     * @return the path at which this document is located.
     */
    public String path()
    {
        return canonicalize( path );
    }

    static String canonicalize( Path path )
    {
        return path.toAbsolutePath().normalize().toUri().toString();
    }
}
