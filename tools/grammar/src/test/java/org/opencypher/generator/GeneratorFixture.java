package org.opencypher.generator;

import java.util.function.Function;

import org.opencypher.grammar.Grammar;

import static org.junit.Assert.assertEquals;
import static org.opencypher.tools.output.Output.output;

public class GeneratorFixture
{
    public static void assertGenerates( Grammar.Builder grammar, String expected )
    {
        assertGenerates( grammar.build(), expected );
    }

    public static void assertGenerates( Grammar grammar, String expected )
    {
        assertGenerates( grammar, x -> expected );
    }

    @SafeVarargs
    public static void assertGenerates( Grammar.Builder grammar, Function<GeneratorFixture, String>... conditions )
    {
        assertGenerates( grammar.build(), conditions );
    }

    @SafeVarargs
    public static void assertGenerates( Grammar grammar, Function<GeneratorFixture, String>... conditions )
    {
        StringBuilder actual = new StringBuilder();
        for ( Function<GeneratorFixture, String> condition : conditions )
        {
            GeneratorFixture fixture = new GeneratorFixture();
            String expected = condition.apply( fixture );
            actual.setLength( 0 );
            new Generator( fixture.random(), grammar ).generate( output( actual ) );
            assertEquals( expected, actual.toString() );
        }
    }

    private Object state;

    private GeneratorFixture()
    {
        this.state = new ChoicesFixture();
    }

    public GeneratorFixture picking( String literal )
    {
        state().pick( literal );
        return this;
    }

    public GeneratorFixture picking( Grammar.Term term )
    {
        state().pick( term );
        return this;
    }

    public GeneratorFixture picking( int codePoint )
    {
        state().pick( codePoint );
        return this;
    }

    public String generates( String result )
    {
        random();
        return result;
    }

    public GeneratorFixture includeOptional()
    {
        state().includeOptional();
        return this;
    }

    public GeneratorFixture skipOptional()
    {
        state().excludeOptional();
        return this;
    }

    public GeneratorFixture repeat( int times, ChoicesFixture.Repetition invocation )
    {
        state().repeat( times, invocation );
        return this;
    }

    private ChoicesFixture state()
    {
        if ( state instanceof ChoicesFixture )
        {
            return (ChoicesFixture) state;
        }
        throw new IllegalStateException();
    }

    private Choices random()
    {
        if ( !(state instanceof Choices) )
        {
            state = state().random();
        }
        return (Choices) state;
    }
}
