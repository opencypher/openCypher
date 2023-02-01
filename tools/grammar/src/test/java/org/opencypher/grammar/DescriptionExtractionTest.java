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
package org.opencypher.grammar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.opencypher.tools.io.Output.lines;

public class DescriptionExtractionTest
{
    @Test
    public void shouldTrimWhitespace() throws Exception
    {
        assertEquals( "hello   world", extract( "   hello   world       " ) );
    }

    @Test
    public void shouldSkipAllWhitespace() throws Exception
    {
        assertEquals( "", extract( "         \t\n  \n\t" ) );
    }

    @Test
    public void shouldAlignLines() throws Exception
    {
        assertEquals(
                lines(
                        "Hello,",
                        "",
                        "This is a message.",
                        "    (the content is not important)",
                        "The important part is the indentation." ),
                extract( lines(
                        "",
                        "              ",
                        "    Hello,    ",
                        "",
                        "    This is a message.",
                        "        (the content is not important)",
                        "    The important part is the indentation.",
                        "",
                        "    " ) ) );
    }

    @Test
    public void shouldAlignToSecondLineIfFirstIsWithoutWhitespace() throws Exception
    {
        assertEquals(
                lines(
                        "Hello,",
                        "",
                        "This is a message.",
                        "    (the content is not important)",
                        "The important part is the indentation." ),
                extract( lines(
                        "Hello,    ",
                        "",
                        "    This is a message.                    ",
                        "        (the content is not important)    ",
                        "    The important part is the indentation." ) ) );
    }

    @Test
    public void shouldAlignToFirstLineIfPrecededByBlankLines() throws Exception
    {
        assertEquals(
                lines(
                        "Hello,",
                        "",
                        "    This is a message.",
                        "        (the content is not important)",
                        "    The important part is the indentation." ),
                extract( lines(
                        "          ",
                        "Hello,    ",
                        "",
                        "    This is a message.                    ",
                        "        (the content is not important)    ",
                        "    The important part is the indentation." ) ) );
    }

    static String extract( String... strings )
    {
        int length = 0;
        for ( String string : strings )
        {
            length = Math.max( length, string.length() );
        }
        char[] buffer = new char[length];
        StringBuilder result = new StringBuilder();
        for ( String string : strings )
        {
            int start = (length - string.length()) / 2;
            string.getChars( 0, string.length(), buffer, start );
            Description.extract( result, buffer, start, string.length() );
        }
        return result.toString();
    }
}
