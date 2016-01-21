package org.opencypher.tools.output;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.lang.Character.highSurrogate;
import static java.lang.Character.isBmpCodePoint;
import static java.lang.Character.isValidCodePoint;
import static java.lang.Character.lowSurrogate;

@FunctionalInterface
public interface Output extends Appendable, Closeable
{
    static Output output( OutputStream stream )
    {
        return new StreamOutput( stream instanceof PrintStream ? (PrintStream) stream : new PrintStream( stream ) );
    }

    static Output output( Writer writer )
    {
        if ( writer instanceof OutputWriter )
        {
            return ((OutputWriter) writer).output;
        }
        return new WriterOutput( writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer ) );
    }

    static Readable output( StringBuilder builder )
    {
        return new StringBuilderOutput( builder );
    }

    static Readable output( StringBuffer buffer )
    {
        return new StringBufferOutput( buffer );
    }

    static Output output( CharBuffer buffer )
    {
        return new BufferOutput( buffer );
    }

    static Readable stringBuilder()
    {
        return output( new StringBuilder() );
    }

    static Output stringBuilder( int size )
    {
        return output( new StringBuilder( size ) );
    }

    static Output stdOut()
    {
        return output( System.out );
    }

    static Output stdErr()
    {
        return output( System.err );
    }

    static Output nowhere()
    {
        return Nowhere.OUTPUT;
    }

    @SuppressWarnings("ManualArrayToCollectionCopy")
    static Output multiplex( Output... output )
    {
        if ( output == null || output.length == 0 )
        {
            return nowhere();
        }
        if ( output.length == 1 )
        {
            return output[0];
        }
        Set<Output> flattened = new HashSet<>();
        boolean altered = false;
        for ( Output item : output )
        {
            if ( item instanceof MultiplexedOutput )
            {
                Collections.addAll( flattened, ((MultiplexedOutput) item).output );
                altered = true;
            }
            else if ( item == Nowhere.OUTPUT )
            {
                altered = true;
            }
            else if ( !flattened.add( item ) )
            {
                altered = true;
            }
        }
        if ( !altered )
        {
            return new MultiplexedOutput( output );
        }
        if ( flattened.size() == 0 )
        {
            return nowhere();
        }
        if ( flattened.size() == 1 )
        {
            return flattened.iterator().next();
        }
        return new MultiplexedOutput( flattened.toArray( new Output[flattened.size()] ) );
    }

    default Output and( Output output )
    {
        return multiplex( this, output );
    }

    static <T> String string( T value, BiConsumer<T, Output> writer )
    {
        return stringBuilder().append( value, writer ).toString();
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

    interface Readable extends Output, CharSequence
    {
        int codePointAt( int index );
    }

    // APPEND

    default <T> Output append( T value, BiConsumer<T, Output> writer )
    {
        writer.accept( value, this );
        return this;
    }

    @Override
    Output append( char x );

    @Override
    default Output append( CharSequence str )
    {
        return append( str, 0, str.length() );
    }

    @Override
    default Output append( CharSequence str, int start, int end )
    {
        for ( int i = start; i < end; i++ )
        {
            append( str.charAt( i ) );
        }
        return this;
    }

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
        if ( isBmpCodePoint( codePoint ) )
        {
            append( (char) codePoint );
        }
        else if ( isValidCodePoint( codePoint ) )
        {
            append( highSurrogate( codePoint ) );
            append( lowSurrogate( codePoint ) );
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return this;
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

    // FORMAT

    default Output printf( String format, Object... args )
    {
        return format( format, args );
    }

    default Output printf( Locale l, String format, Object... args )
    {
        return format( l, format, args );
    }

    default Output format( String format, Object... args )
    {
        new Formatter( this ).format( Locale.getDefault(), format, args );
        return this;
    }

    default Output format( Locale l, String format, Object... args )
    {
        new Formatter( this, l ).format( l, format, args );
        return this;
    }

    // CONTROL

    default void flush()
    {
    }

    @Override
    default void close()
    {
    }

    // CONVERSION

    default Writer writer()
    {
        return new OutputWriter( this );
    }
}
