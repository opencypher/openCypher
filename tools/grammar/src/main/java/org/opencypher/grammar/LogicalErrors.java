package org.opencypher.grammar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class LogicalErrors
{
    private Map<String, Set<String>> missingProductions;

    public void missingProduction( String name, String origin )
    {
        if ( missingProductions == null )
        {
            missingProductions = new LinkedHashMap<>();
        }
        Set<String> origins = missingProductions.get( name );
        if ( origins == null )
        {
            missingProductions.put( name, origins = Collections.newSetFromMap( new LinkedHashMap<>() ) );
        }
        origins.add( origin );
    }

    public void report()
    {
        if ( missingProductions != null && !missingProductions.isEmpty() )
        {
            StringBuilder message = new StringBuilder()
                    .append( "Productions used in non-terminals have not been defined:" );
            for ( Map.Entry<String, Set<String>> entry : missingProductions.entrySet() )
            {
                message.append( "\n  " ).append( entry.getKey() );
                String sep = " used from: ";
                for ( String origin : entry.getValue() )
                {
                    message.append( sep ).append( origin );
                    sep = ", ";
                }
            }
            throw new IllegalArgumentException( message.toString() );
        }
    }
}
