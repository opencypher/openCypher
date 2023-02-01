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
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static java.lang.invoke.MethodHandles.collectArguments;

abstract class Resolver
{
    private final Set<Path> parsedPaths;
    final EnumSet<XmlParser.Option> options;

    Resolver( XmlParser.Option[] options )
    {
        this( new HashSet<>(), options( options ) );
    }

    private static EnumSet<XmlParser.Option> options( XmlParser.Option[] options )
    {
        EnumSet<XmlParser.Option> result = EnumSet.noneOf( XmlParser.Option.class );
        if ( options != null )
        {
            Collections.addAll( result, options );
        }
        return result;
    }

    private Resolver( Set<Path> parsedPaths, EnumSet<XmlParser.Option> options )
    {
        this.parsedPaths = parsedPaths;
        this.options = options;
    }

    final XmlFile file( String path )
    {
        Path resolved = path( path );
        return new XmlFile( new Child( parsedPaths, resolved.getParent(), options ), resolved );
    }

    abstract Path path( String path );

    boolean parsed( Path path )
    {
        return parsedPaths.contains( path );
    }

    <T> T parse( Path path, XmlParser<T> parser )
            throws IOException, SAXException, ParserConfigurationException
    {
        parsedPaths.add( path );
        try ( InputStream stream = Files.newInputStream( path ) )
        {
            InputSource input = new InputSource( stream );
            input.setSystemId( XmlFile.canonicalize( path ) );
            return parser.parse( this, input, options );
        }
    }

    static void initialize( Initializer init )
    {
        init.add( Resolver::file );
        init.add( Resolver::path );
    }

    interface Initializer
    {
        void add( Class<?> type, Function<MethodHandle, MethodHandle> conversion );

        default <T> void add( Reference.BiFunction<Resolver, String, T> conversion )
        {
            MethodHandle mh = conversion.mh();
            add( mh.type().returnType(), conversion( mh ) );
        }
    }

    private static Function<MethodHandle, MethodHandle> conversion( MethodHandle filter )
    {
        return ( mh ) -> collectArguments( mh, 1, filter );
    }

    private static class Child extends Resolver
    {
        private final Path base;

        Child( Set<Path> parsedPaths, Path base, EnumSet<XmlParser.Option> options )
        {
            super( parsedPaths, options );
            this.base = base;
        }

        @Override
        Path path( String path )
        {
            return base.resolve( path );
        }
    }
}
