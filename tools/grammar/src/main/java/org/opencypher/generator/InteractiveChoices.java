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

import java.util.function.Function;

import org.opencypher.grammar.BiasedTerms;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Repetition;
import org.opencypher.tools.grammar.ISO14977;
import org.opencypher.tools.io.LineInput;
import org.opencypher.tools.io.Output;

import static java.lang.Integer.parseUnsignedInt;

import static org.opencypher.tools.Functions.requireNonNull;

public class InteractiveChoices implements Choices
{
    private final Interface repl;

    public InteractiveChoices( LineInput input, Output output, Choices defaultChoices )
    {
        repl = new Interface(
                requireNonNull( LineInput.class, input ),
                requireNonNull( Output.class, output ),
                defaultChoices );
    }

    private interface Default<S, T>
    {
        T of( Choices defaultChoices, Node location, S in );
    }

    private interface Body<S, T>
    {
        void write( Output output, S in, T defaultChoice );
    }

    private static class Interface
    {
        private final LineInput input;
        private final Output output;
        private final Choices defaultChoices;

        private Interface( LineInput input, Output output, Choices defaultChoices )
        {
            this.input = input;
            this.output = output;
            this.defaultChoices = defaultChoices;
        }

        <S, T> T eval( Node loc, S in, Default<S, T> def, Body<S, T> body, Function<String, T> parse )
        {
            T defaultChoice = defaultChoices == null ? null : def.of( defaultChoices, loc, in );
            for (; ; )
            {
                output.append( "At " );
                TracingChoices.location( output, loc );
                body.write( output, in, defaultChoice );
                output.flush();
                String line = input.read().trim();
                if ( line.isEmpty() )
                {
                    if ( defaultChoice != null )
                    {
                        return defaultChoice;
                    }
                    else
                    {
                        output.append( "Invalid input, no default choices available." );
                    }
                }
                else
                {
                    try
                    {
                        T result = parse.apply( line );
                        if ( result == null )
                        {
                            output.append( "Invalid input." );
                        }
                        else
                        {
                            return result;
                        }
                    }
                    catch ( Exception e )
                    {
                        output.append( "Invalid input: " ).append( e.getMessage() );
                    }
                }
                output.println();
            }
        }
    }

    @Override
    public Grammar.Term choose( Node location, BiasedTerms alternatives )
    {
        return repl.eval( location, alternatives, Choices::choose, InteractiveChoices::alternatives,
                          in -> alternatives.term( parseUnsignedInt( in ) ) );
    }

    @Override
    public int repetition( Node location, Repetition repetition )
    {
        return repl.eval( location, repetition, Choices::repetition, InteractiveChoices::repetition,
                          parseInt( repetition ) );
    }

    @Override
    public boolean includeOptional( Node location, Optional optional )
    {
        return repl.eval( location, optional, Choices::includeOptional, InteractiveChoices::optional,
                          InteractiveChoices::parseYesNo );
    }

    @Override
    public int codePoint( Node location, CharacterSet characters )
    {
        return repl.eval( location, characters, Choices::codePoint, InteractiveChoices::character,
                          parseChar( characters ) );
    }

    private static void alternatives( Output output, BiasedTerms alternatives, Grammar.Term def )
    {
        output.println( ", chose one of:" );
        for ( int i = 0, terms = alternatives.terms(); i < terms; i++ )
        {
            output.append( i ).append( ". " );
            Grammar.Term term = alternatives.term( i );
            ISO14977.append( term, output );
            output.append( term == def ? " [default]" : "" );
            output.println();
        }
        output.append( "Chose an alternative [0-" ).append( alternatives.terms() - 1 ).append( "]: " );
    }

    private static void repetition( Output output, Repetition repetition, Integer def )
    {
        output.append( ", how many times should " );
        ISO14977.append( repetition.term(), output );
        output.append( " be repeated? " );
        if ( repetition.limited() )
        {
            output.append( '[' ).append( repetition.minTimes() ).append( repetition.maxTimes() ).append( ']' );
        }
        else
        {
            output.append( "[min " ).append( repetition.minTimes() ).append( ']' );
        }
        if ( def != null )
        {
            output.append( " [default=" ).append( def ).append( ']' );
        }
        output.append( ": " );
    }

    private static void optional( Output output, Optional optional, Boolean def )
    {
        output.append( ", should optional " );
        ISO14977.append( optional.term(), output );
        output.append( " be included? " );
        if ( def == null )
        {
            output.append( "[yn]: " );
        }
        else if ( def )
        {
            output.append( "[Yn]: " );
        }
        else
        {
            output.append( "[yN]: " );
        }
    }

    private static void character( Output output, CharacterSet characters, Integer def )
    {
        output.append( ", emit character" );
        if ( def != null )
        {
            output.append( " [default: 0x" ).append( Integer.toHexString( def ) ).append( "]" );
        }
        output.append( ": " );
    }

    private static Function<String, Integer> parseChar( CharacterSet characters )
    {
        return in -> {
            int cp;
            if ( in.length() >= 1 && Character.charCount( in.codePointAt( 0 ) ) == in.length() )
            {
                cp = in.codePointAt( 0 );
            }
            else if ( in.startsWith( "0x" ) )
            {
                cp = Integer.parseInt( in.substring( 2 ), 16 );
            }
            else
            {
                cp = parseUnsignedInt( in );
            }
            return characters.contains( cp ) ? cp : null;
        };
    }

    private static Function<String, Integer> parseInt( Repetition repetition )
    {
        return in -> {
            int result = parseUnsignedInt( in );
            return (result < repetition.minTimes() || (repetition.limited() && result > repetition.maxTimes()))
                   ? null : result;
        };
    }

    private static Boolean parseYesNo( String in )
    {
        switch ( in.toLowerCase() )
        {
        case "y":
        case "yes":
            return true;
        case "n":
        case "no":
            return false;
        default:
            return null;
        }
    }
}
