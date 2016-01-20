package org.opencypher.tools.grammar;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.opencypher.generator.Generator;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.VerboseUnit;
import org.opencypher.tools.output.Output;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.opencypher.grammar.Grammar.Builder.Option.IGNORE_UNUSED_PRODUCTIONS;
import static org.opencypher.grammar.Grammar.anyCharacter;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.oneOf;
import static org.opencypher.grammar.Grammar.sequence;
import static org.opencypher.grammar.Grammar.zeroOrMore;
import static org.opencypher.tools.Assert.with;
import static org.opencypher.tools.output.Output.stringBuilder;

public class CypherGeneratorFactoryTest
{
    public final @Rule VerboseUnit verboseUnit = new VerboseUnit( 100_000 );
    private final StateStub state = new StateStub();

    @Test
    public void shouldGenerateIdentifier() throws Exception
    {
        // given
        Generator identifier = generator( "identifier",
                                          nonTerminal( "IdentifierStart" ),
                                          zeroOrMore( nonTerminal( "IdentifierPart" ) ) );

        // then
        assertGenerates( identifier, with( String::length, greaterThanOrEqualTo( 1 ) ) );
    }

    @Test
    public void shouldGenerateNodeVariable() throws Exception
    {
        // given
        Generator node = generator( "NodePattern", literal( "(" ), nonTerminal( "Variable" ), literal( ")" ) );

        // then
        assertGenerates( node, with( String::length, greaterThanOrEqualTo( 3 ) ),
                         startsWith( "(" ), endsWith( ")" ) );
    }

    private Generator generator( String name, Grammar.Term first, Grammar.Term... rest )
    {
        return new CypherGeneratorFactory()
        {
            @Override
            protected State newContext()
            {
                return state;
            }
        }.generator(
                grammar( name )
                        .production( name, sequence( first, rest ) )
                        // The Cypher syntax in the immediate vicinity of 'Variable'
                        .production( "Variable", nonTerminal( "SymbolicNameString" ) )
                        .production( "LabelName", nonTerminal( "SymbolicNameString" ) )
                        .production( "RelTypeName", nonTerminal( "SymbolicNameString" ) )
                        .production( "FunctionName", nonTerminal( "SymbolicNameString" ) )
                        .production( "PropertyKeyName", nonTerminal( "SymbolicNameString" ) )
                        .production( "SymbolicNameString", oneOf(
                                nonTerminal( "UnescapedSymbolicNameString" ),
                                nonTerminal( "EscapedSymbolicNameString" ) ) )
                        .production( "parameter", sequence( literal( "{" ), oneOf(
                                nonTerminal( "UnescapedSymbolicNameString" ),
                                nonTerminal( "EscapedSymbolicNameString" ) ), literal( "}" ) ) )
                        .production( "UnescapedSymbolicNameString", sequence(
                                nonTerminal( "IdentifierStart" ),
                                zeroOrMore( nonTerminal( "IdentifierPart" ) ) ) )
                        .production( "EscapedSymbolicNameString", sequence(
                                literal( "`" ),
                                zeroOrMore( anyCharacter().except( '`' ) ),
                                literal( "`" ) ) )
                        .production( "IdentifierStart", charactersOfSet( "JavaIdentifierStart" ) )
                        .production( "IdentifierPart", charactersOfSet( "JavaIdentifierPart" ) )
                        .build( IGNORE_UNUSED_PRODUCTIONS ) );
    }

    @SafeVarargs
    private final void assertGenerates( Generator generator, Matcher<String>... matches )
    {
        verboseUnit.test( out -> {
            Output.Readable buffer = stringBuilder();
            generator.generate( buffer.and( out ) );
            assertThat( buffer.toString(), allOf( matches ) );
        } );
    }

    private static class StateStub extends CypherGeneratorFactory.State
    {
    }
}
