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
