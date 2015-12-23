package org.opencypher.tools.output;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Locale;
import java.util.Objects;

public interface Output extends Appendable, Closeable
{
    static Output output( OutputStream stream )
    {
        return new StreamOutput( stream instanceof PrintStream ? (PrintStream) stream : new PrintStream( stream ) );
    }

    static Output output( Writer writer )
    {
        return new WriterOutput( writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer ) );
    }

    static Output output( StringBuilder builder )
    {
        return new StringBuilderOutput( builder );
    }

    static Output output( StringBuffer buffer )
    {
        return new StringBufferOutput( buffer );
    }

    static Output output( CharBuffer buffer )
    {
        return new BufferOutput( buffer );
    }

    static Output stringBuilder()
    {
        return output( new StringBuilder() );
    }

    static Output stringBuilder( int size )
    {
        return output( new StringBuilder( size ) );
    }

    static String lines( String... lines )
    {
        StringBuilder result = new StringBuilder();
        for ( String line : lines )
        {
            result.append( line ).append( '\n' );
        }
        return result.toString();
    }

    // APPEND

    @Override
    Output append( char x );

    @Override
    Output append( CharSequence str );

    @Override
    Output append( CharSequence str, int start, int end );

    default Output append( boolean x )
    {
        append( Boolean.toString( x ) );
        return this;
    }

    default Output append( int x )
    {
        append( Integer.toString( x ) );
        return this;
    }

    default Output append( long x )
    {
        append( Long.toString( x ) );
        return this;
    }

    default Output append( float x )
    {
        append( Float.toString( x ) );
        return this;
    }

    default Output append( double x )
    {
        append( Double.toString( x ) );
        return this;
    }

    default Output appendCodePoint( int codePoint )
    {
        if ( Character.isBmpCodePoint( codePoint ) )
        {
            append( (char) codePoint );
        }
        else if ( Character.isValidCodePoint( codePoint ) )
        {
            append( Character.highSurrogate( codePoint ) );
            append( Character.lowSurrogate( codePoint ) );
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return this;
    }

    default Output append( Object obj )
    {
        return append( Objects.toString( obj ) );
    }

    default Output append( String str )
    {
        return append( (CharSequence) str );
    }

    default Output append( char[] str )
    {
        return append( CharBuffer.wrap( str ) );
    }

    default Output append( char[] str, int offset, int len )
    {
        return append( CharBuffer.wrap( str, offset, len ) );
    }

    // PRINTLN

    default Output println()
    {
        return append( '\n' );
    }

    default Output println( boolean x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( char x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( int x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( long x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( float x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( double x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( char[] x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( String x )
    {
        return append( x ).append( '\n' );
    }

    default Output println( Object x )
    {
        return append( x ).append( '\n' );
    }

    // FORMAT

    default Output printf( String format, Object... args )
    {
        return format( format, args );
    }

    default Output printf( Locale l, String format, Object... args )
    {
        return format( l, format, args );
    }

    Output format( String format, Object... args );

    Output format( Locale l, String format, Object... args );

    // CLOSE

    void flush();

    @Override
    void close();

    default Writer writer()
    {
        return new Writer()
        {
            @Override
            public void write( char[] cbuf, int off, int len )
            {
                Output.this.append( cbuf, off, len );
            }

            @Override
            public void write( int c ) throws IOException
            {
                Output.this.append( (char) c );
            }

            @Override
            public void write( char[] cbuf ) throws IOException
            {
                Output.this.append( cbuf );
            }

            @Override
            public void write( String str ) throws IOException
            {
                Output.this.append( str );
            }

            @Override
            public void write( String str, int off, int len ) throws IOException
            {
                Output.this.append( str, off, len );
            }

            @Override
            public Writer append( CharSequence csq ) throws IOException
            {
                Output.this.append( csq );
                return this;
            }

            @Override
            public Writer append( CharSequence csq, int start, int end ) throws IOException
            {
                Output.this.append( csq, start, end );
                return this;
            }

            @Override
            public Writer append( char c ) throws IOException
            {
                Output.this.append( c );
                return this;
            }

            @Override
            public void flush()
            {
                Output.this.flush();
            }

            @Override
            public void close()
            {
                Output.this.close();
            }
        };
    }
}
