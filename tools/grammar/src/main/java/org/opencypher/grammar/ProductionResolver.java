package org.opencypher.grammar;

import java.util.Map;
import java.util.Set;

public class ProductionResolver
{
    private final Map<String, Production> productions;
    private final Dependencies dependencies;
    private final Set<String> unused;
    private int nonTerminalIndex;

    public ProductionResolver( Map<String, Production> productions, Dependencies dependencies, Set<String> unused )
    {
        this.productions = productions;
        this.dependencies = dependencies;
        this.unused = unused;
    }

    public void verifyCharacterSet( Production origin, String name )
    {
        if ( productions.get( name ) != null )
        {
            dependencies.invalidCharacterSet( name, origin );
        }
    }

    public Production resolveProduction( Production origin, String name )
    {
        Production production = productions.get( name );
        if ( production == null )
        {
            dependencies.missingProduction( name, origin );
        }
        else
        {
            unused.remove( name );
            dependencies.usedFrom( name, origin );
        }
        return production;
    }

    public int nextNonTerminalIndex()
    {
        return nonTerminalIndex++;
    }
}
