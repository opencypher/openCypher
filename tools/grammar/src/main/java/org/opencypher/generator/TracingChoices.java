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

import org.opencypher.grammar.BiasedTerms;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Repetition;
import org.opencypher.tools.grammar.ISO14977;
import org.opencypher.tools.output.Output;

import static java.util.Objects.requireNonNull;

public class TracingChoices implements Choices
{
    private final Output output;
    private final Choices choices;

    public TracingChoices( Output output, Choices choices )
    {
        this.output = requireNonNull( output, "output" );
        this.choices = requireNonNull( choices, "choices" );
    }

    @Override
    public Grammar.Term choose( Node location, BiasedTerms alternatives )
    {
        Grammar.Term result = choices.choose( location, alternatives );
        location( output.append( "At " ), location )
                .append( " choose: " )
                .append( result.toString() )
                .println();
        return result;
    }

    @Override
    public int repetition( Node location, Repetition repetition )
    {
        int result = choices.repetition( location, repetition );
        location( output.append( "At " ), location )
                .append( " repeat " )
                .append( repetition.term(), ISO14977::append )
                .append( " " )
                .append( result )
                .println( " times" );
        return result;
    }

    @Override
    public boolean includeOptional( Node location, Optional optional )
    {
        boolean result = choices.includeOptional( location, optional );
        location( output.append( "At " ), location )
                .append( result ? " include optional " : " exclude optional " )
                .append( optional.term(), ISO14977::append )
                .println( "." );
        return result;
    }

    @Override
    public int codePoint( Node location, CharacterSet characters )
    {
        int result = choices.codePoint( location, characters );
        location( output.append( "At " ), location )
                .append( " emit char '" )
                .appendCodePoint( result )
                .println( "'." );
        return result;
    }

    static Output location( Output output, Node location )
    {
        Node parent = location.parent();
        if ( parent != null )
        {
            location( output, parent );
            output.append( " -> " );
        }
        output.append( location.name() );
        return output;
    }
}
