package org.opencypher.grammar;

public interface BiasedTerms extends Terms
{
    double bound();

    Grammar.Term term( double bias );
}
