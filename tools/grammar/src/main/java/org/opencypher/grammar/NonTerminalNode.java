package org.opencypher.grammar;

import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

import static java.util.Objects.requireNonNull;

@Element(uri = Grammar.XML_NAMESPACE, name = "non-terminal")
final class NonTerminalNode extends Node implements NonTerminal
{
    @Attribute
    String ref;
    private ProductionNode production;
    private int index = -1;

    @Override
    public Production production()
    {
        return production;
    }

    @Override
    void resolve( ProductionNode origin, ProductionResolver resolver )
    {
        production = resolver.resolveProduction( origin, requireNonNull( ref, "non-terminal reference" ) );
        if ( index < 0 )
        {
            index = resolver.nextNonTerminalIndex();
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode( ref );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj.getClass() != NonTerminalNode.class )
        {
            return false;
        }
        NonTerminalNode that = (NonTerminalNode) obj;
        return /*this.production == that.production &&*/ Objects.equals( this.ref, that.ref );
    }

    @Override
    public String toString()
    {
        return "NonTerminal{" + ref + "}";
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformNonTerminal( param, this );
    }
}
