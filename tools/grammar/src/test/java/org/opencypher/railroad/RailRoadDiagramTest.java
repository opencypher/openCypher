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
package org.opencypher.railroad;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Rule;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.Interactive;
import org.opencypher.tools.Option;
import org.w3c.dom.Document;

import static org.opencypher.grammar.Grammar.caseInsensitive;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.epsilon;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.oneOf;
import static org.opencypher.grammar.Grammar.oneOrMore;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.repeat;
import static org.opencypher.grammar.Grammar.sequence;
import static org.opencypher.grammar.Grammar.zeroOrMore;
import static org.opencypher.railroad.RailRoadViewer.frame;
import static org.opencypher.tools.Option.options;

public class RailRoadDiagramTest
{
    public final @Rule Fixture fixture = new Fixture();
    public final @Rule Interactive<RailRoadViewer.Context, Map<String, Document>> interactive =
            new Interactive<>( new RailRoadViewer() );

    @Test
    public void shouldGenerateLiteral() throws Exception
    {
        generate( literal( "FOO" ) );
    }

    @Test
    public void shouldGenerateNonTerminal() throws Exception
    {
        generate( grammar( "bar" )
                          .production( "bar", nonTerminal( "foo" ) )
                          .production( "foo", literal( "FOO" ) ) );
    }

    @Test
    public void shouldDrawSequence() throws Exception
    {
        generate( sequence( literal( "one" ), literal( "two" ), literal( "three" ) ) );
    }

    @Test
    public void shouldDrawAlt() throws Exception
    {
        generate( oneOf( literal( "short" ), literal( "v" ), literal( "something long" ) ) );
    }

    @Test
    public void shouldDrawLoop() throws Exception
    {
        generate( loop( literal( "item" ), literal( "," ) ) );
    }

    @Test
    public void loopWithAlt() throws Exception
    {
        generate( loop(
                oneOf( literal( "one" ), literal( "two" ), literal( "three" ) ),
                oneOf( literal( "," ), literal( ";" ) ) ) );
    }

    @Test
    public void shouldDrawLoopWithNothing() throws Exception
    {
        generate( grammar( "loops" )
                          .production( "loops", oneOf(
                                  nonTerminal( "oneOrMore" ),
                                  nonTerminal( "zeroOrMore" ) ) )
                          .production( "zeroOrMore", zeroOrMore( literal( "hej" ) ) )
                          .production( "oneOrMore", oneOrMore( literal( "hej" ) ) ) );
    }

    @Test
    public void shouldDrawAltWithNothing() throws Exception
    {
        generate( grammar( "choices" )
                          .production( "choices", oneOf(
                                  nonTerminal( "something-or-nothing" ),
                                  nonTerminal( "nothing-or-something" ),
                                  nonTerminal( "optional-something" ) ) )
                          .production( "something-or-nothing", oneOf( literal( "hello" ), epsilon() ) )
                          .production( "nothing-or-something", oneOf( epsilon(), literal( "hello" ) ) )
                          .production( "optional-something", oneOf( optional( literal( "hello" ) ) ) ) );
    }

    @Test
    public void shouldDrawAltWithLoop() throws Exception
    {
        generate( oneOf(
                literal( "one" ),
                zeroOrMore( literal( "fish" ) ),
                literal( "two" ) ) );
    }

    @Test
    public void allNodes() throws Exception
    {
        generate( grammar( "nodes" ).production(
                "nodes",
                literal( "literal" ),
                charactersOfSet( "ID_Start" ),
                caseInsensitive( "any case" ),
                nonTerminal( "nodes" ) ) );
    }

    @Test
    public void drawLoopWithBounds() throws Exception
    {
        generate( repeat( 5, 10, literal( "hello" ) ) );
    }

    @Test
    public void drawLoopWithLoop() throws Exception
    {
        generate( loop( literal( "hello" ), loop( literal( "one" ), literal( "two" ) ) ) );
    }

    @Test
    public void shouldDrawCharset() throws Exception
    {
        generate( charactersOfSet( "ID_Start" ) );
    }

    @Test
    public void shouldDrawCypher() throws Exception
    {
        generate( fixture.grammarResource( "/cypher.xml" ) );
    }

    @Test
    public void shouldDrawFoo() throws Exception
    {
        generate( fixture.grammarResource( "/foo.xml" ) );
    }

    @Test
    public void cypherWhitespace() throws Exception
    {
        showCypher( "whitespace" );
    }

    private void showCypher( String production ) throws Exception
    {
        if ( interactive.mode() == Interactive.Mode.METHOD )
        {
            CountDownLatch latch = new CountDownLatch( 1 );
            Diagram diagram = Diagram.build(
                    fixture.grammarResource( "/cypher.xml" ).production( production ),
                    options( Diagram.BuilderOptions.class ) );
            ShapeRenderer<Exception> renderer = new ShapeRenderer<>();
            frame( latch, "DOM: " + production, diagram.convert( renderer, SVGShapes.SVG_DOM ) )
                    .setVisible( true );
            frame( latch, "AWT: " + production, diagram.convert( renderer, AwtShapes.AWT, ( AwtShapes awt ) -> awt ) )
                    .setVisible( true );
            latch.await();
        }
    }

    private static Grammar.Term loop( Grammar.Term component, Grammar.Term delimiter )
    {
        return sequence( component, zeroOrMore( delimiter, component ) );
    }

    private final Map<String, Document> generate( Grammar.Term grammar ) throws Exception
    {
        return generate( grammar( fixture.testName() ).production( fixture.testName(), grammar ) );
    }

    private final Map<String, Document> generate( Grammar.Builder grammar) throws Exception
    {
        return generate( grammar.build() );
    }

    private final Map<String, Document> generate( Grammar grammar ) throws Exception
    {
        return interactive.test( new RailRoadViewer.Context( grammar ) );
    }
}
