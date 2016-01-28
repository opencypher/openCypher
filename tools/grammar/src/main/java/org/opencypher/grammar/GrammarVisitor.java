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
package org.opencypher.grammar;

import java.util.function.BiConsumer;

public interface GrammarVisitor<EX extends Exception>
{
    static GrammarVisitor<RuntimeException> production( BiConsumer<String,Grammar.Term> consumer )
    {
        return production -> consumer.accept( production.name(), production.definition() );
    }

    void visitProduction( Production production ) throws EX;

    default void visitAlternatives( Alternatives alternatives ) throws EX
    {
        each( alternatives, this );
    }

    default void visitSequence( Sequence sequence ) throws EX
    {
        each( sequence, this );
    }

    default void visitLiteral( Literal literal ) throws EX
    {
    }

    default void visitNonTerminal( NonTerminal nonTerminal ) throws EX
    {
    }

    default void visitOptional( Optional optional ) throws EX
    {
        optional.term().accept( this );
    }

    default void visitRepetition( Repetition repetition ) throws EX
    {
        repetition.term().accept( this );
    }

    default void visitEpsilon() throws EX
    {
    }

    default void visitCharacters( CharacterSet characters ) throws EX
    {
    }

    static <EX extends Exception> void each( Iterable<Grammar.Term> terms, GrammarVisitor<EX> visitor ) throws EX
    {
        for ( Grammar.Term term : terms )
        {
            term.accept( visitor );
        }
    }
}
