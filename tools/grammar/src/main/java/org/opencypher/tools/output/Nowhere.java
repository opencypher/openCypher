package org.opencypher.tools.output;

import java.util.Locale;

enum Nowhere implements Output
{
    OUTPUT;

    @Override
    public Output and( Output output )
    {
        return output;
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

    private static final String STRING = Output.class.getSimpleName() + ".nowhere()";

    @Override
    public String toString()
    {
        return STRING;
    }
}
