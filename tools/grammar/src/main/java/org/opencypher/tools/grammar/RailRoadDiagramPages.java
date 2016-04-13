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
package org.opencypher.tools.grammar;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Production;
import org.opencypher.railroad.Diagram;
import org.opencypher.railroad.SVGShapes;
import org.opencypher.railroad.ShapeRenderer;
import org.opencypher.tools.io.HtmlTag;
import org.opencypher.tools.io.Output;

import static org.opencypher.tools.grammar.RailRoadDiagrams.canvas;
import static org.opencypher.tools.grammar.RailRoadDiagrams.renderer;
import static org.opencypher.tools.io.HtmlTag.html;

public final class RailRoadDiagramPages extends Tool implements ShapeRenderer.Linker
{
    public static void main( String... args ) throws Exception
    {
        main( RailRoadDiagramPages::new, RailRoadDiagramPages::generate, args );
    }

    private RailRoadDiagramPages( Map<?, ?> properties )
    {
        super( properties );
    }

    private void generate( Grammar grammar, Output output ) throws IOException, XMLStreamException
    {
        Path outputDir = outputDir();
        ShapeRenderer<XMLStreamException> renderer = renderer( this );
        Diagram.CanvasProvider<SVGShapes, XMLStreamException> canvas = canvas( output, outputDir );
        int diagrams = 0;
        for ( Diagram diagram : Diagram.build( grammar, options( Diagram.BuilderOptions.class ) ) )
        {
            grammar.transform( diagram.name(), ( param, production ) -> {
                writeHtml( param, production );
                return null;
            }, outputDir );
            diagram.render( renderer, canvas );
            diagrams++;
        }
        output.append( "Rendered " ).append( diagrams ).println( " diagrams." );
    }

    @Override
    public String referenceLink( String reference )
    {
        return reference + ".html";
    }

    @Override
    public String charsetLink( String charset )
    {
        return RailRoadDiagrams.unicodesetLink( charset );
    }

    private void writeHtml( Path dir, Production production )
    {
        String svg = production.name() + ".svg";
        try ( HtmlTag.Html html = html( dir.resolve( production.name() + ".html" ) ) )
        {
            html.head( title -> production.name() );
            try ( HtmlTag body = html.body() )
            {
                body.tag( "h1" ).text( production.name() ).close();
                body.tag( "object", data -> svg, type -> "image/svg+xml" ).close();
                String description = production.description();
                if ( description != null )
                {
                    body.p();
                    body.text( description );
                }
                Collection<Production> references = production.referencedFrom();
                if ( !references.isEmpty() )
                {
                    body.tag( "h2" ).text( "Referenced from" ).close();
                    try ( HtmlTag ul = body.tag( "ul" ) )
                    {

                        for ( Production reference : references )
                        {
                            try ( HtmlTag li = ul.tag( "li" ) )
                            {
                                String name = reference.name();
                                li.tag( "a", href -> referenceLink( name ) ).text( name ).close();
                            }
                        }
                    }
                }
            }
        }
    }
}
