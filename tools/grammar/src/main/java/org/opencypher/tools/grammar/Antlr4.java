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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.v4.Tool;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.tool.ANTLRMessage;
import org.antlr.v4.tool.ANTLRToolListener;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarRootAST;
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
public class Antlr4 extends BnfWriter {
    static String PREFIX = "oC_";

    private static String prefix = PREFIX;

    public static void setPrefix(String newPrefix) {
        prefix = newPrefix;
    }

    public static void resetPrefix() {
        prefix = PREFIX;
    }

    public static void write(Grammar grammar, Writer writer) {
        write(grammar, output(writer));
    }

    public static void write(Grammar grammar, OutputStream stream) {
        write(grammar, output(stream));
    }

    public static void write( Grammar grammar, Path path, OutputStream stream )
    {
        write( grammar, stream );
    }

    public static void write( Grammar grammar, Output output )
    {
        try ( Antlr4 antlr = new Antlr4( ProductionMappingListener.NONE, output ) )
        {
            antlr.write( grammar );
        }
    }

    private void write( Grammar grammar )
    {
        String header = grammar.header();
        if ( header != null )
        {
            output.println( "/**" ).append( " * " ).printLines( header, " * " ).println( " */" );
        }
        output.append( "grammar " ).append( unspaceString( grammar.language() ) ).println( ";" ).println();

        grammar.accept( this );
    }

