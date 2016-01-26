package org.opencypher.grammar;

public interface Alternatives extends Terms
{
    BiasedTerms eligibleForGeneration();
}
