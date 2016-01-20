package org.opencypher.grammar;

import java.util.Collection;
import java.util.List;

public interface TermTransformation<P, T, EX extends Exception>
{
    T transformAlternatives( P param, Collection<Grammar.Term> alternatives ) throws EX;

    T transformSequence( P param, Collection<Grammar.Term> sequence ) throws EX;

    T transformLiteral( P param, String value ) throws EX;

    T transformNonTerminal( P param, String productionName, Grammar.Term productionDef ) throws EX;

    T transformOptional( P param, Grammar.Term term ) throws EX;

    T transformRepetition( P param, int min, Integer max, Grammar.Term term ) throws EX;

    T transformEpsilon( P param ) throws EX;

    T transformCharacters( P param, String wellKnownSetName, List<Exclusion> exclusions ) throws EX;
}
