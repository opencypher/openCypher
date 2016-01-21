package org.opencypher.tools.output;

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
    public Output println( char[] x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
        }
        return this;
    }

    @Override
    public Output println( String x )
    {
        for ( Output output : this.output )
        {
            output.println( x );
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
