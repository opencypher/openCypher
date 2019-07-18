/*
 * Copyright (c) 2015-2019 "Neo Technology,"
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Production;
import org.opencypher.tools.io.HtmlTag;
import org.opencypher.tools.io.Output;

import static org.opencypher.tools.io.Output.output;

/**
 * Generates a SQL BNF grammar according to ISO/IEC 9075-1 notation specification.
 */
public class SQLBNF extends BnfWriter
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
        try ( SQLBNF writer = new SQLBNF( output ) )
        {
            grammar.accept( writer );
        }
    }

    public static void main( String... args ) throws Exception
    {
        Main.execute( SQLBNF::write, args );
    }

    public static void append( Grammar.Term term, Output output )
    {
        term.accept( new SQLBNF( output ) );
    }

    public static Output string( Output str, Production production )
    {
        return str.append( production.definition(), SQLBNF::append );
    }

    public static void html( HtmlTag parent, Production production, HtmlLinker linker )
    {
        try ( HtmlTag pre = parent.tag( "pre" );
              HtmlTag code = pre.tag( "code" ) )
        {
            new SQLBNF.Html( code, linker ).visitProduction( production );
        }
    }

    public interface HtmlLinker
    {
        String referenceLink( NonTerminal reference );

        default String charsetLink( CharacterSet charset )
        {
            return charsetLink( CharacterSet.Unicode.toSetString( charset ) );
        }

        String charsetLink( String charset );
    }

    /*
     * Copied from Antlr4 writer 
     */
    private final Set<String> seenKeywords = new HashSet<>();
    /*
     * Mutable state -- this map keeps track of the keywords in the current production,
     * and is emptied when the production is exited.
     */
    private final Map<String, String> keywordsInProduction = new LinkedHashMap<>();

    private final Set<CharLit> characterLiterals = new HashSet<>();
    
    private SQLBNF( Output output )
    {
        super( output );
    }

    private static class Html extends SQLBNF
    {
        private final HtmlTag html;
        private final HtmlLinker linker;

        Html( HtmlTag html, HtmlLinker linker )
        {
            super( html.output() );
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
            return target == null ? null : html.tag( "a", href -> target );
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
        output.append("<").append( p.name() ).append( "> ::= " );
    }

    @Override
    protected String prefix( String s )
    {
        return s;
    }

    @Override
    protected void productionEnd()
    {
        output.println().println();
        /*
         * We print out productions for all literal words mentioned in the production
         */
        for ( Map.Entry<String,String> keywordProduction : keywordsInProduction.entrySet() )
        {
            String ruleName = keywordProduction.getKey();
            // Except the ones we've already seen!
            if ( !seenKeywords.contains( ruleName ) )
            {
                seenKeywords.add( ruleName );
                caseInsensitiveProductionStart( ruleName );
                for ( char c : keywordProduction.getValue().toCharArray() )
                {
                	addCaseChar(c);
                	// alternative style (can't work out what the indirect way of this is
                	output.append("<").append(String.valueOf( c ).toUpperCase() ).append("> ");

                }
                output.println( " ;" ).println();
            }
        }
        keywordsInProduction.clear();
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
        output.append( "  " );
    }

    @Override
    protected void literal( String value )
    {
    	// sqlbnf never writes explicit punctuation
    	CharLit lit = CharLit.getByValue(value);
    	if (lit == null) {
    		if (value.matches("[\\w\\d]+")) {
    			// we will do it explicitly
    			output.append(value);
    			return;
    		}
    		if (value.length() == 1 && value.charAt(0) >= 128) {
    			// looks like one unicode
        		String hex =  Integer.toHexString(value.charAt(0));
        		output.append("0x" + hex);
        		return;
            	
    		}

    		throw new IllegalStateException("Unknown character literal " + value + ".");
    	}
    	characterLiterals.add(lit);
    	output.append(lit.getSQLBNF());
//        enclose( value );
    }

    @Override
    protected void caseInsensitive( String value )
    {
    	// modified from Antlr4
        if ( value.length() == 1 )
        {
            inline( value );
        }
        else
        {
            String keywordProduction =  value.toUpperCase() ;
            output.append("<").append( keywordProduction ).append(">");
            keywordsInProduction.put( keywordProduction, value );
        }
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
                        enclose( value.substring( start, i ) );
                    }
                    output.append( sep );
                    sep = " ";
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
    	// sql bnf never uses quotes
    	output.append(value);
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
        output.append("<").append( name ).append( "> ::= " );
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
        output.append("<").append( nonTerminal.productionName() ).append(">");
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
        	if ( minTimes == 0) {
        		groupWith( '[', repeated, ']');
        	} else {
        		groupWith( '{', repeated, '}' );
        	}
        	output.append(" ...");
        } else {
        	// explicit maximum can't be shown directly. lets specify precisely and 
        	// worry about reversing sqlbnf to xml later
        	for (int i=0; i < minTimes; i++) {
            	groupWith( '{', repeated, '}' );
        	}
        	for (int i=minTimes; i < maxTimes; i++) {
            	groupWith( '[', repeated, ']' );
        	}
        }
       }

    @Override
    public void close()
    {
    	for (CharLit lit : characterLiterals) {
    		output.append(lit.getSQLBNF()).append(" ::= ").append(lit.getCharacters());
    		output.println().println();
		}
    	
        for ( int chr : caseChars )
        {
            int upper = Character.toUpperCase( chr );
            int lower = Character.toLowerCase( chr );
            int title = Character.toTitleCase( chr );
            caseInsensitiveProductionStart( String.valueOf( (char) upper ) );
            output.append((char) upper);
            alternativesSeparator();
            output.append((char) lower);
            if ( title != upper )
            {
                alternativesSeparator();
                output.append((char) title);
            }
            productionEnd();
        }
    }
    
    @Override
    protected void groupPrefix()
    {
        output.append( '{' );
    }

    @Override
    protected void groupSuffix()
    {
        output.append( '}' );
    }
}
