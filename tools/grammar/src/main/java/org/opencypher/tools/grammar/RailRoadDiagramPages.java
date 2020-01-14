/*
 * Copyright (c) 2015-2020 "Neo Technology,"
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.xml.stream.XMLStreamException;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.grammar.TermTransformation;
import org.opencypher.grammar.TermVisitor;
import org.opencypher.railroad.Diagram;
import org.opencypher.railroad.SVGShapes;
import org.opencypher.railroad.ShapeRenderer;
import org.opencypher.tools.io.HtmlTag;
import org.opencypher.tools.io.Output;

import static org.opencypher.tools.grammar.RailRoadDiagrams.canvas;
import static org.opencypher.tools.grammar.RailRoadDiagrams.renderer;
import static org.opencypher.tools.io.HtmlTag.html;
import static org.opencypher.tools.io.HtmlTag.meta;

public final class RailRoadDiagramPages extends Tool implements ShapeRenderer.Linker, ISO14977.HtmlLinker
{
    private final Diagram.BuilderOptions options;

    public static void main( String... args ) throws Exception
    {
        main( RailRoadDiagramPages::new, RailRoadDiagramPages::generate, args );
    }

    private RailRoadDiagramPages( Map<?, ?> properties )
    {
        super( properties );
        this.options = options( Diagram.BuilderOptions.class );
    }

    private void generate( Grammar grammar, Output output ) throws IOException, XMLStreamException
    {
        Path outputDir = outputDir();
        ShapeRenderer<XMLStreamException> renderer = renderer( this );
        Diagram.CanvasProvider<SVGShapes, XMLStreamException> canvas = canvas( output, outputDir );
        int diagrams = 0;
        for ( Diagram diagram : Diagram.build( grammar, options ) )
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
    public String referenceLink( NonTerminal reference )
    {
        if ( reference.inline() )
        {
            return "#" + reference.productionName();
        }
        Production production = reference.production();
        while ( options.shouldSkip( production ) )
        {
            production = production.transform( SIMPLE_DEFINITION, null );
        }
        return production == null ? null : referenceLink( production.name() );
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
            html.head( title -> production.name(), meta( "charset", "UTF-8" ) );
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
                body.tag( "h2" ).text( "EBNF" ).close();
                for ( NonTerminal nonTerminal : production.references() )
                {
                    Production site = nonTerminal.declaringProduction();
                    if ( site.skip() )
                    {
                        ISO14977.html( body, site, this );
                    }
                }
                ISO14977.html( body, production, this );
                production.definition().accept( new InlinedProductions()
                {
                    @Override
                    void inline( Production production )
                    {
                        body.tag( "a", name -> production.name() ).close();
                        ISO14977.html( body, production, RailRoadDiagramPages.this );
                    }
                } );
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

    private static abstract class InlinedProductions implements TermVisitor<RuntimeException>, Consumer<Grammar.Term>
    {
        abstract void inline( Production production );

        private final Set<String> inlined = new HashSet<>();

        @Override
        public void accept( Grammar.Term term )
        {
            term.accept( this );
        }

        @Override
        public void visitNonTerminal( NonTerminal nonTerminal )
        {
            if ( nonTerminal.inline() )
            {
                if ( inlined.add( nonTerminal.productionName() ) )
                {
                    inline( nonTerminal.production() );
                    nonTerminal.productionDefinition().accept( this );
                }
            }
        }

        @Override
        public void visitAlternatives( Alternatives alternatives )
        {
            alternatives.forEach( this );
        }

        @Override
        public void visitSequence( Sequence sequence )
        {
            sequence.forEach( this );
        }

        @Override
        public void visitOptional( Optional optional )
        {
            optional.term().accept( this );
        }

        @Override
        public void visitRepetition( Repetition repetition )
        {
            repetition.term().accept( this );
        }

        @Override
        public void visitLiteral( Literal literal )
        {
        }

        @Override
        public void visitEpsilon()
        {
        }

        @Override
        public void visitCharacters( CharacterSet characters )
        {
        }
    }

    private static final TermTransformation<Void,Production,RuntimeException> SIMPLE_DEFINITION =
            new TermTransformation<Void,Production,RuntimeException>()
            {
                @Override
                public Production transformNonTerminal( Void param, NonTerminal nonTerminal ) throws RuntimeException
                {
                    return nonTerminal.production();
                }

                @Override
                public Production transformOptional( Void param, Optional optional ) throws RuntimeException
                {
                    return optional.term().transform( this, param );
                }

                @Override
                public Production transformRepetition( Void param, Repetition repetition ) throws RuntimeException
                {
                    return repetition.term().transform( this, param );
                }

                @Override
                public Production transformAlternatives( Void param, Alternatives alternatives ) throws RuntimeException
                {
                    return null;
                }

                @Override
                public Production transformSequence( Void param, Sequence sequence ) throws RuntimeException
                {
                    return null;
                }

                @Override
                public Production transformLiteral( Void param, Literal literal ) throws RuntimeException
                {
                    return null;
                }

                @Override
                public Production transformEpsilon( Void param ) throws RuntimeException
                {
                    return null;
                }

                @Override
                public Production transformCharacters( Void param, CharacterSet characters ) throws RuntimeException
                {
                    return null;
                }
            };
}
