package org.opencypher.tools.output;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

class WriterOutput implements Output
{
    private final PrintWriter output;

    public WriterOutput( PrintWriter output )
    {
        this.output = output;
    }

    @Override
    public String toString()
    {
        return "Output( " +  output + " )";
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
    public Output append( Object obj )
    {
        output.print( obj );
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
        output.write( str );
        return this;
    }

    @Override
    public Output append( char[] str, int offset, int len )
    {
        output.write( str, offset, len );
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

    // CONTROL

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
        return output;
    }
}
