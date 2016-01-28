/*
 * Copyright (c) 2015-2016 "Neo Technology,"
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
 */
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
