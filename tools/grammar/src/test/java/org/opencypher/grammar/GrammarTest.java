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
package org.opencypher.grammar;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;

public class GrammarTest
{
    public final @Rule Fixture fixture = new Fixture();

    @Test
    public void shouldParseGrammar() throws Exception
    {
        // when
        Grammar grammar = Grammar.parseXML( Paths.get( fixture.resource( "/somegrammar.xml" ).toURI() ),
                                            Grammar.ParserOption.FAIL_ON_UNKNOWN_XML_ATTRIBUTE );

        // then
        assertNotNull( grammar );
        assertEquals( "SomeLanguage", grammar.language() );
    }

    @Test
    public void shouldAccessReferencingProductions() throws Exception
    {
        // given
        Grammar grammar = grammar( "language" )
                .production( "language", nonTerminal( "one" ), nonTerminal( "two" ) )
                .production( "one", nonTerminal( "two" ) )
                .production( "two", literal( "y" ) )
                .build();

        // when
        Collection<Production> references = grammar.production( "two" ).referencedFrom();

        // then
        assertEquals( new HashSet<>( asList( "one", "language" ) ),
                      references.stream().map( Production::name ).collect( Collectors.toSet() ) );
    }
}
