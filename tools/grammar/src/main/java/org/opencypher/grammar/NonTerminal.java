package org.opencypher.grammar;

public interface NonTerminal
{
    Production production();

    default String productionName()
    {
        return production().name();
    }

    default Grammar.Term productionDefinition()
    {
        return production().definition();
    }

    default <Scope> Scope productionScope( Scope scope, ScopeRule.Transformation<Scope> transition )
    {
        return production().scope( scope, transition );
    }
}
