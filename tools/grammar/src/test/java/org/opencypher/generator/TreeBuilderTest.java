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
package org.opencypher.generator;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencypher.grammar.Grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.opencypher.generator.Node.root;
import static org.opencypher.generator.ChoicesFixture.onRepetition;
import static org.opencypher.generator.TreeBuilder.state;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.epsilon;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.oneOf;
import static org.opencypher.grammar.Grammar.oneOrMore;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.sequence;

public class TreeBuilderTest
{
    public final @Rule TestName testName = new TestName();
    private final ChoicesFixture random = new ChoicesFixture();
    private final Map<String, ProductionReplacement<Void>> replacements = new HashMap<>();
    private final TreeBuilder<Void> builder = new TreeBuilder<>( random.random(), () -> null, replacements );
    private Node.Tree actual;

    @Test
    public void shouldTransformLiteral() throws Exception
    {
        // when
        TreeBuilder.State<Void> next = generate( literal( "hello" ) );

        // then
        Node.Tree expected = tree();
        expected.literal( "hello" );
        assertEquals( expected, actual );
        assertNull( next );
    }

    @Test
    public void shouldTransformEpsilon() throws Exception
    {
        // when
        TreeBuilder.State<Void> next = generate( epsilon() );

        // then
        assertEquals( tree(), actual );
        assertNull( next );
    }

    @Test
    public void shouldTransformSequence() throws Exception
    {
        // when
        TreeBuilder.State<Void> next = generate( sequence( literal( "a" ), literal( "b" ), literal( "c" ) ) );

        // then
        Node.Tree expected = tree();
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "a" );
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "b" );
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "c" );
        assertEquals( expected, actual );
        assertNull( next );
    }

    @Test
    public void shouldTransformAlternatives() throws Exception
    {
        // given
        random.pick( "c" );

        // when
        TreeBuilder.State<Void> next = generate( oneOf( literal( "a" ), literal( "b" ), literal( "c" ) ) );

        // then
        Node.Tree expected = tree();
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "c" );
        assertEquals( expected, actual );
        assertNull( next );
    }

    @Test
    public void shouldTransformIncludedOptional() throws Exception
    {
        // given
        random.includeOptional();

        // when
        TreeBuilder.State<Void> next = generate( optional( literal( "ok" ) ) );

        // then
        Node.Tree expected = tree();
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "ok" );
        assertEquals( expected, actual );
        assertNull( next );
    }

    @Test
    public void shouldTransformExcludedOptional() throws Exception
    {
        // given
        random.excludeOptional();

        // when
        TreeBuilder.State<Void> next = generate( optional( literal( "not" ) ) );

        // then
        Node.Tree expected = tree();
        assertEquals( expected, actual );
        assertNull( next );
    }

    @Test
    public void shouldTransformRepetition() throws Exception
    {
        // given
        random.repeat( 3, onRepetition( 1 ) );

        // when
        TreeBuilder.State<Void> next = generate( oneOrMore( literal( "hej" ) ) );

        // then
        Node.Tree expected = tree();
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "hej" );
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "hej" );
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        expected.literal( "hej" );
        assertEquals( expected, actual );
        assertNull( next );
    }

    @Test
    public void shouldTransformCharacterSet() throws Exception
    {
        // given
        class CharSet
        {
            final String name;
            final String expected;

            CharSet( String name, int c )
            {
                this.name = name;
                this.expected = "" + (char) c;
            }
        }
        // <pre>
        for ( CharSet charset : new CharSet[]{
                new CharSet( "NUL",   0x00 ),
                new CharSet( "SOH",   0x01 ),
                new CharSet( "STX",   0x02 ),
                new CharSet( "ETX",   0x03 ),
                new CharSet( "EOT",   0x04 ),
                new CharSet( "ENQ",   0x05 ),
                new CharSet( "ACK",   0x06 ),
                new CharSet( "BEL",   0x07 ),
                new CharSet( "BS",    0x08 ),
                new CharSet( "TAB",   0x09 ),
                new CharSet( "LF",    0x0A ),
                new CharSet( "VT",    0x0B ),
                new CharSet( "FF",    0x0C ),
                new CharSet( "CR",    0x0D ),
                new CharSet( "SO",    0x0E ),
                new CharSet( "SI",    0x0F ),
                new CharSet( "DLE",   0x10 ),
                new CharSet( "DC1",   0x11 ),
                new CharSet( "DC2",   0x12 ),
                new CharSet( "DC3",   0x13 ),
                new CharSet( "DC4",   0x14 ),
                new CharSet( "NAK",   0x15 ),
                new CharSet( "SYN",   0x16 ),
                new CharSet( "ETB",   0x17 ),
                new CharSet( "CAN",   0x18 ),
                new CharSet( "EM",    0x19 ),
                new CharSet( "SUB",   0x1A ),
                new CharSet( "ESC",   0x1B ),
                new CharSet( "FS",    0x1C ),
                new CharSet( "GS",    0x1D ),
                new CharSet( "RS",    0x1E ),
                new CharSet( "US",    0x1F ),
                new CharSet( "SPACE", 0x20 ),
                new CharSet( "DEL",   0x7F ), } )
        //</pre>
        {
            // when
            TreeBuilder.State next = generate( charactersOfSet( charset.name ) );

            // then
            Node.Tree expected = tree();
            expected.literal( charset.expected );
            assertEquals( charset.name, expected, actual );
            assertNull( next );
        }
    }

    @Test
    public void shouldTransformNonTerminal() throws Exception
    {
        // given - resolved non-terminal
        Grammar.Term nonTerminal = grammar( testName.getMethodName() )
                .production( testName.getMethodName(), nonTerminal( "other" ) )
                .production( "other", literal( "done" ) )
                .build().transform( testName.getMethodName(), ( param, prod ) -> prod.definition(), null );

        // when
        TreeBuilder.State<Void> next = generate( nonTerminal );

        // then
        Node.Tree expected = tree();
        Node.Tree other = expected.child( "other" );
        assertEquals( expected, actual );
        assertNotNull( next );

        // when
        next = generate( next );

        // then
        other.literal( "done" );
        assertEquals( expected, actual );
        assertNull( next );
    }

    private TreeBuilder.State<Void> generate( Grammar.Term term )
    {
        return generate( state( term, actual = tree(), null ) );
    }

    private TreeBuilder.State<Void> generate( TreeBuilder.State<Void> state )
    {
        return state.generate( builder );
    }

    private Node.Tree tree()
    {
        return root( testName.getMethodName() );
    }
}
