package org.opencypher.tools.output;

class LineNumberingOutput implements Output
{
    private final Output output;
    boolean newLine = true;
    private int lineNo;

    LineNumberingOutput( Output output )
    {
        this.output = output;
    }

    @Override
    public Output append( char x )
    {
        if ( newLine )
        {
            output.format( "%5d: ", lineNo );
            newLine = false;
        }
        output.append( x );
        if ( x == '\n' )
        {
            newLine = true;
            lineNo++;
        }
        return this;
    }
}
