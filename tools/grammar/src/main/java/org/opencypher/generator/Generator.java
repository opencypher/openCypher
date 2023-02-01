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
package org.opencypher.generator;

import java.util.function.Supplier;

import org.opencypher.grammar.Grammar;
import org.opencypher.tools.io.Output;

import static java.util.Objects.requireNonNull;

import static org.opencypher.tools.Functions.map;
import static org.opencypher.tools.Functions.requireNonNull;

public final class Generator
{
    private final Grammar grammar;
    private final TreeBuilder<?> builder;

    @SafeVarargs
    public Generator( Grammar grammar, ProductionReplacement<Void>... replacements )
    {
        this( Choices.SIMPLE, grammar, replacements );
    }

    @SafeVarargs
    public <T> Generator( Grammar grammar, Supplier<T> context, ProductionReplacement<T>... replacements )
    {
        this( Choices.SIMPLE, grammar, context, replacements );
    }

    @SafeVarargs
    public Generator( Choices random, Grammar grammar, ProductionReplacement<Void>... replacements )
    {
        this( random, grammar, () -> null, replacements );
    }

    @SafeVarargs
    public <T> Generator( Choices random, Grammar grammar, Supplier<T> context,
                          ProductionReplacement<T>... replacements )
    {
        this.grammar = grammar;
        this.builder = new TreeBuilder<>(
                requireNonNull( Choices.class, random ),
                requireNonNull( context, "context" ),
                map( replacement -> {
                    String name = replacement.production();
                    if ( !grammar.hasProduction( name ) )
                    {
                        throw new IllegalArgumentException(
                                "Grammar for " + grammar.language() + " does not contain a production for " +
                                replacement.production() );
                    }
                    return name;
                }, replacements ) );
    }

    public void generate( String start, Output output )
    {
        generateTree( start ).write( output );
    }

    public void generate( Output output )
    {
        generate( grammar.language(), output );
    }

    Node generateTree( String start )
    {
        return generateTree( grammar, builder, start );
    }

    private static <T> Node generateTree( Grammar grammar, TreeBuilder<T> builder, String start )
    {
        return builder.buildTree( grammar.transform( start, builder, null ) );
    }
}
