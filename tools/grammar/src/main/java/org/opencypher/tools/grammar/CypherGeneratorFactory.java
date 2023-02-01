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

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.generator.Choices;
import org.opencypher.generator.Generator;
import org.opencypher.generator.GeneratorFactory;
import org.opencypher.generator.InteractiveChoices;
import org.opencypher.generator.Node;
import org.opencypher.generator.ProductionReplacement;
import org.opencypher.generator.TracingChoices;
import org.opencypher.tools.io.Output;
import org.xml.sax.SAXException;

import static org.opencypher.tools.grammar.Main.execute;
import static org.opencypher.tools.io.LineInput.stdIn;
import static org.opencypher.tools.io.Output.output;
import static org.opencypher.tools.io.Output.stdOut;
import static org.opencypher.tools.io.Output.stringBuilder;

/**
 * Generates sample strings from a language.
 */
public class CypherGeneratorFactory extends GeneratorFactory<CypherGeneratorFactory.State>
        implements Supplier<Generator>
{
    public static final Supplier<Generator> INSTANCE = new CypherGeneratorFactory();

    @Override
    public Generator get()
    {
        try
        {
            return generatorResource( "/cypher.xml" );
        }
        catch ( ParserConfigurationException | SAXException | IOException e )
        {
            throw new IllegalStateException( "Failed to parse Cypher grammar.", e );
        }
    }

    public static void main( String... args ) throws Exception
    {
        execute( ( grammar, workingDir, out ) -> {
            Output.Readable output = stringBuilder();
            new CypherGeneratorFactory()
            {
                @Override
                protected Choices choices()
                {
                    switch ( System.getProperty( "choices", "default" ) )
                    {
                    case "interactive":
                        return new InteractiveChoices( stdIn(), stdOut(), null );
                    case "tracing":
                        return new TracingChoices( stdOut(), super.choices() );
                    default:
                        return super.choices();
                    }
                }
            }.generator( grammar ).generate( output );
            output( out ).println().append( output ).println();
        }, args );
    }

    @Override
    protected State newContext()
    {
        return new State();
    }

    @Replacement
    public void Variable( ProductionReplacement.Context<State> context )
    {
        Node parent = context.node().parent();
        switch ( parent.name() )
        {
        // projection
        case "ReturnItem":
            context.generateDefault();
            break;

        // adds a variable to the current scope
        case "UniqueConstraintSyntax": // commands syntax
        case "NodePropertyExistenceConstraintSyntax": // commands syntax
        case "RelationshipPatternSyntax": // commands syntax
        case "PatternPart": // basic grammar
        case "IdInCol": // basic grammar
        case "Reduce": // basic grammar
        case "StartPoint": // start syntax
        case "LoadCSV":
        case "Unwind":
        case "Foreach":
            context.generateDefault();
            break;

        // adds or references a variable in current scope
        case "RelationshipDetail": // basic grammar
        case "NodePattern": // basic grammar
            context.generateDefault();
            break;

        // references a defined variable
        case "Hint": // Variable defined in the Match this Hint belongs to
        case "SetItem":
        case "RemoveItem":
        case "Expression1": // basic grammar
            context.generateDefault();
            break;

        // unknown...
        case "Pragma":
            context.generateDefault();
            break;
        default:
            throw new IllegalArgumentException( "Cannot generate variable in " + parent.name() );
        }
    }

    @Replacement
    public void LabelName( ProductionReplacement.Context<State> context )
    {
        context.generateDefault();
    }

    @Replacement
    public void RelTypeName( ProductionReplacement.Context<State> context )
    {
        context.generateDefault();
    }

    @Replacement
    public void FunctionName( ProductionReplacement.Context<State> context )
    {
        context.generateDefault();
    }

    @Replacement
    public void PropertyKeyName( ProductionReplacement.Context<State> context )
    {
        context.generateDefault();
    }

    @Replacement
    public void IdentifierStart( ProductionReplacement.Context<State> context )
    {
        context.write( randomCodePoint( Character::isJavaIdentifierStart ) );
    }

    @Replacement
    public void IdentifierPart( ProductionReplacement.Context<State> context )
    {
        context.write( randomCodePoint( Character::isJavaIdentifierPart ) );
    }

    static int randomCodePoint( IntPredicate filter )
    {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int cp;
        do
        {
            cp = random.nextInt( Character.MIN_CODE_POINT, Character.MAX_CODE_POINT );
        } while ( !filter.test( cp ) );
        return cp;
    }

    static class State
    {
    }
}
