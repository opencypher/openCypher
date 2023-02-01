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

import java.util.Locale;

enum Nowhere implements Output.Readable
{
    OUTPUT;

    @Override
    public Output and( Output output )
    {
        return output;
    }

    @Override
    public int length()
    {
        return 0;
    }

    @Override
    public char charAt( int index )
    {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public CharSequence subSequence( int start, int end )
    {
        if (start == 0 && end == 0)
        {
            return "";
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int codePointAt( int index )
    {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean contentEquals( CharSequence that )
    {
        return that.length() == 0;
    }

    @Override
    public Output append( char x )
    {
        return this;
    }

    @Override
    public Output append( CharSequence str )
    {
        return this;
    }

    @Override
    public Output append( CharSequence str, int start, int end )
    {
        return this;
    }

    @Override
    public Output format( String format, Object... args )
    {
        return this;
    }

    @Override
    public Output format( Locale l, String format, Object... args )
    {
        return this;
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close()
    {
    }

    @Override
    public String toString()
    {
        return "";
    }
}
