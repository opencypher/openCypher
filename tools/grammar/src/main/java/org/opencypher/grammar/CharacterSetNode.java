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
import java.util.Random;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "character")
class CharacterSetNode extends Node implements CharacterSet
{
    static final String DEFAULT_SET = Unicode.ANY.name();
    /** Alias for {@link CharacterSet#EOI}. */
    private static final String EOF = "EOF";

    /** {@code null} means EOI (End Of Input). */
    private CodePointSet set = CodePointSet.ANY;

    /**
     * Sets the name or definition of the set to base these characters on.
     * The set can either be the name of a well-known unicode set (such as {@code "ID_Start"}), or it can be a set of
     * characters in unicode set notation.
     * <p>
     * The set of well-known unicode set names are defined in the {@link org.opencypher.grammar.CharacterSet.Unicode
     * Unicode enum}, in addition to those {@code "EOI"} or {@code "EOF"} are also accepted and means a "character"
     * representing the end of the input stream.
     * <p>
     * Unicode set notation needs to be enclosed within {@code [} and {@code ]} and needs to be a sequence of either:
     * <ul>
     * <li>A single character, or</li>
     * <li>a range of character, denoted as two characters with a dash ({@code -}) in between.</li>
     * </ul>
     *
     * @param set The set to base these characters on.
     */
    @Attribute(optional = true)
    public void set( String set )
    {
        try
        {
            this.set = Unicode.valueOf( set ).set;
        }
        catch ( IllegalArgumentException e )
        {
            if ( EOI.equals( set ) || EOF.equals( set ) )
            {
                this.set = null;
            }
            else if ( set.length() > 2 && set.charAt( 0 ) == '[' && set.charAt( set.length() - 1 ) == ']' )
            {
                this.set = CodePointSet.parse( set );
            }
            else
            {
                throw new IllegalArgumentException(
                        "Invalid character set, " +
                        "should either be the name of a well known set, or a set enclosed in '[...]', " +
                        "not: " + set );
            }
        }
    }

    @Override
    CharacterSetNode defensiveCopy()
    {
        CharacterSetNode characters = new CharacterSetNode();
        characters.set = set;
        return characters;
    }

    void exclude( int... codePoints )
    {
        exclude( CodePointSet.codePoints( codePoints ) );
    }

    @Child
    void add( Except exception )
    {
        exclude( exception.set() );
    }

    private void exclude( CodePointSet excepted )
    {
        if ( set == null )
        {
            throw new IllegalArgumentException( "Cannot exclude code points from EOI" );
        }
        set = set.except( excepted );
    }

    @Override
    Node replaceWithVerified()
    {
        return super.replaceWithVerified();
    }

    @Override
    public boolean isControlCharacter() {
        return set.isSingleCharacter();
    }

    @Override
    boolean isEligibleForGeneration()
    {
        return set != null && super.isEligibleForGeneration();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !(o instanceof CharacterSetNode) )
        {
            return false;
        }
        CharacterSetNode that = (CharacterSetNode) o;
        return Objects.equals( this.set, that.set );
    }

    @Override
    public int hashCode()
    {
        return set.hashCode();
    }

    @Override
    public String toString()
    {
        return String.format( "Characters{%s}", set == null ? "EOI" : set );
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformCharacters( param, this );
    }

    @Override
    public String name()
    {
        return set == null ? EOI : set.name;
    }

    @Override
    public <EX extends Exception> void accept( DefinitionVisitor<EX> visitor ) throws EX
    {
        if ( set == null )
        {
            if ( visitor instanceof DefinitionVisitor.NamedSetVisitor )
            {
                ((DefinitionVisitor.NamedSetVisitor<EX>) visitor).visitSet( EOI ).close();
            }
            else
            {
                throw new UnsupportedOperationException( "visitSet(EOI)" );
            }
        }
        else
        {
            set.accept( visitor );
        }
    }

    @Override
    public int randomCodePoint( Random random )
    {
        if ( set == null )
        {
            throw new IllegalArgumentException( "Cannot generate EOI" );
        }
        return set.randomCodePoint( random );
    }

    @Override
    public boolean contains( int codePoint )
    {
        return set != null && set.contains( codePoint );
    }

    
    @Override
    public boolean hasExclusions() {
        return set != null && set.hasExclusions();
    }

    static boolean isReserved( String name )
    {
        if ( ANY.equals( name ) || EOI.equals( name ) || EOF.equals( name ) )
        {
            return true;
        }
        try
        {
            if ( Unicode.valueOf( name ) == null )
            {
                return false;
            }
        }
        catch ( IllegalArgumentException e )
        {
            return false;
        }
        return true;
    }

    public static CharacterSetNode charSet( String name )
    {
        CharacterSetNode result = new CharacterSetNode();
        result.set( name );
        return result;
    }
}
