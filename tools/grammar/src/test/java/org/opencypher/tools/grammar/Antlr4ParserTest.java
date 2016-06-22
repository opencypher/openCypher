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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
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
import org.junit.Test;
import org.opencypher.grammar.Fixture;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.Assert.fail;
import static org.opencypher.grammar.Grammar.ParserOption.INCLUDE_LEGACY;

public class Antlr4ParserTest
{
    private String query;

    @Test
    public void shouldParseValidCypher() throws Exception
    {
        org.antlr.v4.tool.Grammar g = getMassagedAntlrGrammar( "/cypher.xml" );

        parseAndRun( g, "/cypher.txt" );
    }

    @Test
    public void shouldParseLegacyCypher() throws Exception
    {
        System.setProperty( INCLUDE_LEGACY.name(), "true" );
        org.antlr.v4.tool.Grammar g = getMassagedAntlrGrammar( "/cypher.xml", INCLUDE_LEGACY );

        parseAndRun( g, "/cypher-legacy.txt" );
    }

    private void parseAndRun( Grammar g, String queryFile ) throws FileNotFoundException, URISyntaxException
    {
        for ( String q : getQueries( queryFile ) )
        {
            query = q;
            LexerInterpreter lexer = g.createLexerInterpreter( new ANTLRInputStream( query ) );
            ParserInterpreter parser = g.createParserInterpreter( new CommonTokenStream( lexer ) );
            lexer.removeErrorListeners();
            parser.removeErrorListeners();
            lexer.addErrorListener( new FailingErrorListener() );
            parser.addErrorListener( new FailingErrorListener() );
            parser.parse( g.getRule( "cypher" ).index );
        }
    }

    private List<String> getQueries( String queryFile ) throws FileNotFoundException, URISyntaxException
    {
        URL resource = getClass().getResource( queryFile );
        Scanner scanner = new Scanner( new FileReader( Paths.get( resource.toURI() ).toFile() ) );
        scanner.useDelimiter( "§\n" );
        ArrayList<String> queries = new ArrayList<>();
        while ( scanner.hasNext() )
        {
            queries.add( scanner.next() );
        }
        return queries;
    }

    private class FailingErrorListener implements ANTLRErrorListener
    {

        @Override
        public void syntaxError( Recognizer<?,?> recognizer, Object o, int i, int i1, String s, RecognitionException e )
        {
            fail( "syntax error in query: " + query );
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
            System.err.println( "attempting full context due to SLL conflict in query: " + query );
        }

        @Override
        public void reportContextSensitivity( Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet )
        {
            // We're fine with context sensitivity, right?
            System.err.println( "context sensitivity in query: " + query );
        }
    }

    private org.antlr.v4.tool.Grammar getMassagedAntlrGrammar( String resource, org.opencypher.grammar.Grammar.ParserOption... options ) throws IOException
    {

        // We need to do some custom post-processing to get the lexer rules right
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String grammarString = null;
        try
        {
            Antlr4.write( Fixture.grammarResource( Antlr4.class, resource, options ), out );
            grammarString = Antlr4Massager.postProcess( out.toString( UTF_8.name() ) );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            fail( "Unexpected error while writing antlr grammar" );
        }

        org.antlr.v4.Tool tool = new org.antlr.v4.Tool();

        GrammarRootAST ast = tool.parseGrammarFromString( grammarString );
        org.antlr.v4.tool.Grammar grammar = tool.createGrammar( ast );
        tool.process( grammar, false );
        return grammar;
    }

}
