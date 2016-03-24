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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.tools.output.Output;

public class Antlr4Massager
{

    // Run this test to print an Antlr grammar that parses Cypher correctly

    @Test
    @Ignore
    public void printCypherGrammar() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        Antlr4.write( Fixture.grammarResource( Antlr4.class, "/cypher.xml" ), Output.output( sb ) );

        System.out.println( postProcess( sb.toString() ) );
    }

    private String postProcess( String original )
    {
        int startOfKeywords = original.indexOf(
                "CYPHER : ( 'C' | 'c' ) ( 'Y' | 'y' ) ( 'P' | 'p' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' )  ;" );
        int endOfKeywords = original.indexOf( "fragment" );

        String everythingAfterKeywords = original.substring( endOfKeywords );

        String allTheKeyWords = original.substring( startOfKeywords, endOfKeywords );

        List<String> keywords = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader( new StringReader( allTheKeyWords ) ) )
        {
            String s;
            while ( (s = reader.readLine()) != null )
            {
                keywords.add( s.substring( 0, s.indexOf( " : " ) ) );
                reader.readLine(); // skip empty line
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "There was some error when reordering lexer rules", e );
        }

        int justBeforeThis = original.indexOf( "UnescapedSymbolicNameString : IdentifierStart ( IdentifierPart )* ;" );

        String firstPart = original.substring( 0, justBeforeThis );

        String betweenStartOfLexingAndKeywords = original.substring( justBeforeThis, startOfKeywords );

        return addAllKeywordsToSymbolicNameString(
                firstPart + allTheKeyWords + betweenStartOfLexingAndKeywords + everythingAfterKeywords, keywords );
    }

    private String addAllKeywordsToSymbolicNameString( String original, List<String> keywords )
    {
        String symbolicNameStringRule = "symbolicNameString : UnescapedSymbolicNameString\n" +
                                        "                   | EscapedSymbolicNameString";
        int symbolicNameStringPos = original.indexOf( symbolicNameStringRule );

        String firstPart = original.substring( 0, symbolicNameStringPos + symbolicNameStringRule.length() );

        StringBuilder builder = new StringBuilder( firstPart );
        for ( String keyword : keywords )
        {
            builder.append( "\n                   | " )
                    .append( keyword );
        }

        return builder.toString() + original.substring( symbolicNameStringPos + symbolicNameStringRule.length() );
    }

}
