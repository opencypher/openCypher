package org.opencypher.tools.output;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;

class StreamOutput extends BaseOutput<PrintStream>
{
    StreamOutput( PrintStream output )
    {
        super( output );
    }

    // PRINT

    @Override
    public Output append( boolean x )
    {
        output.print( x );
        return this;
    }

    public Output append( char c )
    {
        output.append( c );
        return this;
    }

    @Override
    public Output append( int x )
    {
        output.print( x );
        return this;
    }

    @Override
    public Output append( long x )
    {
        output.print( x );
        return this;
    }

    @Override
    public Output append( float x )
    {
        output.print( x );
        return this;
    }

    @Override
    public Output append( double x )
    {
        output.print( x );
        return this;
    }

    public Output append( CharSequence csq )
    {
        output.append( csq );
        return this;
    }

    public Output append( CharSequence csq, int start, int end )
    {
        output.append( csq, start, end );
        return this;
    }

    @Override
    public Output append( String str )
    {
        output.print( str );
        return this;
    }

    @Override
    public Output append( char[] str )
    {
        output.print( str );
        return this;
    }

    // PRINTLN

    public Output println()
    {
        output.println();
        return this;
    }

    public Output println( boolean x )
    {
        output.println( x );
        return this;
    }

    public Output println( char x )
    {
        output.println( x );
        return this;
    }

    public Output println( int x )
    {
        output.println( x );
        return this;
    }

    public Output println( long x )
    {
        output.println( x );
        return this;
    }

    public Output println( float x )
    {
        output.println( x );
        return this;
    }

    public Output println( double x )
    {
        output.println( x );
        return this;
    }

    public Output println( char[] x )
    {
        output.println( x );
        return this;
    }

    public Output println( String x )
    {
        output.println( x );
        return this;
    }

    public Output println( Object x )
    {
        output.println( x );
        return this;
    }

    // FORMAT

    public Output format( String format, Object... args )
    {
        output.format( format, args );
        return this;
    }

    public Output format( Locale l, String format, Object... args )
    {
        output.format( l, format, args );
        return this;
    }

    // CLOSE

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

    @Override
    public Writer writer()
    {
        return new OutputStreamWriter( output );
    }
}
