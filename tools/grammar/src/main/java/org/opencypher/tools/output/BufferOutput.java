package org.opencypher.tools.output;

import java.nio.CharBuffer;

class BufferOutput extends FormattingOutput
{
    private final CharBuffer output;

    public BufferOutput( CharBuffer output )
    {
        this.output = output;
    }

    @Override
    public String toString()
    {
        return "Output( " +  output + " )";
    }

    @Override
    public Output append( char x )
    {
        output.append( x );
        return this;
    }

    @Override
    public Output append( CharSequence str )
    {
        output.append( str );
        return this;
    }

    @Override
    public Output append( CharSequence str, int start, int end )
    {
        output.append( str, start, end );
        return this;
    }
}
