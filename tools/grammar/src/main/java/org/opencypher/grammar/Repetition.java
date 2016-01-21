package org.opencypher.grammar;

public interface Repetition
{
    int minTimes();

    boolean limited();

    int maxTimes();

    Grammar.Term term();
}
