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

class MultiplexedOutput extends FormattingOutput<Output[]>
{
    MultiplexedOutput( Output[] output )
    {
        super( output );
        assert output.length > 1 : "useless multiplex";
        for ( Output valid : output )
        {
            assert valid != null && valid != Nowhere.OUTPUT : "useless multiplexed output";
        }
    }

    @Override
    public String toString()
    {
        Output result = Output.stringBuilder().append( getClass().getSimpleName() );
        String sep = "( ";
        for ( Output output : this.output )
        {
            result.append( sep ).append( output.toString() );
            sep = ", ";
        }
        return result.append( " )" ).toString();
    }

    // APPEND

    @Override
    public Output append( char x )
    {
        for ( Output output : this.output )
        {
            output.append( x );
        }
        return this;
    }

    @Override
    public Output append( CharSequence str )
    {
        for ( Output output : this.output )
        {
            output.append( str );
        }
        return this;
    }

    @Override
    public Output append( CharSequence str, int start, int end )
    {
        for ( Output output : this.output )
        {
            output.append( str, start, end );
        }
        return this;
    }

    @Override
    public Output append( boolean x )
    {
        for ( Output output : this.output )
        {
            output.append( x );
        }
        return this;
    }

    @Override
    public Output append( int x )
    {
        for ( Output output : this.output )
        {
            output.append( x );
        }
        return this;
    }

    @Override
    public Output append( long x )
    {
        for ( Output output : this.output )
        {
            output.append( x );
        }
        return this;
    }

    @Override
    public Output append( float x )
    {
        for ( Output output : this.output )
        {
            output.append( x );
        }
        return this;
    }

    @Override
    public Output append( double x )
    {
        for ( Output output : this.output )
        {
            output.append( x );
        }
        return this;
    }

    @Override
    public Output appendCodePoint( int codePoint )
    {
        for ( Output output : this.output )
        {
            output.appendCodePoint( codePoint );
        }
        return this;
    }

    @Override
    public Output append( String str )
    {
        for ( Output output : this.output )
        {
            output.append( str );
        }
        return this;
    }

    @Override
    public Output append( char[] str )
    {
        for ( Output output : this.output )
        {
            output.append( str );
        }
        return this;
    }

    @Override
    public Output append( char[] str, int offset, int len )
    {
        for ( Output output : this.output )
        {
            output.append( str, offset, len );
        }
        return this;
    }

    // PRINTLN

    @Override
    public Output println()
    {
        for ( Output output : this.output )
        {
            output.println();
        }
        return this;
    }

    @Override
    public Output println( boolean x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
        }
        return this;
    }

    @Override
    public Output println( char x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
        }
        return this;
    }

    @Override
    public Output println( int x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
        }
        return this;
    }

    @Override
    public Output println( long x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
        }
        return this;
    }

    @Override
    public Output println( float x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
        }
        return this;
    }

    @Override
    public Output println( double x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
        }
        return this;
    }

    @Override
    public Output println( char[] str )
    {
        for ( Output output : this.output )
        {
            output.println( str );
        }
        return this;
    }

    @Override
    public Output println( String str )
    {
        for ( Output output : this.output )
        {
            output.println( str );
        }
        return this;
    }

    // CONTROL

    @Override
    public void flush()
    {
        for ( Output output : this.output )
        {
            output.flush();
        }
    }

    @Override
    public void close()
    {
        for ( Output output : this.output )
        {
            output.close();
        }
    }
}
