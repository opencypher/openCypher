/*
 * Copyright (c) 2015-2023 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.grammar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class Dependencies
{
    private Map<String, Set<ProductionNode>> missingProductions;
    private final Map<String, Set<ProductionNode>> dependencies = new LinkedHashMap<>();

    public void missingProduction( String name, ProductionNode origin )
    {
        if ( missingProductions == null )
        {
            missingProductions = new LinkedHashMap<>();
        }
        update( missingProductions, name, origin );
    }

    public void usedFrom( String name, ProductionNode origin )
    {
        update( dependencies, name, origin );
    }

    private static void update( Map<String, Set<ProductionNode>> productions, String name, ProductionNode origin )
    {
        Set<ProductionNode> origins = productions.get( name );
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
            for ( Map.Entry<String, Set<ProductionNode>> entry : missingProductions.entrySet() )
            {
                message.append( "\n  '" ).append( entry.getKey() ).append( "'" );
                String sep = " used from: ";
                for ( ProductionNode origin : entry.getValue() )
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

    public void invalidCharacterSet( String name, ProductionNode origin )
    {
        throw new IllegalArgumentException(
                "Invalid character set: '" + name + "', a production exists with that name." );
    }
}
