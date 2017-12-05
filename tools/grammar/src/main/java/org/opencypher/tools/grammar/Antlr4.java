/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Production;
import org.opencypher.tools.io.Output;

import static java.lang.Character.charCount;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toUpperCase;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.opencypher.tools.grammar.Main.execute;
import static org.opencypher.tools.io.Output.output;

/**
 * Generates an ANTLR (version 4) grammar from a {@link Grammar}.
 */
public class Antlr4 extends BnfWriter
{
    static String PREFIX = "oC_";

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
            output.append( "/*\n * " )
                  .printLines( header, " * " )
                  .println( " */" );
        }
        output.append( "grammar " ).append( grammar.language() ).println( ";\n" );

        try ( Antlr4 antlr = new Antlr4( output ) )
        {
            grammar.accept( antlr );
        }
    }

    public static void main( String... args ) throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        execute( Antlr4::write, out, "grammar/cypher.xml" );

        System.out.print( out.toString( UTF_8.name() ) );
    }

    private final Map<String, CharacterSet> fragmentRules = new HashMap<>();
    private final Set<String> seenKeywords = new HashSet<>();
    /*
     * Mutable state -- this map keeps track of the keywords in the current production,
     * and is emptied when the production is exited.
     */
    private final Map<String, String> keywordsInProduction = new LinkedHashMap<>();

    private Antlr4( Output output )
    {
        super( output );
    }

    @Override
    public void close()
    {
        /*
         * This prints all the 'fragment' rules (rules that are used to construct lexer tokens,
         * and the only type of rule that can use the character set syntax []) at the bottom of the grammar file.
         */
        for ( Map.Entry<String,CharacterSet> rule : fragmentRules.entrySet() )
        {
            CharacterSet set = rule.getValue();
            output.append( "fragment " );
            lexerRule( rule.getKey() ).append( " : " );
            if ( CharacterSet.ANY.equals( set.name() ) )
            {
                set.accept( new AnyCharacterExceptFormatter( output ) );
            }
            else
            {
                output.append( '[' );
                set.accept( new SetFormatter( output ) );
                output.append( ']' );
            }
            output.println( " ;\n" );
        }
    }

    @Override
    protected void productionCommentPrefix()
    {
        output.append( "/**\n * " );
    }

    @Override
    protected void productionCommentLinePrefix()
    {
        output.append( " * " );
    }

    @Override
    protected void productionCommentSuffix()
    {
        output.println( " */" );
    }

    private String currentProduction;
    private int nextLexerRule;

    @Override
    protected void productionStart( Production p )
    {
        currentProduction = p.name();
        if ( p.lexer() )
        {
            lexerRule( currentProduction ).append( " : " );
        }
        else
        {
            parserRule( currentProduction ).append( " : " );
            nextLexerRule = 0;
        }
    }

    @Override
    protected void productionEnd()
    {
        output.println( " ;" ).println();

        /*
         * We print out lexer rules for all literal words mentioned in the production
         */
        for ( Map.Entry<String,String> lexerRule : keywordsInProduction.entrySet() )
        {
            String ruleName = lexerRule.getKey();
            // Except the ones we've already seen!
            if ( !seenKeywords.contains( ruleName ) )
            {
                seenKeywords.add( ruleName );
                caseInsensitiveProductionStart( ruleName );
                for ( char c : lexerRule.getValue().toCharArray() )
                {
                    groupWith( '(', () ->
                    {
                        literal( String.valueOf( c ).toUpperCase() );
                        alternativesSeparator();
                        literal( String.valueOf( c ).toLowerCase() );
                    }, ')' );
                    sequenceSeparator();
                }
                output.println( " ;" ).println();
            }
        }
        keywordsInProduction.clear();

        currentProduction = null;
    }

    private void addFragmentRule( CharacterSet characters )
    {
        String rule = currentProduction + "_" + nextLexerRule++;
        fragmentRules.put( rule, characters );
        lexerRule( rule );
    }

    @Override
    protected void alternativesLinePrefix( int altPrefix )
    {
        if ( altPrefix > 0 )
        {
            output.println();
            while ( altPrefix-- > 0 )
            {
                output.append( ' ' );
            }
        }
    }

    @Override
    protected void alternativesSeparator()
    {
        output.append( " | " );
    }

    @Override
    protected void sequenceSeparator()
    {
        output.append( " " );
    }

    @Override
    protected void groupPrefix()
    {
        output.append( "( " );
    }

    @Override
    protected void groupSuffix()
    {
        output.append( " )" );
    }

    @Override
    protected boolean optionalPrefix()
    {
        return false;
    }

    @Override
    protected void optionalSuffix()
    {
        output.append( "?" );
    }

    @Override
    protected void repeat( int minTimes, Integer maxTimes, Runnable repeated )
    {
        if ( maxTimes == null )
        {
            if ( minTimes == 0 )
            {
                groupWith( '(', repeated, ')' );
                output.append( "*" );
                return;
            }
            else if ( minTimes == 1 )
            {
                groupWith( '(', repeated, ')' );
                output.append( "+" );
                return;
            }
        }
        else if ( maxTimes == 1 && minTimes == 0 )
        {
            groupWith( '(', repeated, ')' );
            optionalSuffix();
            return;
        }
        else if ( minTimes == maxTimes )
        {
            groupWith( '(', () -> {
                for ( int i = 0; i < minTimes; i++ )
                {
                    if ( i > 0 )
                    {
                        sequenceSeparator();
                    }
                    repeated.run();
                }
            }, ')' );
            return;
        }
        throw new UnsupportedOperationException(
                format( "The Antlr formatter does not support minTimes=%d, maxTimes=%s", minTimes, maxTimes ) );
    }

    @Override
    protected void characterSet( CharacterSet characters )
    {
        String setName = characters.name();
        if ( setName == null )
        {
            addFragmentRule( characters );
        }
        else if ( setName.equals( CharacterSet.EOI ) )
        {
            output.append( "EOF" );
        }
        else
        {
            fragmentRules.put( setName, characters );
            lexerRule( setName );
        }
    }

    @Override
    protected void nonTerminal( NonTerminal nonTerminal )
    {
        if ( nonTerminal.production().lexer() )
        {
            lexerRule( nonTerminal.productionName() );
        }
        else
        {
            parserRule( nonTerminal.productionName() );
        }
    }

    private Output parserRule( String name )
    {
        return output.append( prefix( name ) );
    }

    private Output lexerRule( String name )
    {
        int cp = name.codePointAt( 0 );
        if ( !isUpperCase( cp ) )
        {
            if ( name.codePoints().noneMatch( Character::isUpperCase ) )
            {
                return output.append( name.toUpperCase() );
            }
            return output.appendCodePoint( toUpperCase( cp ) )
                         .append( name, charCount( cp ), name.length() );
        }
        else
        {
            return output.append( name );
        }
    }

    @Override
    protected String prefix( String s )
    {
        return Antlr4.PREFIX + s;
    }

    @Override
    protected void literal( String value )
    {
        escapeAndEnclose( value );
    }

    private boolean reserved( String ruleName )
    {
        return ruleName.equals( "SKIP" );
    }

    private void inline( String value )
    {
        group( () -> {
            String sep = "";
            int start = 0;
            for ( int i = 0, end = value.length(), cp; i < end; i += Character.charCount( cp ) )
            {
                cp = value.charAt( i );
                if ( Character.isLowerCase( cp ) || Character.isUpperCase( cp ) || Character.isTitleCase( cp ) )
                {
                    if ( start < i )
                    {
                        output.append( sep );
                        sep = " ";
                        escapeAndEnclose( value.substring( start, i ) );
                    }
                    output.append( sep );
                    sep = " ";
                    start = i + Character.charCount( cp );
                    cp = Character.toUpperCase( cp );
                    String upper = String.valueOf( (char) cp );
                    escapeAndEnclose( upper );
                    alternativesSeparator();
                    escapeAndEnclose( upper.toLowerCase() );
                }
            }
            if ( start < value.length() )
            {
                output.append( sep );
                escapeAndEnclose( value.substring( start ) );
            }
        } );
    }

    @Override
    protected void caseInsensitive( String value )
    {
        if ( value.length() == 1 )
        {
            inline( value );
        }
        else
        {
            String lexerRule = value.toUpperCase();
            if ( !Character.isLetter( value.codePointAt( 0 ) ) || reserved( lexerRule ) )
            {
                lexerRule = "L_" + lexerRule;
            }
            output.append( lexerRule );
            keywordsInProduction.put( lexerRule, value );
        }
    }

    private void escapeAndEnclose( String value )
    {
        output.append( "'" ).escape( value, Antlr4::escapes ).append( "'" );
    }

    @Override
    protected void caseInsensitiveProductionStart( String name )
    {
        currentProduction = name;
        lexerRule( currentProduction ).append( " : " );
        nextLexerRule = 0;
    }

    @Override
    protected void epsilon()
    {
    }

    private static String escapes( int cp )
    {
        switch ( cp )
        { // <pre>
            case '\r': return "\\r";
            case '\n': return "\\n";
            case '\t': return "\\t";
            case '\b': return "\\b";
            case '\f': return "\\f";
            case '\'': return "\\'";
            case '\\': return "\\\\";
            default: return null;
        } //</pre>
    }

    private static class AnyCharacterExceptFormatter
            implements CharacterSet.DefinitionVisitor.NamedSetVisitor<RuntimeException>,
                       CharacterSet.ExclusionVisitor<RuntimeException>
    {
        private final Output output;

        AnyCharacterExceptFormatter( Output output )
        {
            this.output = output;
        }

        @Override
        public CharacterSet.ExclusionVisitor<RuntimeException> visitSet( String name )
        {
            output.append( "~[" );
            return this;
        }

        @Override
        public void visitCodePoint( int cp )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void excludeCodePoint( int cp )
        {
            codePoint( output, cp );
        }

        @Override
        public void excludeRange( int start, int end )
        {
            codePoint( output, start );
            output.append( '-' );
            codePoint( output, end );
        }

        @Override
        public void excludeSet( String name )
        {
            throw new UnsupportedOperationException( "ANY except a named set." );
        }

        @Override
        public void close()
        {
            output.append( "]" );
        }
    }

    private static class SetFormatter implements CharacterSet.DefinitionVisitor<RuntimeException>
    {
        private final Output output;

        SetFormatter( Output output )
        {
            this.output = output;
        }

        @Override
        public void visitCodePoint( int cp )
        {
            if ( cp <= Character.MAX_VALUE )
            {
                codePoint( output, cp );
            }
        }

        @Override
        public void visitRange( int start, int end )
        {
            // Antlr only supports unicode literals up to \uFFFF
            if ( end <= Character.MAX_VALUE )
            {
                codePoint( output, start );
                output.append( '-' );
                codePoint( output, end );
            }
            else if ( start <= Character.MAX_VALUE )
            {
                codePoint( output, start );
                output.append( '-' );
                // truncate the range
                codePoint( output, Character.MAX_VALUE );
            }
            // just skip larger values
        }
    }

    private static void codePoint( Output output, int cp )
    {
        switch ( cp )
        {// <pre>
            case '\r': output.append("\\r");  break;
            case '\n': output.append("\\n");  break;
            case '\t': output.append("\\t");  break;
            case '\b': output.append("\\b");  break;
            case '\f': output.append("\\f");  break;
            case '\\': output.append("\\\\"); break;
            case '-':  output.append("\\-");  break;
            case ']':  output.append("\\]");  break;
        // </pre>
        default:
            if ( ' ' <= cp && cp <= '~' )
            {
                output.appendCodePoint( cp );
            }
            else
            {
                output.format( "\\u%04X", cp );
            }
        }
    }
}
