package org.opencypher.tools.output;

import java.nio.CharBuffer;

class BufferOutput extends FormattingOutput<CharBuffer>
{
    BufferOutput( CharBuffer output )
    {
        super( output );
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
