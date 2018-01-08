/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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

public interface TermTransformation<P, T, EX extends Exception>
{
    T transformAlternatives( P param, Alternatives alternatives ) throws EX;

    T transformSequence( P param, Sequence sequence ) throws EX;

    T transformLiteral( P param, Literal literal ) throws EX;

    T transformNonTerminal( P param, NonTerminal nonTerminal ) throws EX;

    T transformOptional( P param, Optional optional ) throws EX;

    T transformRepetition( P param, Repetition repetition ) throws EX;

    T transformEpsilon( P param ) throws EX;

    T transformCharacters( P param, CharacterSet characters ) throws EX;
}
