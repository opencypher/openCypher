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

import java.util.List;

import org.junit.Test;
import org.opencypher.grammar.Grammar;
import org.opencypher.test.ParameterTest;
import org.opencypher.tools.grammar.ISO14977;

import static org.junit.Assert.assertEquals;
import static org.opencypher.grammar.Grammar.caseInsensitive;
import static org.opencypher.grammar.Grammar.epsilon;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.oneOf;
import static org.opencypher.grammar.Grammar.sequence;
import static org.opencypher.grammar.Grammar.zeroOrMore;
import static org.opencypher.railroad.Diagram.anyCase;
import static org.opencypher.railroad.Diagram.branch;
import static org.opencypher.railroad.Diagram.line;
import static org.opencypher.railroad.Diagram.loop;
import static org.opencypher.railroad.Diagram.nothing;
import static org.opencypher.railroad.Diagram.reference;
import static org.opencypher.railroad.Diagram.text;
import static org.opencypher.tools.io.Output.stringBuilder;

public class DiagramTest extends ParameterTest<DiagramTest.Expectation>
{
    @Test
    public static Expectation shouldText() throws Exception
    {
        return givenProduction( "foo", literal( "FOO" ) )
                .expectDiagram( "foo", text( "FOO" ) );
    }

    @Test
    public static Expectation shouldProduceAnyCase() throws Exception
    {
        return givenProduction( "bar", caseInsensitive( "BAR" ) )
                .expectDiagram( "bar", anyCase( "BAR" ) );
    }

    @Test
    public static Expectation shouldExpandAnyCase() throws Exception
    {
        return givenProduction( "baz", caseInsensitive( "BAZ" ) )
                .withExpandAnyCase( true )
                .expectDiagram( "baz", line(
                        branch( text( "B" ), text( "b" ) ),
                        branch( text( "A" ), text( "a" ) ),
                        branch( text( "Z" ), text( "z" ) ) ) );
    }

    @Test
    public static Expectation shouldProduceLoopWithSeparator() throws Exception
    {
        return given( grammar( "list" )
                              .production( "list", sequence(
                                      nonTerminal( "item" ),
                                      zeroOrMore( literal( "," ), nonTerminal( "item" ) ) ) )
                              .production( "item", literal( "not interesting" ) ) )
                .expectDiagram( "list", loop( reference( "item" ), text( "," ), 0, null ) );
    }

    @Test
    public static Expectation shouldEliminateRedundantNothing() throws Exception
    {
        return givenProduction( "bar", sequence( sequence(
                oneOf( sequence( literal( "BAR" ), epsilon() ) ),
                oneOf( epsilon(), epsilon() ) ) ) )
                .expectDiagram( "bar", text( "BAR" ) );
    }

    @Test
    public static Expectation shouldCombineAlternativeLines() throws Exception
    {
        return givenProduction( "alts", oneOf(
                sequence( literal( "kill" ), literal( "all" ), literal( "animals" ) ),
                sequence( literal( "kill" ), literal( "no" ), literal( "animals" ) ) ) )
                .expectDiagram( "alts", line(
                        text( "kill" ),
                        branch( text( "all" ), text( "no" ) ),
                        text( "animals" ) ) );
    }

    @Test
    public static Expectation shouldCombineAlternatives() throws Exception
    {
        return givenProduction( "alts", oneOf(
                literal( "FOO" ),
                sequence( literal( "FOO" ), literal( "BAR" ) ) ) )
                .expectDiagram( "alts", line(
                        text( "FOO" ),
                        branch( nothing(), text( "BAR" ) ) ) );
    }

    @Test
    public static Expectation shouldCombineLoopWithPrefix() throws Exception
    {
        return givenProduction( "loop", sequence(
                literal( "a" ), literal( "b" ),
                zeroOrMore( literal( "," ), literal( "a" ), literal( "b" ) ) ) )
                .expectDiagram( "loop", loop( line( text( "a" ), text( "b" ) ), text( "," ), 0, null ) );
    }

    @Override
    protected void run( Expectation expected )
    {
        // when
        List<Diagram> diagrams = Diagram.build( expected.given.grammar, expected.given );

        // then
        Diagram actual = null;
        for ( Diagram diagram : diagrams )
        {
            if ( diagram.name().equals( expected.given.grammar.language() ) )
            {
                actual = diagram;
                break;
            }
        }
        assertEquals( expected.diagram, actual );
    }

    private static Given given( Grammar.Builder grammar )
    {
        return new Given( grammar.build() );
    }

    private static Given givenProduction( String name, Grammar.Term first, Grammar.Term... alternatives )
    {
        return new Given( grammar( name ).production( name, first, alternatives ).build() );
    }

    private static class Given implements Diagram.BuilderOptions
    {
        private final Grammar grammar;
        private boolean expandAnyCase;

        Given( Grammar grammar )
        {
            this.grammar = grammar;
        }

        Expectation expectDiagram( String name, Diagram.Figure figure )
        {
            return new Expectation( this, Diagram.diagram( name, figure ) );
        }

        Given withExpandAnyCase( boolean expandAnyCase )
        {
            this.expandAnyCase = expandAnyCase;
            return this;
        }

        @Override
        public boolean expandAnyCase()
        {
            return expandAnyCase;
        }
    }

    static class Expectation
    {
        final Given given;
        final Diagram diagram;

        Expectation( Given given, Diagram diagram )
        {
            this.given = given;
            this.diagram = diagram;
        }

        @Override
        public String toString()
        {
            return given.grammar.transform( given.grammar.language(), ISO14977::string, stringBuilder() ).toString();
        }
    }
}
