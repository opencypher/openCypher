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

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import org.opencypher.grammar.Grammar;
import org.opencypher.railroad.Diagram;
import org.opencypher.railroad.SVGShapes;
import org.opencypher.railroad.ShapeRenderer;
import org.opencypher.tools.io.Output;

import static org.opencypher.railroad.SVGShapes.svgFile;
import static org.opencypher.tools.io.Output.output;
import static org.opencypher.tools.io.Output.stringBuilder;

/**
 * Generates railroad diagrams (as SVG files) for each of the productions in a {@link Grammar}.
 */
public final class RailRoadDiagrams extends Tool implements ShapeRenderer.Linker
{
    public static void main( String... args ) throws Exception
    {
        main( RailRoadDiagrams::new, RailRoadDiagrams::generate, args );
    }

    private RailRoadDiagrams( Path workingDir, Map<?, ?> properties )
    {
        super( workingDir, properties );
    }

    private void generate( Grammar grammar, Output output ) throws XMLStreamException, IOException
    {
        ShapeRenderer<XMLStreamException> renderer = renderer( this );
        Diagram.CanvasProvider<SVGShapes, XMLStreamException> canvas = canvas( output, outputDir() );
        for ( Diagram diagram : Diagram.build( grammar, options( Diagram.BuilderOptions.class ) ) )
        {
            diagram.render( renderer, canvas );
        }
    }

    @Override
    public String referenceLink( String reference )
    {
        return reference + ".svg";
    }

    @Override
    public String charsetLink( String charset )
    {
        return unicodesetLink( charset );
    }

    static <T extends Tool & ShapeRenderer.Linker, EX extends Exception> ShapeRenderer<EX> renderer( T tool )
    {
        FontRenderContext fonts = new FontRenderContext( new AffineTransform(), true, true );
        return new ShapeRenderer<>( tool, fonts, tool.options( ShapeRenderer.Options.class ) );
    }

    static Diagram.CanvasProvider<SVGShapes, XMLStreamException> canvas( Output log, Path dir )
    {
        return svgFile( name -> {
            String filename = name.replace( '/', ' ' );
            Path file = dir.resolve( filename + ".svg" ).toAbsolutePath();
            log.format( "Writing Railroad diagram for %s to %s%n", name, file );
            return output( file );
        } );
    }

    static String unicodesetLink( String charset )
    {
        return stringBuilder().append( "http://unicode.org/cldr/utility/list-unicodeset.jsp?abb=on&esc=on&a=" )
                              .escape( charset, c -> {/*<pre>*/switch ( c ) {
                                  case ':': return "%3A";
                                  case '[': return "%5B";
                                  case '\\':return "%5C";
                                  case ']': return "%5D";
                                  case '^': return "%5E";
                              default: return null;}}/*</pre>*/ ).toString();
    }
}
