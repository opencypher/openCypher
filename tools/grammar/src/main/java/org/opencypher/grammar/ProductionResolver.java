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

import java.util.Map;
import java.util.Set;

class ProductionResolver
{
    private final Grammar.Resolver resolver;
    private final Map<String, ProductionNode> productions;
    private final Dependencies dependencies;
    private final Set<String> unused;
    private final Set<ProtoGrammar.ResolutionOption> options;
    private final Set<String> legacyProductions;
    private int nonTerminalIndex;

    public ProductionResolver( Grammar.Resolver resolver, Map<String,ProductionNode> productions, Dependencies dependencies,
            Set<String> unused, Set<ProtoGrammar.ResolutionOption> options, Set<String> legacyProductions )
    {
        this.resolver = resolver;
        this.productions = productions;
        this.dependencies = dependencies;
        this.unused = unused;
        this.options = options;
        this.legacyProductions = legacyProductions;
    }

    public ProductionNode resolveProduction( ProductionNode origin, String name )
    {
        ProductionNode production = productions.get( name.toLowerCase() );
        if ( production == null && !options.contains( ProtoGrammar.ResolutionOption.INCLUDE_LEGACY ) && !legacyProductions.contains( name.toLowerCase() ) )
        {
            dependencies.missingProduction( name, origin );
        }
        else if ( production != null && production.name.equals( name ) )
        {
            unused.remove( name );
            dependencies.usedFrom( name, origin );
        }
        else
        {
            production = null;
        }
        return production;
    }

    public int nextNonTerminalIndex()
    {
        return nonTerminalIndex++;
    }

    public Grammar.Unresolved.Production resolve( ForeignReference reference )
    {
        return resolver.resolve( reference );
    }
}
