/*
 * Copyright (c) 2015-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.tools.output;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

import static java.lang.Character.charCount;
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

    static Output output( Path path )
    {
        try
        {
            return output( Files.newOutputStream( path ) );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Failed to open " + path, e );
        }
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

    static Output.Readable nowhere()
    {
        return Nowhere.OUTPUT;
    }

    static Output lineNumbers( Output output )
    {
        return new LineNumberingOutput( output );
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

        default boolean contentsEquals( CharSequence that )
        {
            if ( this.length() != that.length() )
            {
                return false;
            }
            for ( int i = 0, len = length(); i < len; i++ )
            {
                if ( this.charAt( i ) != that.charAt( i ) )
                {
                    return false;
                }
            }
            return true;
        }

        default Reader reader()
        {
            return new CharSequenceReader( this, 0, length() );
        }
    }

    default Output escape( CharSequence str, IntFunction<String> replacement )
    {
        return escape( str, 0, str.length(), replacement );
    }

    default Output escape( CharSequence str, int start, int end, IntFunction<String> replacement )
    {
        if ( start < 0 || end > str.length() )
        {
            throw new IllegalArgumentException(
                    String.format( "start=%d, end=%d, length=%d", start, end, str.length() ) );
        }
        for ( int i = 0, cp; i < end; i += charCount( cp ) )
        {
            String replaced = replacement.apply( cp = codePointAt( str, i ) );
            if ( replaced != null )
            {
                if ( i > start )
                {
                    append( str, start, i );
                }
                append( replaced );
                start = i + charCount( cp );
            }
        }
        if ( start < end )
        {
            append( str, start, end );
        }
        return this;
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

    default Output printLines( String text, String separator )
    {
        boolean emit = false;
        for ( int i = 0, cp; i < text.length(); i += charCount( cp ) )
        {
            if ( emit )
            {
                append( separator );
            }
            appendCodePoint( cp = text.codePointAt( i ) );
            emit = cp == '\n';
        }
        if ( !emit && !text.isEmpty() )
        {
            append( '\n' );
        }
        return this;
    }

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

    // UTILITIES

    static int codePointAt( CharSequence str, int index )
    {
        if ( str instanceof String )
        {
            return ((String) str).codePointAt( index );
        }
        if ( str instanceof Readable )
        {
            return ((Readable) str).codePointAt( index );
        }
        if ( str instanceof StringBuilder )
        {
            return ((StringBuilder) str).codePointAt( index );
        }
        if ( str instanceof StringBuffer )
        {
            return ((StringBuffer) str).codePointAt( index );
        }
        return Character.codePointAt( str, index );
    }

    static void getChars( CharSequence source, int begin, int end, char[] target, int off )
    {
        if ( source instanceof String )
        {
            ((String) source).getChars( begin, end, target, off );
        }
        else if ( source instanceof StringBuilder )
        {
            ((StringBuilder) source).getChars( begin, end, target, off );
        }
        else if ( source instanceof StringBuffer )
        {
            ((StringBuffer) source).getChars( begin, end, target, off );
        }
        else
        {
            if ( begin < 0 )
            {
                throw new StringIndexOutOfBoundsException( begin );
            }
            if ( end > source.length() )
            {
                throw new StringIndexOutOfBoundsException( end );
            }
            if ( begin > end )
            {
                throw new StringIndexOutOfBoundsException( end - begin );
            }
            for ( int i = 0, len = end - begin; i < len; i++ )
            {
                target[i + off] = source.charAt( begin + i );
            }
        }
    }
}
