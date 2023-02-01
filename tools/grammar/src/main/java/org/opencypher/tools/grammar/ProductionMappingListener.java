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
package org.opencypher.tools.grammar;

import org.opencypher.grammar.Production;

import java.util.Arrays;

interface ProductionMappingListener
{
    void map( String name, Production production );

    static ProductionMappingListener combine( ProductionMappingListener... listeners )
    {
        if ( listeners == null || listeners.length == 0 )
        {
            return NONE;
        }
        if ( listeners.length == 1 )
        {
            if ( listeners[0] == null )
            {
                return NONE;
            }
            return listeners[0];
        }
        int len = listeners.length;
        for ( int i = 0; i < len; i++ )
        {
            while ( (listeners[i] == null || listeners[i] == NONE) && i < len )
            {
                listeners[i] = listeners[--len];
            }
            for ( int j = 0; j < i; j++ )
            {
                if ( listeners[i] == listeners[j] )
                {
                    listeners[i--] = listeners[--len];
                    break;
                }
            }
        }
        if ( len == 0 )
        {
            return NONE;
        }
        if ( len == 1 )
        {
            return listeners[0];
        }
        ProductionMappingListener[] targets = Arrays.copyOf( listeners, len );
        return ( name, production ) ->
        {
            for ( ProductionMappingListener listener : targets )
            {
                listener.map( name, production );
            }
        };
    }

    ProductionMappingListener NONE = ( name, production ) ->
    {
    };
}
