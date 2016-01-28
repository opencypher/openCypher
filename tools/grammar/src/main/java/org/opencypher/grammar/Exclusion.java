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

public abstract class Exclusion
{
    public interface Visitor<R, EX extends Exception>
    {
        R excludeLiteral( String literal ) throws EX;

        default R excludeCodePoint( int cp ) throws EX
        {
            return excludeLiteral( new StringBuilder( 2 ).appendCodePoint( cp ).toString() );
        }

        default R excludeCharacterSet( String wellKnownName ) throws EX
        {
            return excludeCodePoint( CharacterSetNode.codePoint( wellKnownName ) );
        }
    }

    public abstract <R, EX extends Exception> R accept( Visitor<R, EX> visitor ) throws EX;

    Exclusion()
    {
    }

    @Override
    public String toString()
    {
        return accept( new Visitor<String, RuntimeException>()
        {
            @Override
            public String excludeLiteral( String literal ) throws RuntimeException
            {
                return "Exclude('" + literal + "')";
            }

            @Override
            public String excludeCharacterSet( String wellKnownName ) throws RuntimeException
            {
                return "Exclude(" + wellKnownName + ")";
            }

            @Override
            public String excludeCodePoint( int cp ) throws RuntimeException
            {
                return "Exclude(0x" + Integer.toHexString( cp ) + ")";
            }
        } );
    }

    static Exclusion literal( String literal )
    {
        if ( literal.length() >= 1 && literal.length() == Character.charCount( literal.codePointAt( 0 ) ) )
        {
            return codePoint( literal.codePointAt( 0 ) );
        }
        return new Exclusion()
        {
            @Override
            public <R, EX extends Exception> R accept( Visitor<R, EX> visitor ) throws EX
            {
                return visitor.excludeLiteral( literal );
            }
        };
    }

    static Exclusion codePoint( int codePoint )
    {
        String name = CharacterSetNode.controlCharName( codePoint );
        if ( name != null )
        {
            return characterSet( name );
        }
        return new Exclusion()
        {
            @Override
            public <R, EX extends Exception> R accept( Visitor<R, EX> visitor ) throws EX
            {
                return visitor.excludeCodePoint( codePoint );
            }
        };
    }

    static Exclusion characterSet( String wellKnownName )
    {
        return new Exclusion()
        {
            @Override
            public <R, EX extends Exception> R accept( Visitor<R, EX> visitor ) throws EX
            {
                return visitor.excludeCharacterSet( wellKnownName );
            }
        };
    }
}
