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

import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

import static java.lang.Character.charCount;
import static java.lang.Character.codePointAt;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;

@Element(uri = Grammar.XML_NAMESPACE, name = "description")
class Description
{
    private final StringBuilder text = new StringBuilder();

    @Child
    final void text( char[] buffer, int start, int length )
    {
        extract( text, buffer, start, length );
    }

    static void extract( StringBuilder target, char[] buffer, int origin, int length )
    {
        //  this method won't work if the buffer has \r in it, other than in the \r\n combination as in
        //  Windows lineSeparator
        
        String NL = System.lineSeparator();
        int end = origin + length;
        int start = findStart( buffer, origin, end );
        if ( start >= end ) // all whitespace
        {
            return;
        }
        // trim whitespace from the end
        for ( int cp; end > start &&
                      (isLowSurrogate( buffer[end - 1] )
                       ? isWhitespace( cp = codePointAt( buffer, end - 2 ) )
                       : isWhitespace( cp = buffer[end - 1] )); )
        {
            end -= charCount( cp );
        }
        // If the first line starts at the origin, and is not preceded by whitespace,
        // it is excluded from the indentation detection by emitting it immediately.
        if ( start == origin && !isWhitespace( codePointAt( buffer, start ) ) )
        {
            // if there is already stuff in the target, and it doesn't end with newline, add newline
            if ( target.length() > 0 && target.charAt( target.length() - 1 ) != '\n' )
            {
                target.append( NL );
            }
            start = emitLine( target, buffer, start, end, 0 );
            if ( start >= end )
            {
                return;
            }
            target.append( NL );
            // emit blank lines
            for ( int pos = start, cp; pos < end; pos += charCount( cp ) )
            {
                cp = codePointAt( buffer, pos );
                if ( cp == '\n' )
                {
                    target.append( NL );
                    start = pos + 1;
                }
                else if ( !isWhitespace( cp ) )
                {
                    break;
                }
            }
        }
        // find the shortest indentation (sequence of same WS char as the first) of any of the (remaining) lines
        int indentation = shortestIndentation( buffer, start, end );
        // emit all lines
        // if there is already stuff in the target, and it doesn't end with newline, add newline
        if ( target.length() > 0 && target.charAt( target.length() - 1 ) != '\n' )
        {
            target.append( NL );
        }
        for ( int ln = target.length() > 0 ? 2 : 0; start < end; ln++ )
        {
            if ( ln == 1 )
            {
                target.append( NL );
            }
            start = emitLine( target, buffer, start, end, indentation );
            if ( ln > 0 )
            {
                target.append( NL );
            }
        }
    }

    @Override
    public String toString()
    {
        return text.toString();
    }

    String appendTo( String text )
    {
        if ( this.text.length() == 0 )
        {
            return text;
        }
        else if ( text.length() > 0 )
        {
            this.text.insert( 0, text ).insert( text.length(), '\n' );
        }
        return toString();
    }

    /**
     * Emits the line that starts at {@code start} position, and ends at a newline or the {@code end} position.
     *
     * @return the start position of the next line (the {@code end} position
     */
    private static int emitLine( StringBuilder target, char[] buffer, int start, int end, int indentation )
    {
        int last = start;
        for ( int pos = start, cp; pos < end; pos += charCount( cp ) )
        {
            cp = codePointAt( buffer, pos );
            if ( cp == '\n' )
            {
                end = pos + 1;
            }
            if ( indentation > 0 )
            {
                if (cp == '\r') {
                    // next will be \n (we assume)
                    // since we are in the indentation, this must be a blank line
                    start = pos + 1;
                }
                if ( --indentation == 0 )
                {
                    start = pos + 1;
                }
                last = pos; 
            }
            else if ( !isWhitespace( cp ) )
            {
                last = pos + 1;
            }
        }
        if (last > start) {
            target.append( buffer, start, last - start );
        }
        return end;
    }

    /**
     * Finds the shortest indentation of any of the lines in the buffer, starting at {@code start}, ending at {@end}.
     */
    private static int shortestIndentation( char[] buffer, int start, int end )
    {
        // find the indenting character (so behaviour with mixed space and tab may not be what we expect)
        int indentCP = codePointAt( buffer, start );
        if ( indentCP != ' ' && indentCP != '\t' )
        {
            return 0;
        }
        int indentation = Integer.MAX_VALUE;
        for ( int pos = start, cp, cur = 0; pos < end; pos += charCount( cp ) )
        {
            cp = codePointAt( buffer, pos );
            if ( cp == indentCP )
            {
                cur++;
            }
            else if ( cp == '\n' )
            {
                cur = 0;
            }
            else if ( cp == '\r' )
            {
                cur = 0;
            }
            else
            {
                indentation = min( indentation, cur );
                cur = 0;
                while ( pos < end )
                {
                    cp = codePointAt( buffer, pos );
                    if ( cp == '\n' )
                    {
                        break;
                    }
                    pos += charCount( cp );
                }
            }
        }
        return indentation;
    }

    /**
     * Find the beginning of the first line that isn't all whitespace.
     */
    private static int findStart( char[] buffer, int start, int end )
    {
        int pos, cp;
        for ( pos = start; pos < end && isWhitespace( cp = codePointAt( buffer, pos ) ); pos += charCount( cp ) )
        {
            if ( cp == '\n' )
            {
                start = pos + 1;
            }
        }
        return pos >= end ? end : start;
    }
}
