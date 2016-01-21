package org.opencypher.grammar;

public interface ProductionTransformation<P, R, EX extends Exception>
{
    R transformProduction( P param, Production production ) throws EX;
}
