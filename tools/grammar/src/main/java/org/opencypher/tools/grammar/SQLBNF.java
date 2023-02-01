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

import static org.opencypher.tools.io.HtmlTag.attr;
import static org.opencypher.tools.io.Output.output;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Production;
import org.opencypher.tools.g4tree.BnfSymbols;
import org.opencypher.tools.g4tree.BnfSymbols.Interleaver;
import org.opencypher.tools.io.HtmlTag;
import org.opencypher.tools.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a SQL BNF grammar according to ISO/IEC 9075-1 notation specification.
 */
public class SQLBNF extends BnfWriter
{
    private static final String DESCRIPTION_LINE_MARKER  = "// ";
    private static final String CHARACTER_SET_START = "$";
    private static final String CHARACTER_SET_END = "$";
    private static final String CODEPOINT_LIST_START = "[";
    private static final String CODEPOINT_LIST_END = "]";
    private static final String CHARACTER_SET_EXCEPT = "~";
    public static final String LETTER_SUFFIX = " case insensitive";
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLBNF.class.getName());

     public static void write( Grammar grammar, Writer writer )
    {
        write( grammar, output( writer ) );
    }

    public static void write( Grammar grammar, OutputStream stream )
    {
        write( grammar, output( stream ) );
    }

    public static void write( Grammar grammar, Path path, OutputStream stream )
    {
        write( grammar, stream );
    }

    public static void write( Grammar grammar, Output output )
    {
        String header = grammar.header();
        if ( header != null )
        {
            // header appears as quasi-comment, each line preceded by !!
            // an empty quasi-comment line is inserted before and after
            // and a blank line after
            // the empty and blank lines will not be in the parsed header
            output.println(DESCRIPTION_LINE_MARKER).append(DESCRIPTION_LINE_MARKER)
                  .printLines( header, DESCRIPTION_LINE_MARKER )
                  .println(DESCRIPTION_LINE_MARKER).println();
        }
        try ( SQLBNF writer = new SQLBNF( output ) )
        {
            grammar.accept( writer );
        }
    }

    public static void main( String... args ) throws Exception
    {
        Main.execute( ( grammar, workingDir, stream ) -> write( grammar, stream ), args );
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

    /*
     * Copied from Antlr4 writer 
     */
    private final Set<String> seenKeywords = new HashSet<>();
    /*
     * Mutable state -- this map keeps track of the keywords in the current production,
     * and is emptied when the production is exited.
     */
    private final Map<String, String> keywordsInProduction = new LinkedHashMap<>();
    /*
     * Mutable state -- if the production is all bnfsymbols, don't generate a new one
     * 
     */
    private boolean markedBnfRule = false;
    private BnfSymbols bnfSymbolFromRuleName;
    /**
     * character literals that need to be added (once)
     */
    private final Set<BnfSymbols> bnfSymbolsNeedingRules = new HashSet<>();
    private final Set<String>  existingRulesWithBnfSymbolNames = new HashSet<>();
    
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

    // descriptions before rules are just the lines as quasicomments. No interspersed blank lines
    //  (comments on the first rule will be confused with the header, so don't)
    // in parsing, there must be no blank lines after the description lines before the rule itself.
    //  there can be empty description lines
    @Override
    protected void productionCommentPrefix()
    {
        output.append(DESCRIPTION_LINE_MARKER);
    }

    @Override
    protected void productionCommentLinePrefix()
    {
        output.append( DESCRIPTION_LINE_MARKER);
    }

    @Override
    protected void productionCommentSuffix()
    {
//        output.append(DESCRIPTION_END ).println();
    }

    @Override
    protected void productionStart( Production p )
    {
        String ruleName = p.name();
        output.append("<").append( ruleName ).append( "> ::=");
        // indent the first line as much as the content of alternatives will be
        // (alternative marker is output as " | "
        alternativesLinePrefix(ruleName.length() + 3);
        // some jiggery-pokery to handle "escaping" bnf symbols by putting them in their own rules
        
        // need to distinguish original rules that have bnf names from ones planted in generated bnf
        //   using p.bnfsymbols() (via attribute in xml) catches some
        markedBnfRule = p.bnfsymbols();
        
        bnfSymbolFromRuleName = BnfSymbols.getByName(ruleName);
        LOGGER.debug("matching {} to {}", ruleName, bnfSymbolFromRuleName);
        if (bnfSymbolFromRuleName != null) {
            existingRulesWithBnfSymbolNames.add(ruleName);
        }
        // need to distinguish original rules that have bnf names from ones planted in generated bnf
        //   using p.bnfsymbols() (via attribute in xml) and perhaps some tweaking of the name

        
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
        bnfSymbolFromRuleName = null;
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
                String glue = "";
                for ( char c : keywordProduction.getValue().toCharArray() )
                {
                    addCaseChar(Character.toUpperCase(c));
                    // alternative style (can't work out what the indirect way of this is
                    output.append(glue).append("<").append(String.valueOf( c ).toUpperCase() ).append(">");
                    glue = " ";

                }
                output.println().println();
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
        output.append( " " );
    }

    @Override
    protected void literal( String value )
    {
        // if the rule is marked as pure bnfsymbols, just do it
        if (markedBnfRule) {
            output.append(value);
            return;
        }
        // if this is really "normal English text", just write it out
        if (value.startsWith("!! ")) {
            output.append(value);
            return;
        }
        // special case // as that is a bnf comment
        if (value.equals("//")) {
            output.append("\\u002F\\u002F");
            return;
        }
        
        // sqlbnf must escape bnf symbols by pushing them into a single production
        // if this happens to be one, don't mess with it
        if (bnfSymbolFromRuleName != null) {
            if (value.equals(bnfSymbolFromRuleName)) {
                LOGGER.warn("Production name {} for '{}'", bnfSymbolFromRuleName.getBnfName(), 
                        bnfSymbolFromRuleName.getActualCharacters() );
                   // this is the whole production, we hope
                output.append(value);
                return;
            } else {
                LOGGER.warn("Production name {} is that of expected bnf definition for '{}'", bnfSymbolFromRuleName.getBnfName(), 
                        bnfSymbolFromRuleName.getActualCharacters() + " but contains " + value );

            }
        }
        // if this contains bnf symbols, they will need replacing
        String sep = "";
        if (BnfSymbols.anyBnfSymbols(value)) {
            Interleaver interleaver = BnfSymbols.getInterleave(value);
            while (interleaver.hasNext()) {
                String text = interleaver.nextText();
                if (text.length() > 0) {
                    output.append(sep).append(text);
                    sep = " ";
                }
                BnfSymbols symbol = interleaver.nextSymbol();
                bnfSymbolsNeedingRules.add(symbol);
                output.append(sep).append("<").append(symbol.getBnfName()).append(">");
                sep = " ";
            }
            String text = interleaver.nextText();
            if (text.length() > 0) {
                output.append(sep).append(text);
            }
        } else {
             // could do this with a different interleaver
            char[] chars = value.toCharArray();
            for (char cp : chars) {
                if ( ' ' < cp && cp <= '~' )
                    // printable, excluding space
                {
                    output.appendCodePoint( cp );
                }
                else
                    
                    output.format( " \\u%04X ", (int) cp );
                }                
            }
    }

    @Override
    protected void caseInsensitive( String value )
    {
        LOGGER.debug("caseinsens {}", value);
        String keywordProduction =  value.toUpperCase() ;
        output.append("<").append( keywordProduction ).append(">");
        if ( value.length() == 1 ) {
            // we have a single letter keyword, so we don't want to make it here, but we must
            // make sure the letter is used
            addCaseChar(keywordProduction.codePointAt(0));
        } else {
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
        output.append(CHARACTER_SET_START);
        String name = characters.name();
        if ( name != null && !name.equals("ANY"))
        {
            output.append(name);
        }
        // according to the notes, we should be able to have (in xml)
        //   <character set="Lu"><except literal="X"/></character>
        //  but I don't think that parses in for xml (name doesn't seem to be set)
        //  so we will assume all exclusions are against any
        else
        {
            // can't work out the right way to get the closing ] on a non-exclusion set
            // since, in practice they are all exclude or include, cheat
            characters.accept( new CharacterSet.DefinitionVisitor.NamedSetVisitor<RuntimeException>()
            {
                String sep = CODEPOINT_LIST_START;
 
                @Override
                public void visitCodePoint( int cp )
                {
                    output.append(sep);
                    codePoint( cp);
                    sep = "";
                }
                
                @Override
                public CharacterSet.ExclusionVisitor<RuntimeException> visitSet( String name )
                {
                    output.append(CHARACTER_SET_EXCEPT);
                    return new CharacterSet.ExclusionVisitor<RuntimeException>()
                    {
                        String sep = CODEPOINT_LIST_START;

                        @Override
                        public void excludeCodePoint( int cp ) throws RuntimeException
                        {
                            output.append(sep);
                            codePoint( cp );
                            sep = "";
                        }
                        @Override
                        public void excludeSet( String name )
                        {
                            // this isn't supported anyway
                            throw new UnsupportedOperationException("can't do exclusion of set name");
                        }

                        @Override
                        public void close() throws RuntimeException
                        {
                            // this is the cheat
//                            if ( !sep.equals(CODEPOINT_LIST_START)) {
//                                output.append(CODEPOINT_LIST_END);
//                            }
                        }
                    };
                }


                
                private void codePoint( int cp )
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
                        // this probably won't parse
                        case '$':  output.append("\\$");  break;
                    // </pre>
                    default:
                        if ( ' ' < cp && cp <= '~' )
                            // printable, excluding space
                        {
                            output.appendCodePoint( cp );
                        }
                        else
                        {
                            output.format( "\\u%04X", cp );
                        }
                    }

//                    String controlChar = CharacterSet.controlCharName( cp );
//                    if ( controlChar != null )
//                    {
//                        output.append( controlChar );
//                    }
//                    else if ( cp == '\'' )
//                    {
//                        output.append( "\"'\"" );
//                    }
//                    else
//                    {
//                        output.append( '\'' ).appendCodePoint( cp ).append( '\'' );
//                    }
                }
            } );
            // list end probably ought to be in a close() but there isn't one
            output.append(CODEPOINT_LIST_END);
        }

        output.append(CHARACTER_SET_END);
    }

    @Override
    protected void nonTerminal( NonTerminal nonTerminal )
    {
        output.append("<").append( nonTerminal.productionName() ).append(">");
    }

    @Override
    protected boolean optionalPrefix()
    {
        output.append( "[ " );
        return true;
    }

    @Override
    protected void optionalSuffix()
    {
        output.append( " ]" );
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
        for (BnfSymbols lit : bnfSymbolsNeedingRules) {
            String name = lit.getBnfName();
            if (! existingRulesWithBnfSymbolNames.contains(name)) {
                output.append("<").append(name).append("> ::= ").append(lit.getBnfForm());
                output.println().println();
            }
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
