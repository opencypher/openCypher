package org.opencypher.grammar;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "except")
class Except
{
    @Attribute(optional = true)
    String literal;
    @Attribute(optional = true)
    Integer codePoint;

    Exclusion exclusion()
    {
        if ( (literal == null && codePoint == null) || (literal != null && codePoint != null) )
        {
            throw new IllegalArgumentException( "Must have either 'literal' or 'codePoint', but not both." );
        }
        if ( literal != null )
        {
            return Exclusion.literal( literal );
        }
        else
        {
            return Exclusion.codePoint( codePoint );
        }
    }
}
