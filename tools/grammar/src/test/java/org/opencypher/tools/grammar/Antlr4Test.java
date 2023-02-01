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

import javax.xml.transform.TransformerException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.io.Output;

import static org.junit.Assert.assertThat;
import static org.opencypher.grammar.Grammar.caseInsensitive;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.zeroOrMore;
import static org.opencypher.tools.grammar.Antlr4ToolFacade.assertGeneratesValidParser;
import static org.opencypher.tools.io.Output.stringBuilder;

public class Antlr4Test
{
    @Test
    public void shouldGroupRepetitionCorrectly() throws TransformerException
    {
        assertGenerates(
                grammar( "foo" )
                        .production( "thing1", literal( "1" ) )
                        .production( "thing2", literal( "2" ) )
                        .production( "bar",
                                zeroOrMore( nonTerminal( "thing1" ), nonTerminal( "thing2" ) ) )
                        .build( Grammar.Builder.Option.ALLOW_ROOTLESS ),
                "grammar foo;",
                "",
                "oC_thing1",
                "      :  '1' ;",
                "",
                "oC_thing2",
                "      :  '2' ;",
                "",
                "oC_bar",
                "   :  ( oC_thing1 oC_thing2 )* ;",
                "" );
    }

    @Test
    public void shouldProduceLiteral() throws TransformerException
    {
        assertGenerates(
                grammar( "foo" )
                        .production( "bar", literal( "LIteR@L" ) )
                        .build( Grammar.Builder.Option.ALLOW_ROOTLESS ),
                "grammar foo;",
                "",
                "oC_bar",
                "   :  'LIteR@L' ;",
                "" );
    }

    @Test
    public void shouldNotMakeRulesForLiteralsWithoutCaseAlternatives() throws TransformerException
    {
        assertGenerates(
                grammar( "foo" )
                        .production( "other", literal( "abc" ) )
                        .production( "bar", nonTerminal( "other" ), caseInsensitive( "=>~`$&@" ), nonTerminal( "other" ) )
                        .build( Grammar.Builder.Option.ALLOW_ROOTLESS ),
                "grammar foo;",
                "",
                "oC_other",
                "     :  'abc' ;",
                "",
                "oC_bar",
                "   :  oC_other",
                "       | '=>~`$&@'",
                "       | oC_other",
                "       ;",
                "" );
    }

    @Test
    public void shouldProduceCaseInsensitive() throws TransformerException
    {
        assertGenerates(
                grammar( "foo" )
                        .production( "bar", caseInsensitive( "LIteR@L" ) )
                        .build( Grammar.Builder.Option.ALLOW_ROOTLESS ),
                "grammar foo;",
                "",
                "oC_bar",
                "   :  LITER@L ;",
                "",
                "LITER@L : ( 'L' | 'l' ) ( 'I' | 'i' ) ( 'T' | 't' ) ( 'E' | 'e' ) ( 'R' | 'r' ) '@' ( 'L' | 'l' ) ;",
                "" );
    }

    @Test
    public void shouldProduceSimpleParserGrammar() throws Exception
    {
        assertGenerates(
                grammar( "MyLanguage" )
                        .production( "MyLanguage", nonTerminal( "value" ) )
                        .production( "value", nonTerminal( "alpha" ), nonTerminal( "beta" ) )
                        .production( "alpha", literal( "a" ) )
                        .production( "beta", literal( "b" ) ),
                "grammar MyLanguage;",
                "",
                "oC_MyLanguage",
                "          :  oC_value ;",
                "",
                "oC_value",
                "     :  oC_alpha",
                "         | oC_beta",
                "         ;",
                "",
                "oC_alpha",
                "     :  'a' ;",
                "",
                "oC_beta",
                "    :  'b' ;",
                "" );
    }

    @Test
    public void shouldEncloseOptionalSequence() throws Exception
    {
        assertGenerates(
                grammar( "FooBar" )
                        .production( "FooBar", optional( literal( "foo" ), literal( "bar" ) ) ),
                "grammar FooBar;",
                "",
                "oC_FooBar",
                "      :  ( 'foo' 'bar' )? ;",
                "" );
    }

    @Test
    public void shouldGenerateCharacterSet() throws Exception
    {
        assertCharset( "White_Space", "[\\p{White_Space}]");
//                "[\\t\\n\\u000B\\f\\r \\u0085\\u00A0\\u1680" +
//                "\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005" +
//                "\\u2006\\u2007\\u2008\\u2009\\u200A" +
//                "\\u2028\\u2029\\u202F\\u205F\\u3000]" );
//        assertCharset( "White_Space",
//                       "[\\t\\n\\u000B\\f\\r \\u0085\\u00A0\\u1680" +
//                       "\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005" +
//                       "\\u2006\\u2007\\u2008\\u2009\\u200A" +
//                       "\\u2028\\u2029\\u202F\\u205F\\u3000]" );
    }

    @Test
    public void shouldGenerateCharacterSetWithExceptions() throws Exception
    {
        assertGenerates(
                grammar( "test" )
                        .production( "test", charactersOfSet( "Sc" ).except(
                                '\u058F', '\u060B', '\u09F2', '\u09F3', '\u09FB', '\u0AF1',
                                '\u0BF9', '\u0E3F', '\u17DB', '\uA838', '\uFDFC', '\uFE69',
                                '\uFF04', '\uFFE0', '\uFFE1', '\uFFE5', '\uFFE6' ) ),
                "grammar test;",
                "",
                "oC_test",
                "    :  TEST_0 ;",
                "",
                "fragment TEST_0 : ~[\\u058F\\u060B\\u09F2-\\u09F3\\u09FB\\u0AF1\\u0BF9\\u0E3F\\u17DB\\uA838\\uFDFC\\uFE69\\uFF04\\uFFE0-\\uFFE1\\uFFE5-\\uFFE6] ;",
                "" );
//        "grammar test;",
//        "",
//        "oC_test : TEST_0 ;",
//        "",
//        "fragment TEST_0 : [$\\u00A2-\\u00A5\\u20A0-\\u20BA] ;",
//        "" );
    }

    static void assertCharset( String name, String def )
    {
        assertGenerates(
                grammar( "test" )
                        .production( "test", charactersOfSet( name ) ),
                "grammar test;",
                "",
                "oC_test",
                "    :  " + name + " ;",
                "",
                "fragment " + name + " : " + def + " ;",
                "" );
    }

    @Test
    public void shouldGenerateTinyGrammar() throws Exception
    {
        assertGeneratesValidParser( "/FewRules.xml" );
    }
    
    @Test
    public void shouldGenerateCypherGrammar() throws Exception
    {
        assertGeneratesValidParser( "/cypher.xml" );
    }

    static void assertGenerates( Grammar.Builder grammar, String... lines )
    {
        assertGenerates( grammar.build(), lines );
    }

    static void assertGenerates( Grammar grammar, String... lines )
    {
        Output.Readable result = stringBuilder();
        Antlr4.write( grammar, result );
        String sres = result.toString();
        assertThat( result, contentsEquals( lines ) );
    }

    static Matcher<Output.Readable> contentsEquals( String... lines )
    {
        String expected = Output.lines( lines );
        return new TypeSafeMatcher<Output.Readable>()
        {
            @Override
            protected boolean matchesSafely( Output.Readable item )
            {
                return item.contentEquals( expected );
            }

            @Override
            public void describeTo( Description description )
            {
                description.appendValue( expected );
            }
        };
    }
}
