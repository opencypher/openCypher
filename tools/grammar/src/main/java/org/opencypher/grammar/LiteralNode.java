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
    @Attribute(optional = true, name = "case-sensitive")
    boolean caseSensitive = true;

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
    public boolean caseSensitive()
    {
        return caseSensitive;
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

    @Override
    public <EX extends Exception> void accept( Visitor<EX> visitor ) throws EX
    {
        if ( caseSensitive )
        {
            visitor.visitLiteral( value );
        }
        else
        {
            int start = 0;
            for ( int i = 0, end = value.length(), cp; i < end; i += Character.charCount( cp ) )
            {
                cp = value.charAt( i );
                if ( Character.isLowerCase( cp ) || Character.isUpperCase( cp ) || Character.isTitleCase( cp ) )
                {
                    if ( start < i )
                    {
                        visitor.visitLiteral( value.substring( start, i ) );
                    }
                    start = i + Character.charCount( cp );
                    visitor.visitAnyCase( cp );
                }
            }
            if ( start < value.length() )
            {
                visitor.visitLiteral( value.substring( start ) );
            }
        }
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
                    textLiteral( add, new String( buffer, start, pos - start ) );
                }
                start = pos + step;
            }
        }
        if ( start != pos )
        {
            textLiteral( add, new String( buffer, start, pos - start ) );
        }
    }

    /**
     * Creates a text literal, a case insensitive literal.
     *
     * @param add     the adder that adds the literal node to its parent node.
     * @param literal the literal string.
     */
    private static void textLiteral( Consumer<? super LiteralNode> add, String literal )
    {
        add.accept( literal( literal, false ) );
    }

    @Override
    Node replaceWithVerified()
    {
        if ( value.length() == 1 )
        {
            String control = CharacterSet.controlCharName( value.charAt( 0 ) );
            if ( control != null )
            {
                return CharacterSetNode.charSet( control );
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
            String control = CharacterSet.controlCharName( cp );
            if ( control != null )
            {
                if ( seq == null )
                {
                    seq = new SequenceNode();
                }
                if ( start < i )
                {
                    seq.add( literal( value.substring( start, i ), caseSensitive ) );
                }
                seq.add( CharacterSetNode.charSet( control ) );
                start = i + 1;
            }
        }
        if ( seq != null )
        {
            if ( start < value.length() )
            {
                seq.add( literal( value.substring( start ), caseSensitive ) );
            }
            return seq;
        }
        return this;
    }

    private static LiteralNode literal( String value, boolean caseSensitive )
    {
        LiteralNode literal = new LiteralNode();
        literal.value = value;
        literal.caseSensitive = caseSensitive;
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
        return Objects.equals( this.value, that.value ) &&
               this.caseSensitive == that.caseSensitive;
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformLiteral( param, this );
    }
}
