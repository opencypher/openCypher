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
