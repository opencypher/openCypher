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
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public final class XmlFile
{
    private final Resolver resolver;
    private final Path path;

    XmlFile( Resolver resolver, Path path )
    {
        this.resolver = resolver;
        this.path = path;
    }

    public <T> T parse( XmlParser<T> parser )
            throws ParserConfigurationException, SAXException, IOException
    {
        return resolver.parse( path, parser );
    }

    public <T> Optional<T> parseOnce( XmlParser<? extends T> parser )
            throws IOException, SAXException, ParserConfigurationException
    {
        return resolver.parsed( path ) ? Optional.empty() : Optional.of( parse( parser ) );
    }

    public String path()
    {
        return canonicalize( path );
    }

    static String canonicalize( Path path )
    {
        return path.toAbsolutePath().normalize().toUri().toString();
    }
}
