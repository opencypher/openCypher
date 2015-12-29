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
            return excludeCodePoint( Characters.codePoint( wellKnownName ) );
        }
    }

    public abstract <R, EX extends Exception> R accept( Visitor<R, EX> visitor ) throws EX;

    Exclusion()
    {
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
        String name = Characters.controlCharName( codePoint );
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
