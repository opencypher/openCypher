package org.opencypher.tools.grammar;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.generator.Generator;
import org.opencypher.generator.GeneratorFactory;
import org.opencypher.generator.Node;
import org.opencypher.generator.ProductionReplacement;
import org.xml.sax.SAXException;

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
    }

    @Replacement
    public void RelTypeName( ProductionReplacement.Context<State> context )
    {
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
