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

import java.io.StringWriter;

import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.io.Output;

import static org.junit.Assert.assertEquals;
import static org.opencypher.grammar.Grammar.atLeast;
import static org.opencypher.grammar.Grammar.caseInsensitive;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.oneOf;
import static org.opencypher.grammar.Grammar.oneOrMore;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.repeat;
import static org.opencypher.grammar.Grammar.sequence;
import static org.opencypher.grammar.Grammar.zeroOrMore;
import static org.opencypher.tools.io.Output.lineNumbers;
import static org.opencypher.tools.io.Output.lines;
import static org.opencypher.tools.io.Output.stdOut;
import static org.opencypher.tools.io.Output.stringBuilder;

public class ISO14977Test
{
    @Test
    public void shouldGenerateCypher() throws Exception
    {
        Output.Readable output = stringBuilder();
        try
        {
            ISO14977.write( Fixture.grammarResource( ISO14977.class, "/cypher.xml" ), output );
        }
        catch ( Throwable e )
        {
            lineNumbers( stdOut() ).append( output );
            throw e;
        }
    }

    @Test
    public void shouldRenderLiteral() throws Exception
    {
        verify( production( "foo", literal( "FOO" ) ),
                "foo = 'FOO' ;" );
    }

    @Test
    public void shouldRenderCaseInsensitiveLiteral() throws Exception
    {
    	// to ensure reproducibility (via a set) the letters are sorted
        verify( production( "foo", caseInsensitive( "LIteR@L" ) ),
                "foo = L,I,T,E,R,'@',L ;",
                "",
                "E = 'E' | 'e' ;",
                "",
                "I = 'I' | 'i' ;",
                "",
                "L = 'L' | 'l' ;",
                "",
                "R = 'R' | 'r' ;",
                "",
                "T = 'T' | 't' ;");
    }

    @Test
    public void shouldRenderLiteralContainingMultipleQuotes() throws Exception
    {
        verify( production( "literals", literal( "'\"\"'" ) ),
                "literals = \"'\", '\"\"', \"'\" ;" );

        verify( production( "literals", oneOf( literal( "'\"\"'" ), literal( "\"''\"" ) ) ),
                "literals = (\"'\", '\"\"', \"'\")",
                "         | ('\"', \"''\", '\"')",
                "         ;" );
    }

    @Test
    public void shouldRenderAlternativesOfProduction() throws Exception
    {
        verify( production( "one", literal( "A" ), literal( "B" ) ),
                "one = 'A'",
                "    | 'B'",
                "    ;" );
    }

    @Test
    public void shouldRenderSequenceWithAlternatives() throws Exception
    {
        verify( production( "something", sequence(
                literal( "A" ),
                oneOf( literal( "B" ), literal( "C" ) ),
                literal( "D" ) ) ),
                "something = 'A', ('B' | 'C'), 'D' ;" );
    }

    @Test
    public void shouldRenderAlternativeSequences() throws Exception
    {
        verify( production( "alts", sequence( literal( "A" ), literal( "B" ) ),
                            sequence( literal( "C" ), literal( "D" ) ) ),
                "alts = ('A', 'B')",
                "     | ('C', 'D')",
                "     ;" );
    }

    @Test
    public void shouldRenderRepetitionWithExactCount() throws Exception
    {
        verify( production( "repeat", repeat( 6, literal( "hello" ) ) ),
                "repeat = 6 * 'hello' ;" );
    }

    @Test
    public void shouldRenderRepetitionWithMinAndMax() throws Exception
    {
        verify( production( "repeat", repeat( 5, 10, literal( "hello" ) ) ),
                "repeat = 5 * 'hello', 5 * [ 'hello' ] ;" );
    }

    @Test
    public void shouldRenderRepetitionWithMin() throws Exception
    {
        verify( production( "repeat", atLeast( 3, literal( "hello" ) ) ),
                "repeat = 3 * 'hello', { 'hello' } ;" );
    }

    @Test
    public void shouldRenderRepetitionWithMax() throws Exception
    {
        verify( production( "repeat", repeat( 0, 7, literal( "hello" ) ) ),
                "repeat = 7 * [ 'hello' ] ;" );
    }

    @Test
    public void shouldRenderOneOrMore() throws Exception
    {
        verify( production( "repeat", oneOrMore( literal( "hello" ) ) ),
                "repeat = { 'hello' }- ;" );
    }

    @Test
    public void shouldRenderZeroOrMore() throws Exception
    {
        verify( production( "repeat", zeroOrMore( literal( "hello" ) ) ),
                "repeat = { 'hello' } ;" );
    }

    @Test
    public void shouldRenderAlternativeRepetitions() throws Exception
    {
        verify( production( "stuff", zeroOrMore( literal( "foo" ), literal( "bar" ) ),
                            repeat( 5, literal( "abc" ), literal( "xyz" ) ) ),
                "stuff = { 'foo', 'bar' }",
                "      | 5 * ('abc', 'xyz')",
                "      ;" );
    }

    @Test
    public void shouldRenderOptional() throws Exception
    {
        verify( production( "opt", optional( literal( "foo" ) ) ),
                "opt = ['foo'] ;" );
    }

    @Test
    public void shouldRenderRecursiveDefinition() throws Exception
    {
        verify( production( "rec", sequence( literal( "A" ), optional( nonTerminal( "rec" ) ), literal( "B" ) ) ),
                "rec = 'A', [rec], 'B' ;" );
    }

    Grammar.Builder production( String name, Grammar.Term first, Grammar.Term... alternatives )
    {
        return Grammar.grammar( name ).production( name, first, alternatives );
    }

    static void verify( Grammar.Builder grammar, String... lines )
    {
        StringWriter writer = new StringWriter();
        ISO14977.write( grammar.build(), writer );
        assertEquals( lines( lines ).trim(), writer.toString().trim() );
    }
}
