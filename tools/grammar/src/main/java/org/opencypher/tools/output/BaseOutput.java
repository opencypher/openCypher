package org.opencypher.tools.output;

import static java.util.Objects.deepEquals;
import static java.util.Objects.hash;

abstract class BaseOutput<Target> implements Output
{
    final Target output;

    BaseOutput( Target output )
    {
        assert output != null : "null output";
        this.output = output;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "( " + output + " )";
    }

    @Override
    public final boolean equals( Object that )
    {
        return this == that || (
                that != null &&
                this.getClass() == that.getClass() &&
                deepEquals( output, ((BaseOutput<?>) that).output ));
    }

    @Override
    public final int hashCode()
    {
        return hash( output );
    }
}
