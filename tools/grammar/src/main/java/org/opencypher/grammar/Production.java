/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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

    <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P parameter ) throws EX;

    boolean skip();

    boolean inline();

    boolean legacy();

    boolean lexer();

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
