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
package org.opencypher.tools.grammar;

import java.io.OutputStream;
import java.io.Writer;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.GrammarVisitor;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.tools.output.Output;

import static org.opencypher.tools.output.Output.output;

public class ISO14977 implements GrammarVisitor<RuntimeException>
{
    public static void write( Grammar grammar, Writer writer )
    {
        write( grammar, output( writer ) );
    }

    public static void write( Grammar grammar, OutputStream stream )
    {
        write( grammar, output( stream ) );
    }

    public static void write( Grammar grammar, Output output )
    {
        String header = grammar.header();
        if ( header != null )
        {
            output.append( "(*\n * " )
                  .printLines( header, " * " )
                  .println( " *)" );
        }
        grammar.accept( new ISO14977( output ) );
    }

    public static void main( String... args ) throws Exception
    {
        Main.execute( ISO14977::write, args );
    }

    public static void append( Grammar.Term term, Output output )
    {
        term.accept( new ISO14977( output ) );
    }

    private final Output output;
    private String altPrefix = "";
    private boolean group;

    private ISO14977( Output output )
    {
        this.output = output;
    }

    @Override
    public void visitProduction( Production production ) throws RuntimeException
    {
        StringBuilder altPrefix = new StringBuilder( production.name().length() + 1 ).append( '\n' );
        for ( int i = production.name().length(); i-- > 0; )
        {
            altPrefix.append( ' ' );
        }
        String description = production.description();
        if ( description != null )
        {
            String prefix = "(* ";
            for ( int pos = 0, line; pos < description.length(); pos = line )
            {
                line = description.indexOf( '\n', pos );
                if ( line == -1 )
                {
                    line = description.length();
                }
                else
                {
                    line += 1;
                }
                output.append( prefix ).append( description, pos, line );
                prefix = " * ";
            }
            output.println( " *)" );
        }
        this.altPrefix = altPrefix.toString();
        group = false;
        output.append( production.name() ).append( " = " );
        production.definition().accept( this );
        output.println( " ;" ).println();
        this.altPrefix = "";
    }

    @Override
    public void visitAlternatives( Alternatives alternatives )
    {
        group( () -> {
            boolean prefix = false;
            for ( Grammar.Term term : alternatives )
            {
                if ( prefix )
                {
                    output.append( altPrefix ).append( " | " );
                }
                term.accept( this );
                prefix = true;
            }
            if ( alternatives.terms() > 1 )
            {
                output.append( altPrefix );
            }
        } );
    }

    @Override
    public void visitSequence( Sequence sequence )
    {
        String altPrefix = this.altPrefix;
        this.altPrefix = "";
        group( () -> {
            String prefix = "";
            for ( Grammar.Term term : sequence )
            {
                output.append( prefix );
                term.accept( this );
                prefix = ", ";
            }
        } );
        this.altPrefix = altPrefix;
    }

    @Override
    public void visitLiteral( Literal literal )
    {
        literal( literal.toString() );
    }

    public void literal( String value )
    {
        char enclose;
        int sq, dq;
        if ( (sq = value.indexOf( '\'' )) == -1 )
        {
            enclose = '\'';
        }
        else if ( (dq = value.indexOf( '"' )) == -1 )
        {
            enclose = '"';
        }
        else
        {
            char other;
            if ( sq < dq )
            {
                sq = dq;
                enclose = '"';
                other = '\'';
            }
            else
            {
                enclose = '\'';
                other = '"';
            }
            if ( group )
            {
                output.append( '(' );
            }
            int start = 0;
            for ( int end = sq; end != -1; start = end, end = value.indexOf( enclose, end + 1 ) )
            {
                output.append( enclose ).append( value.subSequence( start, end ) ).append( enclose ).append( ", " );
                char last = enclose;
                enclose = other;
                other = last;
            }
            output.append( enclose ).append( value.subSequence( start, value.length() ) ).append( enclose );
            if ( group )
            {
                output.append( ')' );
            }
            return;
        }
        output.append( enclose ).append( value ).append( enclose );
    }

