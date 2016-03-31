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
package org.opencypher.tools.io;

import java.nio.CharBuffer;

class BufferOutput extends FormattingOutput<CharBuffer>
{
    BufferOutput( CharBuffer output )
    {
        super( output );
    }

    @Override
    public Output append( char x )
    {
        output.append( x );
        return this;
    }

    @Override
    public Output append( CharSequence str )
    {
        output.append( str );
        return this;
    }

    @Override
    public Output append( CharSequence str, int start, int end )
    {
        output.append( str, start, end );
        return this;
    }
}
