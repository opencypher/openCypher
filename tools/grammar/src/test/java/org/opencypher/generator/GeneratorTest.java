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

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;

import static java.lang.Character.charCount;

import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.opencypher.generator.GeneratorFixture.assertGenerates;
import static org.opencypher.generator.ChoicesFixture.onRepetition;
import static org.opencypher.generator.ProductionReplacement.replace;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.sequence;
import static org.opencypher.grammar.Grammar.zeroOrMore;
import static org.opencypher.tools.io.Output.output;
import static org.opencypher.tools.io.Output.stdOut;

public class GeneratorTest
{
    public final @Rule Fixture fixture = new Fixture();

    @Ignore
    @Test
    public void generateStuff() throws Exception
    {
        // given
        Grammar grammar = fixture.grammarResource( "/somegrammar.xml" );
        Generator generator = new Generator( grammar );

        // when
        Node tree = generator.generateTree( grammar.language() );

        // then
        tree.sExpression( stdOut() );
    }

    @Test
    public void shouldGenerateLiteral() throws Exception
    {
        assertGenerates(
                grammar( "foo" )
                        .production( "foo", literal( "Hello" ) ),
                "Hello" );
    }

    @Test
    public void shouldGenerateSequence() throws Exception
    {
        assertGenerates(
                grammar( "foo" )
                        .production( "foo", sequence(
                                literal( "Hello" ),
                                literal( "World" ) ) ),
                "HelloWorld" );
    }

    @Test
    public void shouldFollowNonTerminals() throws Exception
    {
        assertGenerates( grammar( "foo" )
                                 .production( "foo", sequence(
                                         nonTerminal( "hello" ),
                                         nonTerminal( "world" ) ) )
                                 .production( "hello", literal( "Hello" ) )
                                 .production( "world", literal( "World" ) ),
                         "HelloWorld" );
    }

    @Test
    public void shouldGenerateOptional() throws Exception
    {
        assertGenerates( grammar( "foo" )
                                 .production( "foo", sequence(
                                         nonTerminal( "hello" ),
                                         optional( nonTerminal( "world" ) ) ) )
                                 .production( "hello", literal( "Hello" ) )
                                 .production( "world", literal( "World" ) ),
                         x -> x.skipOptional().generates( "Hello" ),
                         x -> x.includeOptional().generates( "HelloWorld" ) );
    }

    @Test
    public void shouldGenerateAlternative() throws Exception
    {
        assertGenerates( grammar( "foo" )
                                 .production( "foo", literal( "Hello" ), literal( "World" ) ),
                         x -> x.picking( "Hello" )
                               .generates( "Hello" ),
                         x -> x.picking( "World" )
                               .generates( "World" ) );
    }

    @Test
    public void shouldGenerateRepetition() throws Exception
    {
        assertGenerates( grammar( "foo" )
                                 .production( "foo", zeroOrMore( literal( "w" ) ) ),
                         x -> x.repeat( 0, onRepetition( 0 ) ).generates( "" ),
                         x -> x.repeat( 3, onRepetition( 0 ) ).generates( "www" ) );
    }

    @Test
    public void shouldGenerateCharactersFromWellKnownSet() throws Exception
    {
        assertCharacterSet( "NUL", "\0" );
        assertCharacterSet( "TAB", "\t" );
        assertCharacterSet( "LF", "\n" );
        assertCharacterSet( "CR", "\r" );
        assertCharacterSet( "FF", "\f" );
    }

    @Test
    public void shouldReplaceProductions() throws Exception
    {
        // when
        String generated = generate( grammar( "foo" )
                                             .production( "foo", nonTerminal( "bar" ) )
                                             .production( "bar", literal( "WRONG!" ) ),
                                     replace( "bar", bar -> bar.write( "OK" ) ) );

        // then
        assertEquals( "OK", generated );
    }

    @Test
    public void shouldAllowContextSensitiveReplacements() throws Exception
    {
        assertEquals( "one - two",
                      generate( grammar( "lang" ).production( "lang", sequence(
                              nonTerminal( "alpha" ), literal( " - " ), nonTerminal( "beta" ) ) )
                                                 .production( "alpha", nonTerminal( "symbol" ) )
                                                 .production( "beta", nonTerminal( "symbol" ) )
                                                 .production( "symbol", literal( "<NOT REPLACED>" ) ),
                                replace("symbol", symbol -> {
                                    switch ( symbol.node().parent().name() )
                                    {
                                    case "alpha":
                                        symbol.write( "one" );
                                        break;
                                    case "beta":
                                        symbol.write( "two" );
                                        break;
                                    default:
                                        symbol.generateDefault();
                                        break;
                                    }
                                } ) ) );
    }

    private void assertCharacterSet( String name, String characters )
    {
        Grammar grammar = grammar( "foo" ).production( "foo", charactersOfSet( name ) ).build();
        StringBuilder expected = new StringBuilder();
        Set<Integer> codepoints = new HashSet<>();
        for ( int i = 0, cp; i < characters.length(); i += charCount( cp ) )
        {
            cp = characters.codePointAt( i );
            expected.setLength( 0 );
            expected.appendCodePoint( cp );
            int codepoint = cp;
            assertGenerates( grammar, x -> x.picking( codepoint ).generates( expected.toString() ) );
            codepoints.add( cp );
        }
        for ( int i = codepoints.size() * 10; i-- > 0; )
        {
            assertThat( generate( grammar ).codePointAt( 0 ), isIn( codepoints ) );
        }
    }

    @SafeVarargs
    static String generate( Grammar.Builder grammar, ProductionReplacement<Void>... replacements )
    {
        return generate( grammar.build(), replacements );
    }

    @SafeVarargs
    static String generate( Grammar grammar, ProductionReplacement<Void>... replacements )
    {
        StringBuilder result = new StringBuilder();
        new Generator( grammar, replacements ).generate( output( result ) );
        return result.toString();
    }
}
