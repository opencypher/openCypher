package org.opencypher.tools.output;

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
    public Output append( Object obj )
    {
        output.append( obj );
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
