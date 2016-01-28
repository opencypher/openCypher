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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "literal")
final class LiteralNode extends Node implements Literal
{
    @Attribute
    String value;

    @Override
    public int length()
    {
        return value.length();
    }

    @Override
    public char charAt( int index )
    {
        return value.charAt( index );
    }

    @Override
    public int codePointAt( int index )
    {
        return value.codePointAt( index );
    }

    @Override
    public CharSequence subSequence( int start, int end )
    {
        return value.substring( start, end );
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public IntStream chars()
    {
        return value.chars();
    }

    @Override
    public IntStream codePoints()
    {
        return value.codePoints();
    }

    static void fromCharacters( char[] buffer, int start, int length, Consumer<? super LiteralNode> add )
    {
        int pos = start;
        for ( int end = start + length, step; pos < end; pos += step )
        {
            int cp = Character.codePointAt( buffer, pos );
            step = Character.charCount( cp );
            if ( Character.isWhitespace( cp ) )
            {
                if ( start != pos )
                {
                    literal( add, new String( buffer, start, pos - start ) );
                }
                start = pos + step;
            }
        }
        if ( start != pos )
        {
            literal( add, new String( buffer, start, pos - start ) );
        }
    }

    private static void literal( Consumer<? super LiteralNode> add, String literal )
    {
        add.accept( literal( literal ) );
    }

    @Override
    Node replaceWithVerified()
    {
        if ( value.length() == 1 )
        {
            if ( (value.charAt( 0 ) < 0x20 || value.charAt( 0 ) == 0x7F) )
            {
                return CharacterSetNode.codePoint( value.charAt( 0 ) );
            }
            else
            {
                return this;
            }
        }
        SequenceNode seq = null;
        int start = 0;
        for ( int i = 0, cp; i < value.length(); i += Character.charCount( cp ) )
        {
            cp = value.codePointAt( i );
            if ( cp < 0x20 || cp == 0x7F )
            {
                if ( seq == null )
                {
                    seq = new SequenceNode();
                }
                if ( start < i )
                {
                    seq.add( literal( value.substring( start, i ) ) );
                }
                seq.add( CharacterSetNode.codePoint( cp ) );
                start = i + 1;
            }
        }
        if ( seq != null )
        {
            if ( start < value.length() )
            {
                seq.add( literal( value.substring( start ) ) );
            }
            return seq;
        }
        return this;
    }

    private static LiteralNode literal( String value )
    {
        LiteralNode literal = new LiteralNode();
        literal.value = value;
        return literal;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode( value );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj.getClass() != LiteralNode.class )
        {
            return false;
        }
        LiteralNode that = (LiteralNode) obj;
        return Objects.equals( this.value, that.value );
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformLiteral( param, this );
    }
}