    public static void main(String... args) throws Exception {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        int index = argsList.indexOf("-o");
        String g4OutputFilePath = null;
        if (index >= 0) {
            g4OutputFilePath = argsList.remove(index + 1);
            argsList.remove(index);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        execute( ( grammar, workingDir, stream ) -> write( grammar, stream ), out, "grammar/cypher.xml");

        if (g4OutputFilePath == null) {
            System.out.print(out.toString(UTF_8.name()));
        } else {
            Files.write(Paths.get(g4OutputFilePath), out.toByteArray());
            System.out.println("Wrote output grammar to " + g4OutputFilePath);

        }
    }

    public static Parser generateParser( Grammar grammar, String root, Output output )
    {
        return ParserGenerator.generateParser( grammar, root, output );
    }

    private static class ParserGenerator
    {
        static Parser generateParser( Grammar grammar, String root, Output output )
        {
            Output.Readable source = Output.stringBuilder();
            org.antlr.v4.Tool tool = new org.antlr.v4.Tool();
            AntlrMessageLogger messages = new AntlrMessageLogger( tool, output );
            try ( Antlr4 antlr = new Antlr4( messages, source ) )
            {
                antlr.write( grammar );
            }
            ANTLRReaderStream generatorInput;
            try
            {
                generatorInput = new ANTLRReaderStream( source.reader() );
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Failed to create reader from buffer." );
            }
            GrammarRootAST ast = tool.parse( grammar.language(), generatorInput );
            org.antlr.v4.tool.Grammar g = tool.createGrammar( ast );
            tool.process( g, false );
            messages.report( source, grammar );
            String rootRuleName = ruleName( root );
            Rule rootRule = g.getRule( rootRuleName );
            if ( rootRule == null )
            {
                throw new IllegalArgumentException(
                        "The generated parser does not define a rule for '" + root + "' (it should have been called '" + rootRuleName + "' by the parser)." );
            }
            int rootRuleIndex = rootRule.index;
            return input ->
            {
                LexerInterpreter lexer = g.createLexerInterpreter( CharStreams.fromString( input ) );
                ParserInterpreter parser = g.createParserInterpreter( new CommonTokenStream( lexer ) );
                ParseTree tree = parser.parse( rootRuleIndex );
                return new Antlr4Tree( tree );
            };
        }
    }

    private static class AntlrMessageLogger implements ProductionMappingListener, ANTLRToolListener
    {
        private final Output output;
        private final ErrorManager errMgr;
        private boolean errors;
        private final Map<String, Production> productions = new HashMap<>();
        private final List<Message> messages = new ArrayList<>();

        AntlrMessageLogger( Tool tool, Output output )
        {
            this.errMgr = tool.errMgr;
            this.output = output;
            tool.addListener( this );
        }

        @Override
        public void map( String name, Production production )
        {
            productions.put( name, production );
        }

        @Override
        public void info( String msg )
        {
            output.format( "ANTLR Parser Generator: %s%n", msg );
        }

        @Override
        public void error( ANTLRMessage msg )
        {
            errors = true;
            messages.add( new Message( true, msg ) );
        }

        @Override
        public void warning( ANTLRMessage msg )
        {
            messages.add( new Message( false, msg ) );
        }

        public void report( Output.Readable source, Grammar grammar )
        {
            if ( !messages.isEmpty() )
            {
                messages.sort( Comparator.comparingInt( ( Message msg ) -> msg.message.line ).thenComparing( ( Message msg ) -> msg.message.charPosition ) );
                source.lines( new BiConsumer<>()
                {
                    Iterator<Message> iterator = messages.iterator();
                    Message message = iterator.next();

                    @Override
                    public void accept( String line, Integer no )
                    {
                        output.format( "%7d: ", no ).println( line );
                        while ( message != null && message.message.line <= no )
                        {
                            if ( message.message.charPosition >= 0 )
                            {
                                output.append( message.error ? "ERROR:   " : "WARNING: " ).repeat( ' ', message.message.charPosition ).append( '^' ).println();
                            }
                            output.append( message.error ? "ERROR:   " : "WARNING: " ).println( errMgr.getMessageTemplate( message.message ).render() );
                            message = iterator.hasNext() ? iterator.next() : null;
                        }
                    }
                } );
                Map<String,List<Message>> productions = new HashMap<>();
                for ( Message message : messages )
                {
                    addProductions( productions, message, message.message.getArgs() );
                }
                for ( Map.Entry<String,List<Message>> entry : productions.entrySet() )
                {
                    output.println();
                    Production production;
                    try
                    {
                        production = grammar.production( entry.getKey() );
                    }
                    catch ( IllegalArgumentException noSuchProduction )
                    {
                        production = null;
                    }
                    if ( production != null )
                    {
                        ISO14977.append( production, output );
                    }
                    else
                    {
                        output.append( "Keyword \"" ).append( entry.getKey() ).println( "\"" );
                    }
                    for ( Message message : entry.getValue() )
                    {
                        output.println( errMgr.getMessageTemplate( message.message ).render() );
                    }
                    output.println();
                }
                if ( errors )
                {
                    throw new IllegalStateException(
                            "There were errors when generating the ANTLR parser. " + productions.size() + " productions in error: " + productions.keySet() );
                }
            }
        }

        private void addProductions( Map<String,List<Message>> productions, Message message, Object obj )
        {
            if ( obj instanceof String )
            {
                Production production = this.productions.get( obj );
                productions.computeIfAbsent( production != null ? production.name() : (String) obj, key -> new ArrayList<>() ).add( message );
            }
            else if ( obj instanceof Rule )
            {
                addProductions( productions, message, ((Rule) obj).name );
            }
            else if ( obj instanceof Object[] )
            {
                for ( Object object : (Object[]) obj )
                {
                    addProductions( productions, message, object );
                }
            }
            else if ( obj instanceof Collection<?> )
            {
                for ( Object object : (Collection<?>) obj )
                {
                    addProductions( productions, message, object );
                }
            }
        }

        static class Message
        {
            final boolean error;
            final ANTLRMessage message;

            Message( boolean error, ANTLRMessage message )
            {
                this.error = error;
                this.message = message;
            }
        }
    }

    private static class Antlr4Tree implements Parser.ParseTree
    {
        private final ParseTree tree;

        Antlr4Tree( ParseTree tree )
        {
            this.tree = tree;
        }
    }

    private static String ruleName( String rule )
    {
        return Antlr4.prefix + unspaceString( rule );
    }

    private final Map<String, CharacterSet> fragmentRules = new HashMap<>();
    private final Set<String> seenKeywords = new HashSet<>();
    /*
     * Mutable state -- this map keeps track of the keywords in the current
     * production, and is emptied when the production is exited.
     */
    private final Map<String, String> keywordsInProduction = new LinkedHashMap<>();
    private final ProductionMappingListener mappingListener;

    private Antlr4( ProductionMappingListener mappingListener, Output output )
    {
        super( output );
        this.mappingListener = mappingListener;
    }

    @Override
    public void close() {
        /*
         * This prints all the 'fragment' rules (rules that are used to construct lexer
         * tokens, and the only type of rule that can use the character set syntax [])
         * at the bottom of the grammar file.
         */
        // // now add the letter rules
        // if (seenKeywords.size() > 0) {
        // String[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        // for (String letter : alphabet) {
        // output.append("fragment ").append(letter).append(" : '").append(letter)
        // .append("' | '").append(letter.toLowerCase()).append("'");
        // output.println(" ;");
        // }
        // output.println();
        // }
        for (Map.Entry<String, CharacterSet> rule : fragmentRules.entrySet()) {
            CharacterSet set = rule.getValue();
            output.append("fragment ");
            lexerRule(rule.getKey());
            output.append(" : ");
            if (set.name() != null && !set.isControlCharacter()) {
                // antlr 4.7.2 is thought to accept all the names we know except the control
                // characters
                output.append("[\\p{").append(set.name()).append("}]");
            }
            //
            // // this test used not have the haveExclusions test -
            ////// but setting an exception changes the name of the set to null (since it
            // isn't the any set anymore)
            else if (CharacterSet.ANY.equals(set.name())) {
                set.accept(new AnyCharacterExceptFormatter(output));
            } else if (set.hasExclusions()) {
                set.accept(new AnyCharacterExceptFormatter(output));
            } else {
                output.append('[');
                set.accept(new SetFormatter(output));
                output.append(']');
            }
            output.println(" ;").println();
        }
    }

    @Override
    protected void productionCommentPrefix() {
        output.println("/**").append(" * ");
    }

    @Override
    protected void productionCommentLinePrefix() {
        output.append(" * ");
    }

    @Override
    protected void productionCommentSuffix() {
        output.println(" */");
    }

    private String currentProduction;
    private int nextLexerRule;

    @Override
    protected void productionStart(Production p) {
        currentProduction = p.name();
        String name;
        if (p.lexer()) {
            name = lexerRule(currentProduction);
        } else {
            name = parserRule(currentProduction);
            nextLexerRule = 0;
        }
        mappingListener.map( name, p );
        // we don't really want the : to line up with all the pipes (at least, I don't)
        alternativesLinePrefix(currentProduction.length());
        output.append(":  ");
        // output.append(" : ");
    }

    @Override
    protected void productionEnd() {
        output.println(" ;").println();

        /*
         * We print out lexer rules for all literal words mentioned in the production
         */
        for (Map.Entry<String, String> lexerRule : keywordsInProduction.entrySet()) {
            String ruleName = lexerRule.getKey();
            // Except the ones we've already seen!
            if (!seenKeywords.contains(ruleName)) {
                seenKeywords.add(ruleName);
                caseInsensitiveProductionStart(ruleName);
                inline( lexerRule.getValue() );
                output.println(" ;").println();
            }
        }
        keywordsInProduction.clear();

        currentProduction = null;
    }

    private void addFragmentRule(CharacterSet characters) {
        String rule = currentProduction + "_" + nextLexerRule++;
        fragmentRules.put(rule, characters);
        lexerRule(rule);
    }

    @Override
    protected void alternativesLinePrefix(int altPrefix) {
        if (altPrefix > 0) {
            output.println();
            while (altPrefix-- > 0) {
                output.append(' ');
            }
        }
    }

    @Override
    protected void alternativesSeparator() {
        output.append(" | ");
    }

    @Override
    protected void sequenceSeparator() {
        output.append(" ");
    }

    @Override
    protected void groupPrefix() {
        output.append("( ");
    }

    @Override
    protected void groupSuffix() {
        output.append(" )");
    }

    @Override
    protected boolean optionalPrefix() {
        return false;
    }

    @Override
    protected void optionalSuffix() {
        output.append("?");
    }

    @Override
    protected void repeat(int minTimes, Integer maxTimes, Runnable repeated) {
        if (maxTimes == null) {
            if (minTimes == 0) {
                groupWith('(', repeated, ')');
                output.append("*");
                return;
            } else if (minTimes == 1) {
                groupWith('(', repeated, ')');
                output.append("+");
                return;
            }
        } else if (maxTimes == 1 && minTimes == 0) {
            groupWith('(', repeated, ')');
            optionalSuffix();
            return;
        } else if (minTimes == maxTimes) {
            groupWith('(', () -> {
                for (int i = 0; i < minTimes; i++) {
                    if (i > 0) {
                        sequenceSeparator();
                    }
                    repeated.run();
                }
            }, ')');
            return;
        }
        throw new UnsupportedOperationException(
                format("The Antlr formatter does not support minTimes=%d, maxTimes=%s", minTimes, maxTimes));
    }

    @Override
    protected void characterSet(CharacterSet characters) {
        String setName = characters.name();
        if (setName == null) {
            addFragmentRule(characters);
        } else if (setName.equals(CharacterSet.EOI)) {
            output.append("EOF");
        } else {
            fragmentRules.put(setName, characters);
            lexerRule(setName);
        }
    }

    @Override
    protected void nonTerminal(NonTerminal nonTerminal) {
        if (nonTerminal.production().lexer()) {
            lexerRule(nonTerminal.productionName());
        } else {
            parserRule(nonTerminal.productionName());
        }
    }

    private static String unspaceString(String original) {
        return original.replaceAll("(\\s+|-|\\.)", "_");
    }

    private String parserRule( String name )
    {
        String handle = prefix( unspaceString( name ) );
        output.append( handle );
        return handle;
    }

    private String lexerRule( String original )
    {
        String name = unspaceString( original );
        int cp = name.codePointAt( 0 );
        if ( !isUpperCase( cp ) )
        {
            if ( name.codePoints().noneMatch( Character::isUpperCase ) )
            {
                name = name.toUpperCase();
            }
            else
            {
                name = new StringBuilder( name.length() ).appendCodePoint( toUpperCase( cp ) ).append( name, charCount( cp ), name.length() ).toString();
            }
        }
//        name = Antlr4.prefix.toUpperCase() + name;
        output.append( name );
        return name;
    }

    @Override
    protected String prefix(String s) {
        return Antlr4.prefix + s;
    }

    @Override
    protected void literal(String value) {
        escapeAndEnclose(value);
    }

    private boolean reserved(String ruleName) {
        return ruleName.equals("SKIP") || ruleName.equals("MORE");
    }

    private void inline(String value) {
        group(() -> {
            String sep = "";
            int start = 0;
            for (int i = 0, end = value.length(), cp; i < end; i += Character.charCount(cp)) {
                cp = value.charAt(i);
                if (Character.isLowerCase(cp) || Character.isUpperCase(cp) || Character.isTitleCase(cp)) {
                    if (start < i) {
                        output.append(sep);
                        sep = " ";
                        escapeAndEnclose(value.substring(start, i));
                    }
                    output.append(sep);
                    sep = " ";
                    start = i + Character.charCount(cp);
                    cp = Character.toUpperCase(cp);
                    String upper = String.valueOf((char) cp);
                    groupWith( '(', () ->
                    {
                        escapeAndEnclose( upper );
                        alternativesSeparator();
                        escapeAndEnclose( upper.toLowerCase() );
                    }, ')' );
                }
            }
            if (start < value.length()) {
                output.append(sep);
                escapeAndEnclose(value.substring(start));
            }
        });
    }

    @Override
    protected void caseInsensitive(String value) {
        if (value.length() == 1) {
            inline(value);
        } else {
            String lexerRule = value.toUpperCase();
            if (!Character.isLetter(value.codePointAt(0)) || reserved(lexerRule)) {
                lexerRule = "L_" + lexerRule;
            }
            output.append(lexerRule);
            keywordsInProduction.put(lexerRule, value);
        }
    }

    private void escapeAndEnclose(String value) {
        output.append("'").escape(value, Antlr4::escapes).append("'");
    }

    @Override
    protected void caseInsensitiveProductionStart(String name) {
        currentProduction = name;
        lexerRule( currentProduction );
        output.append( " : " );
        nextLexerRule = 0;
    }

    @Override
    protected void epsilon() {
    }

    private static String escapes(int cp) {
        switch (cp) { // <pre>
        case '\r':
            return "\\r";
        case '\n':
            return "\\n";
        case '\t':
            return "\\t";
        case '\b':
            return "\\b";
        case '\f':
            return "\\f";
        case '\'':
            return "\\'";
        case '\\':
            return "\\\\";
        default:
            // how mad will this get ? PRF
            if (cp >= 128) {
                String hex = Integer.toHexString(cp);
                return "\\u" + (hex.length() < 4 ? "00" : "") + Integer.toHexString(cp);
            }
            return null;
        } // </pre>
    }

    private static class AnyCharacterExceptFormatter
            implements CharacterSet.DefinitionVisitor.NamedSetVisitor<RuntimeException>,
            CharacterSet.ExclusionVisitor<RuntimeException> {
        private final Output output;

        AnyCharacterExceptFormatter(Output output) {
            this.output = output;
        }

        @Override
        public CharacterSet.ExclusionVisitor<RuntimeException> visitSet(String name) {
            output.append("~[");
            return this;
        }

        @Override
        public void visitCodePoint(int cp) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void excludeCodePoint(int cp) {
            codePoint(output, cp);
        }

        @Override
        public void excludeRange(int start, int end) {
            codePoint(output, start);
            output.append('-');
            codePoint(output, end);
        }

        @Override
        public void excludeSet(String name) {
            throw new UnsupportedOperationException("ANY except a named set.");
        }

        @Override
        public void close() {
            output.append("]");
        }
    }

    private static class SetFormatter implements CharacterSet.DefinitionVisitor<RuntimeException> {
        private final Output output;

        SetFormatter(Output output) {
            this.output = output;
        }

        @Override
        public void visitCodePoint(int cp) {
            if (cp <= Character.MAX_VALUE) {
                codePoint(output, cp);
            }
        }

        @Override
        public void visitRange(int start, int end) {
            // Antlr only supports unicode literals up to \uFFFF
            if (end <= Character.MAX_VALUE) {
                codePoint(output, start);
                output.append('-');
                codePoint(output, end);
            } else if (start <= Character.MAX_VALUE) {
                codePoint(output, start);
                output.append('-');
                // truncate the range
                codePoint(output, Character.MAX_VALUE);
            }
            // just skip larger values
        }
    }

    private static void codePoint(Output output, int cp) {
        switch (cp) {// <pre>
        case '\r':
            output.append("\\r");
            break;
        case '\n':
            output.append("\\n");
            break;
        case '\t':
            output.append("\\t");
            break;
        case '\b':
            output.append("\\b");
            break;
        case '\f':
            output.append("\\f");
            break;
        case '\\':
            output.append("\\\\");
            break;
        case '-':
            output.append("\\-");
            break;
        case ']':
            output.append("\\]");
            break;
        // </pre>
        default:
            if (' ' <= cp && cp <= '~') {
                output.appendCodePoint(cp);
            } else {
                output.format("\\u%04X", cp);
            }
        }
    }
}
