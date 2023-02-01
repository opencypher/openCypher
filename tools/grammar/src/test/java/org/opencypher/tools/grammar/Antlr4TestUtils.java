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
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.opencypher.grammar.Fixture;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.Assert.fail;
import static org.opencypher.grammar.Grammar.ParserOption.INCLUDE_LEGACY;

public class Antlr4TestUtils
{
    private static org.antlr.v4.tool.Grammar g = null;
    private static org.antlr.v4.tool.Grammar legacyGrammar = null;

    public static void parse( String query )
    {
        initGrammar( "/cypher.xml" );
        parse( g, query );
    }

    static void parseLegacyWithListeners( String query, ANTLRErrorListener lexerListener, ANTLRErrorListener parserListener )
    {
        System.setProperty( INCLUDE_LEGACY.name(), "true" );

        initLegacyGrammar( "/cypher.xml" );
        parseWithListeners( legacyGrammar, query, lexerListener, parserListener );
    }

    static void parseLegacy( String query )
    {
        System.setProperty( INCLUDE_LEGACY.name(), "true" );

        initLegacyGrammar( "/cypher.xml" );
        parse( legacyGrammar, query );
    }

    private static void initLegacyGrammar( String resource )
    {
        if ( legacyGrammar == null)
        {
            legacyGrammar = createGrammar( resource, INCLUDE_LEGACY );
        }
    }

    private static void initGrammar( String resource )
    {
        if ( g == null)
        {
            g = createGrammar( resource );
        }
    }

    private static Grammar createGrammar( String resource, org.opencypher.grammar.Grammar.ParserOption... options )
    {
        // We need to do some custom post-processing to get the lexer rules right
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String grammarString = null;
        try
        {
            Antlr4.write( Fixture.grammarResource( Antlr4.class, resource, options ), out );
            grammarString = out.toString( UTF_8.name() );
            //System.out.println(grammarString);
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            fail( "Unexpected error while writing antlr grammar" );
        }

        org.antlr.v4.Tool tool = new org.antlr.v4.Tool();

        GrammarRootAST ast = tool.parseGrammarFromString( grammarString );
        Grammar grammar = tool.createGrammar( ast );
        tool.process( grammar, false );
        return grammar;
    }

    private static void parse( Grammar grammar, String query )
    {
        parseWithListeners( grammar, query,
                new FailingErrorListener( query ),
                new FailingErrorListener( query ) );
    }

    private static void parseWithListeners( Grammar grammar, String query, ANTLRErrorListener lexerListener, ANTLRErrorListener parserListener )
    {
        LexerInterpreter lexer = grammar.createLexerInterpreter( CharStreams.fromString( query ) );
        ParserInterpreter parser = grammar.createParserInterpreter( new CommonTokenStream( lexer ) );
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener( lexerListener );
        parser.addErrorListener( parserListener );
        parser.parse( grammar.getRule( Antlr4.PREFIX + "Cypher" ).index );
    }

    private static class FailingErrorListener implements ANTLRErrorListener
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
            // We're fine with context sensitivity, right?
//            System.err.println( "context sensitivity in query: " + query );
        }
    }

}
