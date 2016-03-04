package org.opencypher.tools.output;

import java.io.IOException;
import java.io.Reader;

/**
 * Inspired by {@link java.io.StringReader}.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class CharSequenceReader extends Reader
{
    private CharSequence str;
    private int end;
    private int next;
    private int mark = 0;

    public CharSequenceReader( CharSequence str, int start, int end )
    {
        this.str = str;
        this.next = start;
        this.end = end;
    }

    public int read() throws IOException
    {
        synchronized ( lock )
        {
            ensureOpen();
            if ( next >= end )
            {
                return -1;
            }
            return str.charAt( next++ );
        }
    }

    public int read( char[] cbuf, int off, int len ) throws IOException
    {
        synchronized ( lock )
        {
            ensureOpen();
            if ( (off < 0) || (off > cbuf.length) || (len < 0) ||
                 ((off + len) > cbuf.length) || ((off + len) < 0) )
            {
                throw new IndexOutOfBoundsException();
            }
            else if ( len == 0 )
            {
                return 0;
            }
            if ( next >= end )
            {
                return -1;
            }
            int n = Math.min( end - next, len );
            Output.getChars( str, next, next + n, cbuf, off );
            next += n;
            return n;
        }
    }

    public long skip( long ns ) throws IOException
    {
        synchronized ( lock )
        {
            ensureOpen();
            if ( next >= end )
            {
                return 0;
            }
            long n = Math.min( end - next, ns );
            n = Math.max( -next, n );
            next += n;
            return n;
        }
    }

    public boolean ready() throws IOException
    {
        synchronized ( lock )
        {
            ensureOpen();
            return true;
        }
    }

    public boolean markSupported()
    {
        return true;
    }

    public void mark( int readAheadLimit ) throws IOException
    {
        if ( readAheadLimit < 0 )
        {
            throw new IllegalArgumentException( "Read-ahead limit < 0" );
        }
        synchronized ( lock )
        {
            ensureOpen();
            mark = next;
        }
    }

    public void reset() throws IOException
    {
        synchronized ( lock )
        {
            ensureOpen();
            next = mark;
        }
    }

    public void close()
    {
        str = null;
    }

    private void ensureOpen() throws IOException
    {
        if ( str == null )
        {
            throw new IOException( "Stream closed" );
        }
    }
}
