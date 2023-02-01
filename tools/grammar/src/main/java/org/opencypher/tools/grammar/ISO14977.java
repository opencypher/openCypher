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

import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Production;
import org.opencypher.tools.io.HtmlTag;
import org.opencypher.tools.io.Output;

import static org.opencypher.tools.io.HtmlTag.attr;
import static org.opencypher.tools.io.Output.output;

/**
 * Generates an EBNF grammar according to the <a href="https://www.cl.cam.ac.uk/~mgk25/iso-14977.pdf">ISO14977</a>
 * notation specification.
 */
public class ISO14977 extends BnfWriter
{
    public static void write( Grammar grammar, Writer writer )
    {
        write( grammar, output( writer ) );
    }

    public static void write( Grammar grammar, Path workingDir, OutputStream stream )
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
        try ( ISO14977 writer = new ISO14977( output ) )
        {
            grammar.accept( writer );
        }
    }

    public static void main( String... args ) throws Exception
    {
        Main.execute( ISO14977::write, args );
    }

    public static void append( Grammar.Term term, Output output )
    {
        term.accept( new ISO14977( output ) );
    }

    public static void append( Production production, Output output )
    {
        new ISO14977( output )
        {
            @Override
            protected void productionEnd()
            {
                output.println( " ;" );
            }
        }.visitProduction( production );
    }

    public static Output string( Output str, Production production )
    {
        return str.append( production.definition(), ISO14977::append );
    }

    public static void html( HtmlTag parent, Production production, HtmlLinker linker )
    {
        try ( HtmlTag pre = parent.tag( "pre" );
              HtmlTag code = pre.tag( "code" ) )
        {
            new ISO14977.Html( code, linker ).visitProduction( production );
        }
    }

    private ISO14977( Output output )
    {
        super( output );
    }

    private static class Html extends ISO14977
    {
        private final HtmlTag html;
        private final HtmlLinker linker;

        Html( HtmlTag html, HtmlLinker linker )
        {
            super( html.textOutput() );
            this.html = html;
            this.linker = linker;
        }

        @Override
        protected void nonTerminal( NonTerminal nonTerminal )
        {
            try ( HtmlTag ignored = link( linker.referenceLink( nonTerminal ) ) )
            {
                super.nonTerminal( nonTerminal );
            }
        }

        @Override
        protected void characterSet( CharacterSet characters )
        {
            try ( HtmlTag ignored = link( linker.charsetLink( characters ) ) )
            {
                super.characterSet( characters );
            }
        }

        @Override
        protected void literal( String value )
        {
            try ( HtmlTag ignored = link( literalLink( value ) ) )
            {
                super.literal( value );
            }
        }

        @Override
        void appendCaseChar( int cp )
        {
            int lo = Character.toLowerCase( cp ), up = Character.toUpperCase( cp ), title = Character.toTitleCase( cp );
            Output.Readable link = Output.stringBuilder();
            link.append( '[' ).format( "[\\u%04X]", lo );
            if ( up != lo )
            {
                link.format( "[\\u%04X]", up );
            }
            if ( title != up && title != lo )
            {
                link.format( "[\\u%04X]", title );
            }
            try ( HtmlTag ignored = link( linker.charsetLink( link.append( ']' ).toString() ) ) )
            {
                super.appendCaseChar( cp );
            }
        }

        private HtmlTag link( String target )
        {
            return target == null ? null : html.tag( "a", attr( "href", target ) );
        }

        private String literalLink( String literal )
        {
            int cp;
            if ( !literal.isEmpty() && literal.length() == Character.charCount( cp = literal.codePointAt( 0 ) ) )
            {
                return linker.charsetLink( String.format( "[\\u%04X]", cp ) );
            }
            return null;
        }

        @Override
        protected void productionEnd()
        {
            output.println( " ;" );
        }
    }

    @Override
    protected void productionCommentPrefix()
    {
        output.append( "(* " );
    }

    @Override
    protected void productionCommentLinePrefix()
    {
        output.append( " * " );
    }

    @Override
    protected void productionCommentSuffix()
    {
        output.append( " *)" );
    }

    @Override
    protected void productionStart( Production p )
    {
        output.append( p.name() ).append( " = " );
    }

    @Override
    protected String prefix( String s )
    {
        return s;
    }

    @Override
    protected void productionEnd()
    {
        output.println( " ;" ).println();
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
        output.append( ", " );
    }

    @Override
    protected void literal( String value )
    {
        enclose( value );
    }

    @Override
    protected void caseInsensitive( String value )
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
                        sep = ",";
                        enclose( value.substring( start, i ) );
                    }
                    output.append( sep );
                    sep = ",";
                    start = i + Character.charCount( cp );
                    cp = Character.toUpperCase( cp );
                    addCaseChar( cp );
                    appendCaseChar( cp );
                }
            }
            if ( start < value.length() )
            {
                output.append( sep );
                enclose( value.substring( start ) );
            }
        } );
    }

    void appendCaseChar( int cp )
    {
        output.appendCodePoint( cp );
    }

    private void enclose( String value )
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
            final int _sq = sq;
            group( () -> encloseGroupElements( value, enclose, _sq, other ) );
            return;
        }
        output.append( enclose ).append( value ).append( enclose );
    }

    private void encloseGroupElements( String value, char enclose, int sq, char other )
    {
        int start = 0;
        for ( int end = sq; end != -1; start = end, end = value.indexOf( enclose, end + 1 ) )
        {
            output.append( enclose ).append( value.subSequence( start, end ) ).append( enclose ).append( ", " );
            char last = enclose;
            enclose = other;
            other = last;
        }
        output.append( enclose ).append( value.subSequence( start, value.length() ) ).append( enclose );
    }

    @Override
    protected void caseInsensitiveProductionStart( String name )
    {
        output.append( name ).append( " = " );
    }

    @Override
    protected void epsilon()
    {
    }

    @Override
    protected void characterSet( CharacterSet characters )
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
                        public void excludeSet( String name )
                        {
                            output.append( sep ).append( name );
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
    protected void nonTerminal( NonTerminal nonTerminal )
    {
        output.append( nonTerminal.productionName() );
    }

    @Override
    protected boolean optionalPrefix()
    {
        output.append( "[" );
        return true;
    }

    @Override
    protected void optionalSuffix()
    {
        output.append( "]" );
    }

    @Override
    protected void repeat( int minTimes, Integer maxTimes, Runnable repeated )
    {
        if ( maxTimes == null )
        {
            if ( minTimes == 0 || minTimes == 1 )
            {
                groupWith( '{', repeated, '}' );
                if ( minTimes == 1 )
                {
                    output.append( '-' );
                }
            }
            else
            {
                group( () -> {
                    output.append( minTimes ).append( " * " );
                    repeated.run();
                    output.append( ", " );
                    groupWith( '{', repeated, '}' );
                } );
            }
        }
        else if ( minTimes == maxTimes )
        {
            output.append( minTimes ).append( " * " );
            groupWithoutPrefix( repeated );
        }
        else if ( minTimes > 0 )
        {
            group( () -> {
                output.append( minTimes ).append( " * " );
                repeated.run();
                output.append( ", " );
                output.append( maxTimes - minTimes ).append( " * " );
                groupWith( '[', repeated, ']' );
            } );
        }
        else
        {
            output.append( maxTimes - minTimes ).append( " * " );
            groupWith( '[', repeated, ']' );
        }
    }

    @Override
    protected void groupPrefix()
    {
        output.append( '(' );
    }

    @Override
    protected void groupSuffix()
    {
        output.append( ')' );
    }
}
