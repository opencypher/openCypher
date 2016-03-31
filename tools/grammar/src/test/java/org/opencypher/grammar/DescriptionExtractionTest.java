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
