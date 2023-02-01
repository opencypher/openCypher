/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.tools.io;

import java.io.BufferedReader;
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

/**
 * Unifies the different classes that can handle text output and formatting, such as {@link PrintStream}, {@link
 * PrintWriter}, and {@link StringBuilder}.
 * <p>
 * The methods in this interface are defined to return the {@code Output} instance itself ({@code this}), in order to
 * allow chaining method calls in a "fluent" style.
 */
public interface Output extends Appendable, Closeable
{
    /**
     * Adapt an {@link OutputStream} to the Output interface.
     *
     * @param stream the {@link OutputStream} to adapt.
     * @return an {@code Output} instance that writes to the supplied {@link OutputStream}.
     */
    static Output output( OutputStream stream )
    {
        return new StreamOutput( stream instanceof PrintStream ? (PrintStream) stream : new PrintStream( stream ) );
    }

    /**
     * Adapt a {@link Writer} to the Output interface.
     *
     * @param writer the {@link Writer} to adapt.
     * @return an {@code Output} instance that writes to the supplied {@link Writer}.
     */
    static Output output( Writer writer )
    {
        if ( writer instanceof OutputWriter )
        {
            return ((OutputWriter) writer).output;
        }
        return new WriterOutput( writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer ) );
    }

    /**
     * Adapt a {@link StringBuilder} to the Output interface.
     *
     * @param builder the {@link StringBuilder} to adapt.
     * @return a {@linkplain Readable readable} {@code Output} instance that writes to the supplied {@link
     * StringBuilder}.
     */
    static Readable output( StringBuilder builder )
    {
        return new StringBuilderOutput( builder );
    }

    /**
     * Adapt a {@link StringBuffer} to the Output interface.
     *
     * @param buffer the {@link StringBuffer} to adapt.
     * @return a {@linkplain Readable readable} {@code Output} instance that writes to the supplied {@link
     * StringBuffer}.
     */
    static Readable output( StringBuffer buffer )
    {
        return new StringBufferOutput( buffer );
    }

    /**
     * Adapt a {@link CharBuffer} to the Output interface.
     *
     * @param buffer the {@link CharBuffer} to adapt.
     * @return an {@code Output} instance that writes to the supplied {@link CharBuffer}.
     */
    static Output output( CharBuffer buffer )
    {
        return new BufferOutput( buffer );
    }

    /**
     * Create an {@code Output} that writes to the specified file.
     *
     * @param path the file to write to.
     * @return an {@code Output} instance that writes to the specified file.
     */
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

    /**
     * Create a new {@code Output} that writes to a new string builder. The returned object can be used to {@linkplain
     * Readable#toString() retrieve the constructed string}.
     *
     * @return a new {@linkplain Readable readable} {@code Output} instance.
     */
    static Readable stringBuilder()
    {
        return output( new StringBuilder() );
    }

    /**
     * Create a new {@code Output} that writes to a new string builder of given capacity. The returned object can be
     * used to {@linkplain Readable#toString() retrieve the constructed string}.
     *
     * @param size the {@linkplain StringBuilder#StringBuilder(int) initial capacity} of the internal string builder.
     * @return a new {@linkplain Readable readable} {@code Output} instance.
     */
    static Output stringBuilder( int size )
    {
        return output( new StringBuilder( size ) );
    }

    /**
     * Get an {@code Output} instance that writes to {@link System#out the standard output stream}.
     *
     * @return an {@code Output} instance that writes to {@link System#out standard out}.
     */
    static Output stdOut()
    {
        return output( System.out );
    }

    /**
     * Get an {@code Output} instance that writes to {@link System#err the standard error output stream}.
     *
     * @return an {@code Output} instance that writes to {@link System#err standard error}.
     */
    static Output stdErr()
    {
        return output( System.err );
    }

    /**
     * Get an {@code Output} instance that writes nowhere.
     * <p>
     * The returned instance is {@linkplain Readable readable} in order to be usable in places where a {@linkplain
     * Readable readable output} is required, although the result of reading from this instance is always as if nothing
     * was written.
     *
     * @return an {@code Output} instance that writes nowhere.
     */
    static Output.Readable nowhere()
    {
        return Nowhere.OUTPUT;
    }

    /**
     * Wrap an {@link Output} in another instance that prepends a line number to every line written.
     *
     * @param output the wrapped output, where numbered lines will be written to.
     * @return an {@code Output} instance that prepends lines with line numbers.
     */
    static Output lineNumbers( Output output )
    {
        return new LineNumberingOutput( output );
    }

    /**
     * Combine multiple {@code Output} instances to one instance that writes to all combined instances.
     *
     * @param output the output instances to combine and write all output to.
     * @return an {@code Output} instance that writes to all the supplied instances.
     */
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

    /**
     * Fluent API for {@linkplain #multiplex(Output...) multiplexing} multiple {@code Output} instances.
     *
     * @param output an {@code Output} instance to also write to.
     * @return an {@code Output} instance that writes to both {@code this} and the supplied {@code Output} instance.
     */
    default Output and( Output output )
    {
        return multiplex( this, output );
    }

    /**
     * Convenience method for converting an object to a {@link String} through the means of a method that knows how to
     * write such an object to an {@link Output}.
     *
     * @param value  the object to convert to a {@link String}.
     * @param writer the method that knows how to write the supplied object to an {@code Output}.
     * @param <T>    the type of the object to write.
     * @return the resulting {@link String}.
     * @see #append(Object, BiConsumer)
     */
    static <T> String string( T value, BiConsumer<T, Output> writer )
    {
        return stringBuilder().append( value, writer ).toString();
    }

    /**
     * Join several strings as lines into one string with a line separator in between.
     *
     * @param lines the lines to join.
     * @return a string that consists of the supplied lines.
     */
    static String lines( String... lines )
    {
        StringBuilder result = new StringBuilder();
        String NL = System.lineSeparator();
        for ( String line : lines )
        {
            result.append( line ).append( NL );
        }
        return result.toString();
    }

    default Output repeat( int cp, int times )
    {
        for ( int i = 0; i < times; i++ )
        {
            appendCodePoint( cp );
        }
        return this;
    }

    /**
     * An extension of {@link Output} that signals that what was written can be read back.
     */
    interface Readable extends Output, CharSequence
    {
        /**
         * Returns the Unicode code point (starting) at the specified index.
         *
         * @param index the index to the {@code char} values.
         * @return the code point value of the character at the {@code index}
         * @throws IndexOutOfBoundsException if the {@code index} argument is negative or not less than the
         *                                   {@linkplain #length() length of the contents} of this object.
         * @see String#codePointAt(int) the equivalent method of <code>String</code>
         */
        int codePointAt( int index );

        /**
         * Compares the contents of this object to the specified {@link CharSequence}. The result is {@code true} if
         * and only if this instance holds the same sequence if {@code char} values as the specified sequence.
         *
         * @param that the sequence to compare this instance against.
         * @return {@code true} if this {@code String} represents the same sequence of {@code char} values as the
         * specified sequence, {@code false} otherwise.
         * @see String#contentEquals(CharSequence) the equivalent method of <code>String</code>.
         */
        default boolean contentEquals( CharSequence that )
        {
            if ( this == that )
            {
                return true;
            }
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

        /**
         * Return a {@link Reader} that reads the contents of this object.
         *
         * @return a {@link Reader} that reads the contents of this object.
         */
        default Reader reader()
        {
            return new CharSequenceReader( this, 0, length() );
        }

        default void lines( BiConsumer<String,Integer> lineHandler )
        {
            BufferedReader reader = new BufferedReader( reader() );
            try
            {
                String line;
                for ( int no = 1; null != (line = reader.readLine()); no++ )
                {
                    lineHandler.accept( line, no );
                }
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Should not throw exception when reading from Readable.", e );
            }
        }
    }

    /**
     * Escape the code points of the given character sequence according to the given replacement function.
     * <p>
     * The replacement function can (and is recommended to) be a <i>partial function</i>, only returning replacements
     * for the code points that are to be escaped and returning {@code null} for other code points.
     *
     * @param str the character sequence to escape the contents of.
     * @param replacement the replacement function that defines how to escape the code points that need escaping.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output escape( CharSequence str, IntFunction<String> replacement )
    {
        return escape( str, 0, str.length(), replacement );
    }

    /**
     * Escape the code points of the given character sequence according to the given replacement function.
     * <p>
     * The replacement function can (and is recommended to) be a <i>partial function</i>, only returning replacements
     * for the code points that are to be escaped and returning {@code null} for other code points.
     *
     * @param str the character sequence to escape the contents of.
     * @param start the position in the given character sequence to start at.
     * @param end the position in the given character sequence to end at.
     * @param replacement the replacement function that defines how to escape the code points that need escaping.
     * @return this {@code Output} instance to allow invocation chaining.
     */
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

    /**
     * Append an arbitrary object, formatted by a supplied method for writing the object.
     * <p>
     * Sample usage of this method:
     * <pre><code>
     * class Container {
     *      private Contained one, other;
     *
     *      public @Override String toString() {
     *          return Output.stringBuilder().append( this, Container::write ).toString();
     *      }
     *      public void write( Output output ) {
     *          output.append( "Container[one=" ).append( one, Contained::write )
     *                .append( ", other=" ).append( other, Contained::write ).append( ']' );
     *      }
     * }
     * </code></pre>
     *
     * @param value  the object to write.
     * @param writer the method that knows how to write the supplied object to an {@code Output}.
     * @param <T>    the type of the object to write.
     * @return this {@code Output} instance to allow invocation chaining.
     */
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

    /**
     * Append a boolean value.
     *
     * @param x the {@code boolean} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( boolean x )
    {
        append( Boolean.toString( x ) );
        return this;
    }

    /**
     * Append an integer.
     *
     * @param x the {@code int} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( int x )
    {
        append( Integer.toString( x ) );
        return this;
    }

    /**
     * Append a long integer.
     *
     * @param x the {@code long} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( long x )
    {
        append( Long.toString( x ) );
        return this;
    }

    /**
     * Append a floating-point number.
     *
     * @param x the {@code float} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( float x )
    {
        append( Float.toString( x ) );
        return this;
    }

    /**
     * Append a double-precision floating-point number.
     *
     * @param x the {@code double} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( double x )
    {
        append( Double.toString( x ) );
        return this;
    }

    /**
     * Appends the string representation of the {@code codePoint} argument to this sequence.
     *
     * @param codePoint a Unicode code point
     * @return this {@code Output} instance to allow invocation chaining.
     * @throws IllegalArgumentException if the specified {@code codePoint} isn't a valid Unicode code point.
     * @see java.lang.StringBuilder#appendCodePoint(int) the corresponding method of <code>StringBuilder</code>
     */
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

    /**
     * Append a string.
     *
     * @param str the {@code String} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( String str )
    {
        return append( (CharSequence) str );
    }

    /**
     * Append an array of characters.
     *
     * @param str the {@code char[]} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( char[] str )
    {
        return append( CharBuffer.wrap( str ) );
    }

    /**
     * Append a range from an array of characters.
     *
     * @param str    the {@code char[]} to append.
     * @param offset the position in the supplied array to start from.
     * @param len    the number of characters from the supplied array to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output append( char[] str, int offset, int len )
    {
        return append( CharBuffer.wrap( str, offset, len ) );
    }

    // PRINTLN

    /**
     * Append a specified string, with a given separator inserted between each line in the string.
     *
     * @param text      the {@code String} to append.
     * @param separator the {@code String} to insert between each line of the string.
     * @return this {@code Output} instance to allow invocation chaining.
     */
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
            append( System.lineSeparator() );
        }
        return this;
    }

    /**
     * Append a line separator.
     *
     * @return this {@code Output} instance to allow invocation chaining.
     * @see System#lineSeparator()
     */
    default Output println()
    {
        return append( System.lineSeparator() );
    }

    /**
     * Append a boolean value and a line separator.
     *
     * @param x the {@code boolean} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( boolean x )
    {
        return append( x ).println();
    }

    /**
     * Append the specified character and a line separator.
     *
     * @param x the {@code char} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( char x )
    {
        return append( x ).println();
    }

    /**
     * Append an integer and a line separator.
     *
     * @param x the {@code int} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( int x )
    {
        return append( x ).println();
    }

    /**
     * Append a long integer and a line separator.
     *
     * @param x the {@code long} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( long x )
    {
        return append( x ).println();
    }

    /**
     * Append a floating-point number and a line separator.
     *
     * @param x the {@code float} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( float x )
    {
        return append( x ).println();
    }

    /**
     * Append a double-precision floating-point number and a line separator.
     *
     * @param x the {@code double} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( double x )
    {
        return append( x ).println();
    }

    /**
     * Append an array of characters and a line separator.
     *
     * @param str the {@code char[]} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( char[] str )
    {
        return append( str ).println();
    }

    /**
     * Append a string and a line separator.
     *
     * @param str the {@code String} to append.
     * @return this {@code Output} instance to allow invocation chaining.
     */
    default Output println( String str )
    {
        return append( str ).println();
    }

    // FORMAT

    /**
     * Appends a formatted string to this output stream using the specified format string and arguments.
     *
     * @param format the format string.
     * @param args   Arguments referenced by the format specifiers in the format string.
     * @return this {@code Output} instance to allow invocation chaining.
     * @throws java.util.IllegalFormatException If a format string contains an illegal syntax, a format specifier that
     *                                          is incompatible with the given arguments, insufficient arguments given
     *                                          the format string, or other illegal conditions.
     * @see Formatter#format(String, Object...)
     */
    default Output format( String format, Object... args )
    {
        return format( Locale.getDefault( Locale.Category.FORMAT ), format, args );
    }

    /**
     * Appends a formatted string to this output stream using the specified format string and arguments.
     *
     * @param l      The {@linkplain java.util.Locale locale} to apply during
     *               formatting.  If {@code l} is {@code null} then no localization
     *               is applied.
     * @param format the format string.
     * @param args   Arguments referenced by the format specifiers in the format string.
     * @return this {@code Output} instance to allow invocation chaining.
     * @throws java.util.IllegalFormatException If a format string contains an illegal syntax, a format specifier that
     *                                          is incompatible with the given arguments, insufficient arguments given
     *                                          the format string, or other illegal conditions.
     * @see Formatter#format(Locale, String, Object...)
     */
    default Output format( Locale l, String format, Object... args )
    {
        new Formatter( this, l ).format( l, format, args );
        return this;
    }

    // CONTROL

    /**
     * Flushes the underlying output.
     */
    default void flush()
    {
    }

    @Override
    default void close()
    {
    }

    // CONVERSION

    /**
     * Return a {@link Writer} that writes to this {@code Output}.
     *
     * @return a {@link Writer} that writes to this {@code Output}.
     */
    default Writer writer()
    {
        return new OutputWriter( this );
    }

    // UTILITIES

    /**
     * Get the Unicode code point of the specified character sequence at the specified index.
     * <p>
     * This method improves  {@linkplain Character#codePointAt(CharSequence, int) the corresponding method in
     * <code>Character</code>} by delegating to methods on specific {@link CharSequence} implementations if present.
     *
     * @param str   the character sequence to get the code point from.
     * @param index the index in the character sequence the get the code point at.
     * @return the code point value of the character(s) at the {@code index} of the character sequence.
     * @throws IndexOutOfBoundsException if the {@code index} argument is negative or not less than the {@linkplain
     *                                   CharSequence#length() length of the contents} of the character sequence.
     * @see Character#codePointAt(CharSequence, int)
     */
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

    /**
     * Copies characters from the source {@link CharSequence} into the destination character array.
     * <p>
     * This method delegates to methods on specific {@link CharSequence} implementations if present.
     *
     * @param source the character sequence to copy characters from.
     * @param begin  the index of the first character in the sequence to copy.
     * @param end    the index after the last character in the sequence to copy.
     * @param target the destination array.
     * @param off    the start offset in the destination array.
     * @throws IndexOutOfBoundsException If any of the following is true:
     *                                   <ul><li>{@code srcBegin} is negative.
     *                                   <li>{@code srcBegin} is greater than {@code srcEnd}
     *                                   <li>{@code srcEnd} is greater than the length of the character sequence
     *                                   <li>{@code dstBegin} is negative
     *                                   <li>{@code dstBegin+(srcEnd-srcBegin)} is larger than {@code dst.length}</ul>
     * @see String#getChars(int, int, char[], int) the corresponding method in <code>String</code>
     */
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
