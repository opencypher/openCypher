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
package org.opencypher.demo.grammar;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InterpreterRuleContext;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.opencypher.tools.grammar.Antlr4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AntlrParserDemo
{
    private static final String QUERY = "MATCH (c:Label), (d:Label2) SET c.property = d.property WITH c MATCH (e:Label3 {name: c.name}) RETURN c";
    private static final String INDENT = "  ";
    private static final String GRAMMAR_XML = "/cypher.xml";
    private static final String GRAMMAR_G4 = "Cypher.g4";

    public static void main( String[] args )
    {
        System.out.println( "== With tree listener, grammar from XML ==" );
        new AntlrParserDemo( true, true, QUERY, INDENT );
        System.out.println( "== With tree listener, grammar from g4 ==" );
        new AntlrParserDemo( true, false, QUERY, INDENT );
        System.out.println( "== Manual tree walking, grammar from XML ==" );
        new AntlrParserDemo( false, true, QUERY, INDENT );
        System.out.println( "== Manual tree walking, grammar from g4 ==" );
        new AntlrParserDemo( false, false, QUERY, INDENT );
    }

    public AntlrParserDemo( boolean useTreeListener, boolean createGrammarFromXML, String query, String indentStep )
    {
        org.antlr.v4.tool.Grammar grammar;
        if ( createGrammarFromXML )
        {
            grammar = createGrammarFromXML( GRAMMAR_XML );
        }
        else
        {
            grammar = readGrammarFromG4( GRAMMAR_G4 );
        }
        if ( grammar != null )
        {
            LexerInterpreter lexer = grammar.createLexerInterpreter( CharStreams.fromString( query ) );
            ParserInterpreter parser = grammar.createParserInterpreter( new CommonTokenStream( lexer ) );
            lexer.removeErrorListeners();
            parser.removeErrorListeners();
            lexer.addErrorListener( new FailingErrorListener( query ) );
            parser.addErrorListener( new FailingErrorListener( query ) );
            if ( useTreeListener )
            {
                parser.addParseListener( new MyParseTreeListener( grammar, INDENT ) );
            }
            ParseTree tree = parser.parse( grammar.getRule( "oC_Cypher" ).index );
            if ( !useTreeListener )
            {
                print( grammar, tree, INDENT, "" );
            }
        }
    }

    /*
     * Using a ParseTreeListener to consume the parse tree
     */
    private class MyParseTreeListener implements ParseTreeListener
    {
        private String indent = "";
        private final String indentStep;
        private final org.antlr.v4.tool.Grammar grammar;

        MyParseTreeListener( org.antlr.v4.tool.Grammar grammar, String indentStep )
        {
            this.grammar = grammar;
            this.indentStep = indentStep;
        }

        @Override
        public void visitTerminal( TerminalNode node )
        {
            System.out.println( indent + indentStep + "\"" + node.getText() + "\"" );
        }

        @Override
        public void visitErrorNode( ErrorNode node )
        {

        }

        @Override
        public void enterEveryRule( ParserRuleContext ctx )
        {
            String ruleName = grammar.getRule( ctx.getRuleIndex() ).name;
            indent = indent + indentStep;
            System.out.println( indent + ruleName );
            //System.out.println("Entre:" + ctx.getText());
        }

        @Override
        public void exitEveryRule( ParserRuleContext ctx )
        {
            indent = indent.substring( 0, indent.length() - indentStep.length() );
        }
    }

    /*
     * Walking the parse tree manually
     */
    private void print( org.antlr.v4.tool.Grammar grammar, ParseTree tree, String indentStep, String indent )
    {
        if ( tree != null )
        {
            Object payload = tree.getPayload();
            if ( payload instanceof InterpreterRuleContext )
            {
                String ruleName = grammar.getRule( ((InterpreterRuleContext) payload).getRuleIndex() ).name;
                System.out.println( indent + ruleName );
            }
            else
            {
                System.out.println( indent + "\"" + tree.getText() + "\"" );
            }
            for ( int i = 0; i <= tree.getChildCount(); i++ )
            {
                print( grammar, tree.getChild( i ), indentStep, indent + indentStep );
            }
        }
    }

    /*
     * Grammar loading: Cypher.g4 -> org.antlr.v4.tool.Grammar instance
     */
    private Grammar readGrammarFromG4( String filename )
    {
        String grammarString;
        try
        {
            File g4File = new File( filename );
            if ( g4File.exists() )
            {
                grammarString = new String( Files.readAllBytes( g4File.toPath() ) );
                return parseGrammarFromString( grammarString );
            }
            else
            {
                System.out.println( filename + " not found." );
                return null;
            }
        }
        catch ( Throwable t )
        {
            fail( "Unexpected error while reading g4 grammar: " + t.getMessage() );
        }
        return null;
    }

    /*
     * Grammar loading: cypher.xml -> g4 grammar string -> org.antlr.v4.tool.Grammar instance
     */
    private Grammar createGrammarFromXML( String resource, org.opencypher.grammar.Grammar.ParserOption... options )
    {
        String grammarString = null;
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            org.opencypher.grammar.Grammar oCGrammar = org.opencypher.grammar.Grammar.parseXML( Paths.get( resourceURL( resource ) ), options );
            Antlr4.write( oCGrammar, out );
            grammarString = out.toString( UTF_8.name() );
        }
        catch ( Throwable t )
        {
            fail( "Unexpected error while writing antlr grammar: " + t.getMessage() );
        }
        return parseGrammarFromString( grammarString );
    }

    private Grammar parseGrammarFromString( String grammarString )
    {
        org.antlr.v4.Tool tool = new org.antlr.v4.Tool();
        GrammarRootAST ast = tool.parseGrammarFromString( grammarString );
        Grammar grammar = tool.createGrammar( ast );
        tool.process( grammar, false );
        return grammar;
    }

    private URI resourceURL( String resource ) throws URISyntaxException
    {
        URL url = this.getClass().getResource( resource );
        if ( url == null )
        {
            throw new IllegalArgumentException( "No such resource: " + resource );
        }
        return url.toURI();
    }

    /*
     * ANTLR error listener
     */
    private class FailingErrorListener implements ANTLRErrorListener
    {
        private final String query;

        FailingErrorListener( String query )
        {
            this.query = query;
        }

        @Override
        public void syntaxError( Recognizer<?,?> recognizer, Object o, int line, int charPositionInLine, String msg, RecognitionException e )
        {
            fail( "syntax error in query at line " + line + ":" + charPositionInLine + ": " + msg + ": " + query );
        }

        @Override
        public void reportAmbiguity( Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet,
                                     ATNConfigSet atnConfigSet )
        {
            fail( "ambiguity in query: " + query );
        }

        @Override
        public void reportAttemptingFullContext( Parser parser, DFA dfa, int i, int i1, BitSet bitSet,
                                                 ATNConfigSet atnConfigSet )
        {
//            System.err.println( "attempting full context due to SLL conflict in query: " + query );
        }

        @Override
        public void reportContextSensitivity( Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet )
        {
//            System.err.println( "context sensitivity in query: " + query );
        }
    }

    private void fail( String msg )
    {
        throw new RuntimeException( msg );
    }
}
