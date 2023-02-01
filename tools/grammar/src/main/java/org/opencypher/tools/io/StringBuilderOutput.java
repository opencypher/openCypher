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
package org.opencypher.tools.io;

import java.io.Reader;
import java.util.stream.IntStream;

class StringBuilderOutput extends FormattingOutput<StringBuilder> implements Output.Readable
{
    StringBuilderOutput( StringBuilder output )
    {
        super( output );
    }

    @Override
    public int length()
    {
        return output.length();
    }

    @Override
    public char charAt( int index )
    {
        return output.charAt( index );
    }

    @Override
    public int codePointAt( int index )
    {
        return output.codePointAt( index );
    }

    @Override
    public Reader reader()
    {
        return new CharSequenceReader( output, 0, output.length() );
    }

    @Override
    public CharSequence subSequence( int start, int end )
    {
        return output.subSequence( start, end );
    }

    @Override
    public IntStream chars()
    {
        return output.chars();
    }

    @Override
    public IntStream codePoints()
    {
        return output.codePoints();
    }

    @Override
    public String toString()
    {
        return output.toString();
    }

    @Override
    public Output append( char c )
    {
        output.append( c );
        return this;
    }

    @Override
    public Output append( String str )
    {
        output.append( str );
        return this;
    }

    @Override
    public Output append( CharSequence s )
    {
        output.append( s );
        return this;
    }

    @Override
    public Output append( CharSequence s, int start, int end )
    {
        output.append( s, start, end );
        return this;
    }

    @Override
    public Output append( char[] str )
    {
        output.append( str );
        return this;
    }

    @Override
    public Output append( char[] str, int offset, int len )
    {
        output.append( str, offset, len );
        return this;
    }

    @Override
    public Output append( boolean b )
    {
        output.append( b );
        return this;
    }

    @Override
    public Output append( int i )
    {
        output.append( i );
        return this;
    }

    @Override
    public Output append( long lng )
    {
        output.append( lng );
        return this;
    }

    @Override
    public Output append( float f )
    {
        output.append( f );
        return this;
    }

    @Override
    public Output append( double d )
    {
        output.append( d );
        return this;
    }

    @Override
    public Output appendCodePoint( int codePoint )
    {
        output.appendCodePoint( codePoint );
        return this;
    }
}
