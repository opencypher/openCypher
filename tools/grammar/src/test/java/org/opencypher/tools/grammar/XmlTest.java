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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;

import static org.junit.Assert.assertEquals;
import static org.opencypher.grammar.GrammarVisitor.production;

public class XmlTest
{
    public final @Rule Fixture fixture = new Fixture();

    @Test
    public void shouldProduceSameGrammarWhenParsingOutput() throws Exception
    {
        // given
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Grammar first = fixture.grammarResource( "/somegrammar.xml" );

        // when
        Xml.write( first, out );

        // then
        Grammar second = Grammar.parseXML( new ByteArrayInputStream( out.toByteArray() ) );
        try
        {
            assertEquals( first, second );
        }
        catch ( Throwable e )
        {
            Map<String, Grammar.Term> before = new HashMap<>();
            first.accept( production( before::put ) );
            second.accept( production( ( name, def ) -> {
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
