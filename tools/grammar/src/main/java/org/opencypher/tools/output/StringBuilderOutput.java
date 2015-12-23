package org.opencypher.tools.output;

class StringBuilderOutput extends FormattingOutput
{
    private final StringBuilder output;

    StringBuilderOutput( StringBuilder output )
    {
        this.output = output;
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
