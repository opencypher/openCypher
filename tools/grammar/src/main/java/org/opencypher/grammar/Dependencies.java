package org.opencypher.grammar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class Dependencies
{
    private Map<String, Set<Production>> missingProductions;
    private final Map<String, Set<Production>> dependencies = new LinkedHashMap<>();

    public void missingProduction( String name, Production origin )
    {
        if ( missingProductions == null )
        {
            missingProductions = new LinkedHashMap<>();
        }
        update( missingProductions, name, origin );
    }

    public void usedFrom( String name, Production origin )
    {
        update( dependencies, name, origin );
    }

    private static void update( Map<String, Set<Production>> productions, String name, Production origin )
    {
        Set<Production> origins = productions.get( name );
        if ( origins == null )
        {
            productions.put( name, origins = Collections.newSetFromMap( new LinkedHashMap<>() ) );
        }
        origins.add( origin );
    }

    public void reportMissingProductions()
    {
        if ( missingProductions != null && !missingProductions.isEmpty() )
        {
            StringBuilder message = new StringBuilder()
                    .append( "Productions used in non-terminals have not been defined:" );
            for ( Map.Entry<String, Set<Production>> entry : missingProductions.entrySet() )
            {
                message.append( "\n  " ).append( entry.getKey() );
                String sep = " used from: ";
                for ( Production origin : entry.getValue() )
                {
                    message.append( sep );
                    if ( origin.name == null )
                    {
                        message.append( "The root of the '" ).append( origin.vocabulary ).append( "' grammar" );
                    }
                    else
                    {
                        message.append( '\'' ).append( origin.name )
                               .append( "' in '" ).append( origin.vocabulary ).append( '\'' );
                    }
                    sep = ", ";
                }
            }
            throw new IllegalArgumentException( message.toString() );
        }
    }
}
