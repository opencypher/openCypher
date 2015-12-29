package org.opencypher.grammar;

import java.util.Collection;
import java.util.List;

public interface GrammarVisitor<EX extends Exception>
{
    void visitProduction( String production, Grammar.Term definition ) throws EX;

    default void visitAlternatives( Collection<Grammar.Term> alternatives ) throws EX
    {
    }

    default void visitSequence( Collection<Grammar.Term> sequence ) throws EX
    {
    }

    default void visitLiteral( String value ) throws EX
    {
    }

    default void visitNonTerminal( String productionName, Grammar.Term productionDef ) throws EX
    {
    }

    default void visitOptional( Grammar.Term term ) throws EX
    {
    }

    default void visitRepetition( int min, Integer max, Grammar.Term term ) throws EX
    {
    }

    default void visitEpsilon() throws EX
    {
    }

    default void visitCharacters( String wellKnownSetName, List<Exclusion> exclusions ) throws EX
    {
    }
}
