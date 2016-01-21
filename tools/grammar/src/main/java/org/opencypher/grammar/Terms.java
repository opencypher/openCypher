package org.opencypher.grammar;

public interface Terms extends Iterable<Grammar.Term>
{
    int terms();

    Grammar.Term term( int offset );
}
