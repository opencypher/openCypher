package org.opencypher.tools.output;

import java.io.Writer;

class OutputWriter extends Writer
{
    final Output output;

    OutputWriter( Output output )
    {
        this.output = output;
    }

    @Override
    public void write( char[] cbuf, int off, int len )
    {
        output.append( cbuf, off, len );
    }

    @Override
    public void write( int c )
    {
        output.append( (char) c );
    }

    @Override
    public void write( char[] cbuf )
    {
        output.append( cbuf );
    }

    @Override
    public void write( String str )
    {
        output.append( str );
    }

    @Override
    public void write( String str, int off, int len )
    {
        output.append( str, off, len );
    }

    @Override
    public Writer append( CharSequence csq )
    {
        output.append( csq );
        return this;
    }

    @Override
    public Writer append( CharSequence csq, int start, int end )
    {
        output.append( csq, start, end );
        return this;
    }

    @Override
    public Writer append( char c )
    {
        output.append( c );
        return this;
    }

    @Override
    public void flush()
    {
        output.flush();
    }

    @Override
    public void close()
    {
        output.close();
    }
}