    @Override
    public void visitCharacters( CharacterSet characters )
    {
        String name = characters.name();
        if ( name != null )
        {
            output.append( name );
        }
        else
        {
            characters.accept( new CharacterSet.DefinitionVisitor.NamedSetVisitor<RuntimeException>()
            {
                String sep = "";

                @Override
                public CharacterSet.ExclusionVisitor<RuntimeException> visitSet( String name )
                {
                    output.append( name );
                    return new CharacterSet.ExclusionVisitor<RuntimeException>()
                    {
                        String sep = " - (";

                        @Override
                        public void excludeCodePoint( int cp ) throws RuntimeException
                        {
                            output.append( sep );
                            codePoint( cp );
                            sep = " | ";
                        }

                        @Override
                        public void close() throws RuntimeException
                        {
                            if ( sep.charAt( sep.length() - 1 ) != '(' )
                            {
                                output.append( ')' );
                            }
                        }
                    };
                }

                @Override
                public void visitCodePoint( int cp )
                {
                    output.append( sep );
                    codePoint( cp );
                    sep = " | ";
                }

                private void codePoint( int cp )
                {
                    String controlChar = CharacterSet.controlCharName( cp );
                    if ( controlChar != null )
                    {
                        output.append( controlChar );
                    }
                    else if ( cp == '\'' )
                    {
                        output.append( "\"'\"" );
                    }
                    else
                    {
                        output.append( '\'' ).appendCodePoint( cp ).append( '\'' );
                    }
                }
            } );
        }
    }

    @Override
    public void visitNonTerminal( NonTerminal nonTerminal )
    {
        output.append( nonTerminal.productionName() );
    }

    @Override
    public void visitOptional( Optional optional )
    {
        String altPrefix = this.altPrefix;
        this.altPrefix = "";
        {
            group( '[', () -> optional.term().accept( this ), ']' );
        }
        this.altPrefix = altPrefix;
    }

    @Override
    public void visitRepetition( Repetition repetition )
    {
        String altPrefix = this.altPrefix;
        this.altPrefix = "";
        {
            if ( !repetition.limited() )
            {
                if ( repetition.minTimes() == 0 || repetition.minTimes() == 1 )
                {
                    group( '{', () -> repetition.term().accept( this ), '}' );
                    if ( repetition.minTimes() == 1 )
                    {
                        output.append( '-' );
                    }
                }
                else
                {
                    group( () -> {
                        output.append( repetition.minTimes() ).append( " * " );
                        repetition.term().accept( this );
                        output.append( ", " );
                        group( '{', () -> repetition.term().accept( this ), '}' );
                    } );
                }
            }
            else if ( repetition.minTimes() == repetition.maxTimes() )
            {
                output.append( repetition.minTimes() ).append( " * " );
                grouping( () -> repetition.term().accept( this ) );
            }
            else if ( repetition.minTimes() > 0 )
            {
                group( () -> {
                    output.append( repetition.minTimes() ).append( " * " );
                    repetition.term().accept( this );
                    output.append( ", " );
                    output.append( repetition.maxTimes() - repetition.minTimes() ).append( " * " );
                    group( '[', () -> repetition.term().accept( this ), ']' );
                } );
            }
            else
            {
                output.append( repetition.maxTimes() - repetition.minTimes() ).append( " * " );
                group( '[', () -> repetition.term().accept( this ), ']' );
            }
        }
        this.altPrefix = altPrefix;
    }

    private void grouping( Runnable action )
    {
        boolean group = this.group;
        this.group = true;
        action.run();
        this.group = group;
    }

    private void group( Runnable action )
    {
        boolean group = this.group;
        this.group = true;
        if ( group )
        {
            output.append( '(' );
        }
        action.run();
        if ( group )
        {
            output.append( ')' );
        }
        this.group = group;
    }

    private void group( char prefix, Runnable action, char suffix )
    {
        boolean group = this.group;
        this.group = false;
        output.append( prefix );
        action.run();
        output.append( suffix );
        this.group = group;
    }
}
