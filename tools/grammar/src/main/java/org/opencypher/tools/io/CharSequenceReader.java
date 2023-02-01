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

import java.io.IOException;
import java.io.Reader;

/**
 * Inspired by {@link java.io.StringReader}.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
class CharSequenceReader extends Reader
{
    private CharSequence str;
    private int end;
    private int next;
    private int mark = 0;

    CharSequenceReader( CharSequence str, int start, int end )
    {
        super( str );
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
