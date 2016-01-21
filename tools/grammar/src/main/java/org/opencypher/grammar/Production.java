package org.opencypher.grammar;

public interface Production
{
    String name();

    String description();

    Grammar.Term definition();

    <Scope> Scope scope( Scope scope, ScopeRule.Transformation<Scope> transition );
}
