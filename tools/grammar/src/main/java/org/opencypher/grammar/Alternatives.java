package org.opencypher.grammar;

import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "alt")
class Alternatives extends Container
{
    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformAlternatives( param, terms() );
    }
}
