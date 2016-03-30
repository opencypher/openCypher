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
package org.opencypher.railroad;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opencypher.grammar.Grammar;
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
import static org.opencypher.tools.output.Output.stringBuilder;

@RunWith(Parameterized.class)
public class DiagramTest
{
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> given()
    {
        return Arrays.asList(
                // 'FOO'
                givenProduction( "foo", literal( "FOO" ) )
                        .expectDiagram( "foo", text( "FOO" ) ),
                // 'BAR'
                givenProduction( "bar", caseInsensitive( "BAR" ) )
                        .expectDiagram( "bar", anyCase( "BAR" ) ),
                // expand "B,A,Z" to alternative chars
                givenProduction( "baz", caseInsensitive( "BAZ" ) )
                        .withExpandAnyCase( true )
                        .expectDiagram( "baz", line(
                                branch( text( "B" ), text( "b" ) ),
                                branch( text( "A" ), text( "a" ) ),
                                branch( text( "Z" ), text( "z" ) ) ) ),
                // item, {',', item}
                given( grammar( "list" )
                               .production( "list", sequence(
                                       nonTerminal( "item" ),
                                       zeroOrMore( literal( "," ), nonTerminal( "item" ) ) ) )
                               .production( "item", literal( "not interesting" ) ) )
                        .expectDiagram( "list", loop( reference( "item" ), text( "," ), 0, null ) ),
                // flatten "('BAR',), (|)" to "'BAR'"
                givenProduction( "bar", sequence( sequence(
                        oneOf( sequence( literal( "BAR" ), epsilon() ) ),
                        oneOf( epsilon(), epsilon() ) ) ) )
                        .expectDiagram( "bar", text( "BAR" ) ),
                // combine alternatives
                givenProduction( "alts", oneOf(
                        sequence( literal( "kill" ), literal( "all" ), literal( "animals" ) ),
                        sequence( literal( "kill" ), literal( "no" ), literal( "animals" ) ) ) )
                        .expectDiagram( "alts", line(
                                text( "kill" ),
                                branch( text( "all" ), text( "no" ) ),
                                text( "animals" ) ) ),
                // combine alternatives
                givenProduction( "alts", oneOf(
                        literal( "FOO" ),
                        sequence( literal( "FOO" ), literal( "BAR" ) ) ) )
                        .expectDiagram( "alts", line(
                                text( "FOO" ),
                                branch( nothing(), text( "BAR" ) ) ) ),
                // a, b, {',', a, b}
                givenProduction( "loop", sequence(
                        literal( "a" ), literal( "b" ),
                        zeroOrMore( literal( "," ), literal( "a" ), literal( "b" ) ) ) )
                        .expectDiagram( "loop", loop( line( text( "a" ), text( "b" ) ), text( "," ), 0, null ) )
        );
    }

    @Test
    public void buildDiagram() throws Exception
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

    static Given given( Grammar.Builder grammar )
    {
        return new Given( grammar.build() );
    }

    static Given givenProduction( String name, Grammar.Term first, Grammar.Term... alternatives )
    {
        return new Given( grammar( name ).production( name, first, alternatives ).build() );
    }

    private final Expectation expected;

    public DiagramTest( Expectation expected )
    {
        this.expected = expected;
    }

    private static class Given implements Diagram.BuilderOptions
    {
        private final Grammar grammar;
        private boolean expandAnyCase;

        Given( Grammar grammar )
        {
            this.grammar = grammar;
        }

        Object[] expectDiagram( String name, Diagram.Figure figure )
        {
            return new Object[]{new Expectation( this, Diagram.diagram( name, figure ) )};
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

    private static class Expectation
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
