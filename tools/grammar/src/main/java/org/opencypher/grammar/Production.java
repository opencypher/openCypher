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

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public interface Production
{
    String name();

    String description();

    Grammar.Term definition();

    <Scope> Scope scope( Scope scope, ScopeRule.Transformation<Scope> transition );

    <P, T, EX extends Exception> T transform( TermTransformation<P,T,EX> transformation, P parameter ) throws EX;

    default boolean isEmpty()
    {
        Grammar.Term def = definition();
        if ( def instanceof Node )
        {
            Node node = (Node) def;
            return node.isEpsilon();
        }
        else
        {
            return def.transform( new TermTransformation<Void,Boolean,RuntimeException>()
            {   // <pre>
                @Override public Boolean transformEpsilon( Void param ) { return true; }
                @Override public Boolean transformAlternatives( Void param, Alternatives alternatives ) { return false; }
                @Override public Boolean transformSequence( Void param, Sequence sequence ) { return false; }
                @Override public Boolean transformLiteral( Void param, Literal literal ) { return false; }
                @Override public Boolean transformNonTerminal( Void param, NonTerminal nonTerminal ) { return false; }
                @Override public Boolean transformOptional( Void param, Optional optional ) { return false; }
                @Override public Boolean transformRepetition( Void param, Repetition repetition ) { return false; }
                @Override public Boolean transformCharacters( Void param, CharacterSet characters ) { return false; }
                // </pre>
            }, null );
        }
    }

    boolean skip();

    boolean inline();

    boolean legacy();

    boolean lexer();

    boolean bnfsymbols();

    Collection<NonTerminal> references();

    default Collection<Production> referencedFrom()
    {
        return references()
                .stream()
                .flatMap( nonTerminal -> {
                    Production site = nonTerminal.declaringProduction();
                    if ( nonTerminal.skip() || site.skip() )
                    {
                        return Stream.empty();
                    }
                    else if ( site.inline() )
                    {
                        return site.referencedFrom().stream();
                    }
                    else
                    {
                        return Stream.concat(
                                Stream.of( site ),
                                site.references().stream()
                                    .filter( NonTerminal::inline )
                                    .map( NonTerminal::declaringProduction ) );
                    }
                } )
                .collect( toSet() );
    }
}
