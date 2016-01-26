package org.opencypher.grammar;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "opt")
final class OptionalNode extends Sequenced implements Optional
{
    @Attribute(optional = true, uri = Grammar.GENERATOR_XML_NAMESPACE)
    double probability = 0.5;

    @Override
    <T, P, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param, Node term )
            throws EX
    {
        return transformation.transformOptional( param, this );
    }

    @Override
    public double probability()
    {
        return probability;
    }
}
