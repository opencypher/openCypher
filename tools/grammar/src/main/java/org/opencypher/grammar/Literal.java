package org.opencypher.grammar;

import java.util.Objects;
import java.util.function.Consumer;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "literal")
final class Literal extends Node
{
    @Attribute
    String value;

    static void fromCharacters( char[] buffer, int start, int length, Consumer<? super Literal> add )
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

    private static void literal( Consumer<? super Literal> add, String literal )
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
                return Characters.codePoint( value.charAt( 0 ) );
            }
            else
            {
                return this;
            }
        }
        Sequence seq = null;
        int start = 0;
        for ( int i = 0, cp; i < value.length(); i += Character.charCount( cp ) )
        {
            cp = value.codePointAt( i );
            if ( cp < 0x20 || cp == 0x7F )
            {
                if ( seq == null )
                {
                    seq = new Sequence();
                }
                if ( start < i )
                {
                    seq.add( literal( value.substring( start, i ) ) );
                }
                seq.add( Characters.codePoint( cp ) );
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

    private static Literal literal( String value )
    {
        Literal literal = new Literal();
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
        if ( obj.getClass() != Literal.class )
        {
            return false;
        }
        Literal that = (Literal) obj;
        return Objects.equals( this.value, that.value );
    }

    @Override
    public String toString()
    {
        return "Literal{" + value + "}";
    }

    @Override
    public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
    {
        visitor.visitLiteral( value );
    }
}
