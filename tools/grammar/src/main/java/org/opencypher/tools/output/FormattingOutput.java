package org.opencypher.tools.output;

import java.util.Formatter;
import java.util.Locale;

abstract class FormattingOutput<Target> extends BaseOutput<Target>
{
    FormattingOutput( Target output )
    {
        super( output );
    }

    private Formatter formatter;

    // FORMAT

    @Override
    public final Output format( String format, Object... args )
    {
        if ( formatter == null || formatter.locale() != Locale.getDefault() )
        {
            formatter = new Formatter( this );
        }
        formatter.format( Locale.getDefault(), format, args );
        return this;
    }

    @Override
    public final Output format( Locale l, String format, Object... args )
    {
        if ( formatter == null || formatter.locale() != l )
        {
            formatter = new Formatter( this, l );
        }
        formatter.format( l, format, args );
        return this;
    }

    @Override
    public final Output printf( String format, Object... args )
    {
        return format( format, args );
    }

    @Override
    public final Output printf( Locale l, String format, Object... args )
    {
        return format( l, format, args );
    }
}
