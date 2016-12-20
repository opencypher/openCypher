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

import static org.opencypher.grammar.Grammar.ParserOption.INCLUDE_LEGACY;

// TODO: we should try to make these rules more generic, to not depend on specific names or positions in the result file
class Antlr4Massager
{

    static String postProcess( String original )
    {
        String firstKeyword;
        if ( Boolean.parseBoolean( System.getProperty( INCLUDE_LEGACY.name() ) ) )
        {
            firstKeyword =
                    "CYPHER : ( 'C' | 'c' ) ( 'Y' | 'y' ) ( 'P' | 'p' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' )  ;";
        }
        else
        {
            firstKeyword = "UNION : ( 'U' | 'u' ) ( 'N' | 'n' ) ( 'I' | 'i' ) ( 'O' | 'o' ) ( 'N' | 'n' )  ;";
        }
        int startOfKeywords = original.indexOf( firstKeyword );
        int endOfKeywords = original.indexOf( "L_0X :" );

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

        int justBeforeThis = original.indexOf( "UnescapedSymbolicName : IdentifierStart ( IdentifierPart )* ;" );

        String firstPart = original.substring( 0, justBeforeThis );

        // Disallow keywords as function names
        String firstPartAmended = firstPart.replace( "functionName : symbolicName ;",
                "functionName : UnescapedSymbolicName\n             | EscapedSymbolicName\n             | COUNT ;");

        String betweenStartOfLexingAndKeywords = original.substring( justBeforeThis, startOfKeywords );

        return addAllKeywordsToSymbolicName(
                firstPartAmended + allTheKeyWords + betweenStartOfLexingAndKeywords + everythingAfterKeywords, keywords );
    }

    private static String addAllKeywordsToSymbolicName( String original, List<String> keywords )
    {
        String symbolicNameStringRule = "symbolicName : UnescapedSymbolicName\n" +
                                        "             | EscapedSymbolicName";
        int symbolicNameStringPos = original.indexOf( symbolicNameStringRule );

        String firstPart = original.substring( 0, symbolicNameStringPos + symbolicNameStringRule.length() );

        StringBuilder builder = new StringBuilder( firstPart );
        keywords.add( "HexLetter" );

        for ( String keyword : keywords )
        {
            builder.append( "\n             | " )
                   .append( keyword );
        }

        return builder.toString() + original.substring( symbolicNameStringPos + symbolicNameStringRule.length() );
    }

}
