package org.opencypher.grammar;

import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "repeat")
class Repetition extends Sequenced
{
    @Attribute(optional = true)
    int min;
    @Attribute(optional = true)
    Integer max;

    @Override
    <EX extends Exception> void accept( Node term, GrammarVisitor<EX> visitor ) throws EX
    {
        visitor.visitRepetition( min, max, term );
    }

    @Override
    int attributeHash()
    {
        return Objects.hash( min, max );
    }

    @Override
    boolean attributeEquals( Sequenced obj )
    {
        Repetition that = (Repetition) obj;
        return Objects.equals( this.min, that.min ) && Objects.equals( this.max, that.max );
    }

    @Override
    void attributeString( StringBuilder result )
    {
        if ( min > 0 )
        {
            result.append( "{min=" ).append( min );
        }
        if ( max != null )
        {
            result.append( min > 0 ? "{max=" : ",max=" ).append( max );
        }
        if ( min > 0 || max != null )
        {
            result.append( '}' );
        }
    }
}
