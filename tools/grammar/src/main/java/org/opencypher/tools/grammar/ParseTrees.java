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
package org.opencypher.tools.grammar;

import org.opencypher.grammar.Grammar;
import org.opencypher.tools.io.Output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class ParseTrees extends Tool
{
    interface Options
    {
        default Path sources()
        {
            return null;
        }

        default Parser.Generator parserGenerator()
        {
            return Antlr4::generateParser;
        }

        default String rootProduction()
        {
            return null;
        }
    }

    public static void main( String... args ) throws Exception
    {
        main( ParseTrees::new, ParseTrees::generate, args );
    }

    private final Options options;

    ParseTrees( Path workingDir, Map<?,?> properties )
    {
        super( workingDir, properties );
        this.options = options( Options.class );
    }

    public void generate( Grammar grammar, Output output ) throws IOException
    {
        Parser parser = options.parserGenerator().generateParser( grammar, rootProductionRule( grammar ), output );
        Path sources = options.sources();
        if ( sources != null )
        {
            Path destination = outputDir();
            if ( Files.isDirectory( sources ) )
            {
                Files.walk( sources ).filter( Files::isRegularFile ).forEach( path -> generate( parser, path, destination, output ) );
            }
            else if ( Files.isRegularFile( sources ) )
            {
                generate( parser, sources, destination, output );
            }
            else
            {
                output.format( "ParseTree Source does not exist: %s,%n", sources );
            }
        }
        else
        {
            output.println( "No 'source' specified for ParseTree." );
        }
    }

    private String rootProductionRule( Grammar grammar )
    {
        String root = options.rootProduction();
        if ( root == null )
        {
            String language = grammar.language();
            if ( grammar.hasProduction( language ) )
            {
                return language;
            }
            throw new IllegalStateException( "No root production rule specified for " + language );
        }
        return root;
    }

    private void generate( Parser parser, Path source, Path destination, Output log )
    {
        String filename = source.getFileName().toString();
        int dot = filename.lastIndexOf( '.' );
        switch ( dot < 0 ? "" : filename.substring( dot ).toLowerCase() )
        {
        case ".cypher":
            try
            {
                Parser.ParseTree tree = parser.parse( Files.readString( source ) );
                if ( false )
                {
                    System.out.println( tree );
                }
            }
            catch ( IOException e )
            {
                log.format( "IOException in reading '%s': %s%n", source, e.getMessage() );
            }
            break;
        case ".feature":
//            new GherkinVintageFeatureParser();
            //break;
        default:
            log.format( "Cannot determine type of file: %s%n", source );
        }
    }

    @Override
    protected <T> T transform( Class<T> type, String value )
    {
        if ( type == Parser.Generator.class )
        {
            return type.cast( Parser.generator( value ) );
        }
        return super.transform( type, value );
    }
}
