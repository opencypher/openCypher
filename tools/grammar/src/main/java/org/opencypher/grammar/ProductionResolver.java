package org.opencypher.grammar;

import java.util.Map;
import java.util.Set;

class ProductionResolver
{
    private final Map<String, ProductionNode> productions;
    private final Dependencies dependencies;
    private final Set<String> unused;
    private int nonTerminalIndex;

    public ProductionResolver( Map<String, ProductionNode> productions, Dependencies dependencies, Set<String> unused )
    {
        this.productions = productions;
        this.dependencies = dependencies;
        this.unused = unused;
    }

    public void verifyCharacterSet( ProductionNode origin, String name )
    {
        if ( productions.get( name ) != null )
        {
            dependencies.invalidCharacterSet( name, origin );
        }
    }

    public ProductionNode resolveProduction( ProductionNode origin, String name )
    {
        ProductionNode production = productions.get( name );
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
