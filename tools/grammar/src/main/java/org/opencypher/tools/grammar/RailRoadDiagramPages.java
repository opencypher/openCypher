/*
 * Copyright (c) 2015-2022 "Neo Technology,"
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
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
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
import static org.opencypher.tools.io.HtmlTag.attr;
import static org.opencypher.tools.io.HtmlTag.head;
import static org.opencypher.tools.io.HtmlTag.html;
import static org.opencypher.tools.io.HtmlTag.meta;

public final class RailRoadDiagramPages extends Tool implements ShapeRenderer.Linker, HtmlLinker
{
    interface Options extends Diagram.BuilderOptions
    {
        default ProductionDetailsLinker productionDetailsLink()
        {
            return null;
        }

        default BnfFlavour bnfFlavour()
        {
            return ISO14977::html;
        }
    }

    public interface BnfFlavour
    {
        void bnf( HtmlTag parent, Production production, HtmlLinker linker );

        static BnfFlavour fromString( String value )
        {
            throw new UnsupportedOperationException( "not implemented" );
        }
    }

    public interface ProductionDetailsLinker
    {
        String productionDetailsLink( String production );

        static ProductionDetailsLinker fromString( String template )
        {
            MessageFormat format = new MessageFormat( template );
            return input -> format.format( new Object[]{input} );
        }
    }

    private final Options options;
    private final ProductionDetailsLinker detailsLinker;
    private final BnfFlavour bnfFlavour;

    public static void main( String... args ) throws Exception
    {
        main( RailRoadDiagramPages::new, RailRoadDiagramPages::generate, args );
    }

    public static void generate( Grammar grammar, Output output, Map<String,?> properties ) throws IOException, XMLStreamException
    {
        new RailRoadDiagramPages( properties ).generate( grammar, output );
    }

    private RailRoadDiagramPages( Map<?, ?> properties )
    {
        super( properties );
        this.options = options( Options.class );
        this.bnfFlavour = options.bnfFlavour();
        this.detailsLinker = options.productionDetailsLink();
    }

    @Override
    protected <T> T transform( Class<T> type, String value )
    {
        if ( type == BnfFlavour.class )
        {
            return type.cast( BnfFlavour.fromString( value ) );
        }
        else if ( type == ProductionDetailsLinker.class )
        {
            return type.cast( ProductionDetailsLinker.fromString( value ) );
        }
        else
        {
            return super.transform( type, value );
        }
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
        try ( Output backlinks = Output.output( outputDir.resolve( "backlinks.js" ) ) )
        {
            backlinks.println( "var backlinks = {" );
            grammar.accept( production ->
            {
                backlinks.append( "  " ).append( '"' ).append( production.name() ).append( '"' ).append( ": [" );
                for ( Production source : production.referencedFrom() )
                {
                    backlinks.append( '"' ).append( source.name() ).append( '"' ).append( ", " );
                }
                backlinks.println( "]," );
            } );
            backlinks.println( "};" );
        }
        try
        {
            Files.copy(
                    Path.of( getClass().getResource( "/explore-backlinks.js" ).toURI() ),
                    outputDir.resolve( "explore-backlinks.js" ),
                    StandardCopyOption.REPLACE_EXISTING );
        }
        catch ( NullPointerException | URISyntaxException e )
        {
            throw new IOException( "Failed to copy 'explore-backlinks.js'", e );
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
            html.head(
                    head( "title", production.name() ), meta( "charset", "UTF-8" ),
                    head( "script", null, attr( "src", "backlinks.js" ) ),
                    head( "script", null, attr( "src", "explore-backlinks.js" ) ) );
            try ( HtmlTag body = html.body() )
            {
                String detailsLink = null;
                if ( this.detailsLinker != null && (null != (detailsLink = this.detailsLinker.productionDetailsLink( production.name() ))) )
                {
                    try ( HtmlTag h1 = body.tag( "h1" ) )
                    {
                        h1.a( detailsLink, production.name() );
                    }
                }
                else
                {
                    body.textTag( "h1", production.name() );
                }
                body.tag( "object", attr( "data", svg ), attr( "type", "image/svg+xml" ) ).close();
                String description = production.description();
                if ( description != null )
                {
                    body.p();
                    body.text( description );
                }
                body.textTag( "h2", "EBNF" );
                bnf( body, production );
                for ( NonTerminal nonTerminal : production.references() )
                {
                    Production site = nonTerminal.declaringProduction();
                    if ( site.skip() )
                    {
                        bnf( body, production );
                    }
                }
                production.definition().accept( new InlinedProductions()
                {
                    @Override
                    void inline( Production production )
                    {
                        body.tag( "a", attr( "name", production.name() ) ).close();
                        bnf( body, production );
                    }
                } );
                Collection<Production> references = production.referencedFrom();
                if ( !references.isEmpty() )
                {
                    body.textTag( "h2", "Referenced from" );
                    try ( HtmlTag ul = body.tag( "ul" ) )
                    {
                        for ( Production reference : references )
                        {
                            try ( HtmlTag li = ul.tag( "li", attr( "class", "backlink" ), attr( "backlink", reference.name() ) ) )
                            {
                                String name = reference.name();
                                li.a( referenceLink( name ), name );
                            }
                        }
                    }
                }
            }
        }
    }

    private void bnf( HtmlTag body, Production production )
    {
        bnfFlavour.bnf( body, production, this );
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
