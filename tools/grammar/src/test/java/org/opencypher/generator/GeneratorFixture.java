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
package org.opencypher.generator;

import java.util.function.Function;

import org.opencypher.grammar.Grammar;

import static org.junit.Assert.assertEquals;
import static org.opencypher.tools.io.Output.output;

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
