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

import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

import static java.lang.Character.charCount;
import static java.lang.Character.codePointAt;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.isWhitespace;

@Element(uri = Grammar.XML_NAMESPACE, name = "description")
class Description
{
    private final StringBuilder text = new StringBuilder();

    @Child
    final void text( char[] buffer, int start, int length )
    {
        extract( text, buffer, start, length );
    }

    static void extract( StringBuilder target, char[] buffer, int start, int length )
    {
        int cp;
        while ( isWhitespace( cp = codePointAt( buffer, start ) ) )
        {
            int chars = charCount( cp );
            start += chars;
            length -= chars;
        }
        while ( length > 0 &&
                (isLowSurrogate( buffer[start + length - 1] )
                 ? isWhitespace( cp = codePointAt( buffer, start + length - 2 ) )
                 : isWhitespace( cp = buffer[start + length - 1] )) )
        {
            length -= charCount( cp );
        }
        if ( length > 0 )
        {
            if ( target.length() > 0 )
            {
                target.append( '\n' );
            }
            target.append( buffer, start, length );
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
}
