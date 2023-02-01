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

import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.opencypher.generator.ProductionReplacement.replace;
import static org.opencypher.tools.Assert.assertEquals;
import static org.opencypher.tools.io.Output.string;

public class NodeWritingTest
{
    public final @Rule TestName testName = new TestName();

    @Test
    public void shouldWriteLiterals() throws Exception
    {
        assertGenerates( "abc", literal( "a" ), literal( "b" ), literal( "c" ) );
    }

    @Test
    public void shouldWriteTree() throws Exception
    {
        assertGenerates( "a1,b2",
                         child( "one", literal( "a" ), literal( "1" ) ),
                         literal( "," ),
                         child( "two", literal( "b" ), literal( "2" ) ) );
    }

    @Test
    public void emptyNodesAreNotWritten() throws Exception
    {
        assertGenerates( "hello", child( "ignored" ), literal( "hello" ) );
    }

    @Test
    public void shouldWriteReplacement() throws Exception
    {
        assertGenerates( "!bar", literal( "!" ), production( replace( "foo", foo -> foo.write( "bar" ) ) ) );
    }

    @Test
    @SuppressWarnings("Convert2MethodRef")
    public void shouldWriteDefaultValueForReplacement() throws Exception
    {
        assertGenerates( "!foo", literal( "!" ), production( replace( "foo", foo -> foo.generateDefault() ), literal( "foo" ) ) );
    }

    @Test
    public void shouldAllowAccessToSurroundingTreeFromReplacements() throws Exception
    {
        assertGenerates( "aalpha", child( "alpha", literal( "a" ), production(
                replace("beta", beta -> beta.write( beta.node().parent().name() ) ) ) ) );
    }

    // dsl

    @SafeVarargs
    final void assertGenerates( String expected, Consumer<Node.Tree>... data )
    {
        Node node = node( data );
        assertEquals( () -> string( node, Node::sExpression ), expected, string( node, Node::write ) );
    }

    @SafeVarargs
    final Node node( Consumer<Node.Tree>... data )
    {
        return apply( Node.root( testName.getMethodName() ), data );
    }

    static Consumer<Node.Tree> literal( String literal )
    {
        return node -> node.literal( literal );
    }

    @SafeVarargs
    static Consumer<Node.Tree> child( String name, Consumer<Node.Tree>... data )
    {
        return node -> apply( node.child( name ), data );
    }

    static Consumer<Node.Tree> production( ProductionReplacement<Void> replacement )
    {
        return production( replacement, defaults -> {
            throw new UnsupportedOperationException( "No default value for " + replacement.production() );
        } );
    }

    static Consumer<Node.Tree> production( ProductionReplacement<Void> replacement, Consumer<Node.Tree> defaultValue )
    {
        return node -> node.production( replacement.production(), replacement, null, defaultValue );
    }

    @SafeVarargs
    static Node apply( Node.Tree node, Consumer<Node.Tree>... data )
    {
        for ( Consumer<Node.Tree> item : data )
        {
            item.accept( node );
        }
        return node;
    }
}
