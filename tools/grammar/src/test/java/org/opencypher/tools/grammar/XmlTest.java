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

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.output.Output;

import static org.junit.Assert.assertEquals;
import static org.opencypher.grammar.GrammarVisitor.production;
import static org.opencypher.tools.output.Output.lineNumbers;
import static org.opencypher.tools.output.Output.stdOut;
import static org.opencypher.tools.output.Output.stringBuilder;

public class XmlTest
{
    public final @Rule Fixture fixture = new Fixture();

    @Test
    public void shouldProduceSameGrammarWhenParsingOutput() throws Exception
    {
        testRoundTrip( fixture.grammarResource( "/somegrammar.xml" ) );
    }

    @Test
    public void shouldGenerateCypher() throws Exception
    {
        // given
        Output.Readable out = stringBuilder();
        Grammar first = fixture.grammarResource( "/cypher.xml" );

        // when
        Xml.write( first, out );

        // then
        testRoundTrip( Grammar.parseXML( out.reader() ) );
    }

    private void testRoundTrip( Grammar grammar ) throws Exception
    {
        // given
        Output.Readable out = stringBuilder();

        // when
        Xml.write( grammar, out );

        // then
        try
        {
            Grammar second = Grammar.parseXML( out.reader() );
            assertGrammarEquals( grammar, second );
        }
        catch ( Throwable e )
        {
            lineNumbers( stdOut() ).append( out );
            throw e;
        }
    }

    static void assertGrammarEquals( Grammar expected, Grammar actual )
    {
        try
        {
            assertEquals( expected, actual );
        }
        catch ( Throwable e )
        {
            Map<String, Grammar.Term> before = new HashMap<>();
            expected.accept( production( before::put ) );
            actual.accept( production( ( name, def ) -> {
                try
                {
                    assertEquals( before.get( name ), def );
                }
                catch ( Throwable x )
                {
                    e.addSuppressed( x );
                }
            } ) );
            throw e;
        }
    }
}
