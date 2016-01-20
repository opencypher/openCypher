package org.opencypher.grammar;

import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "opt")
final class Optional extends Sequenced
{
    @Override
    <T, P, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param, Node term )
            throws EX
    {
        return transformation.transformOptional( param, term );
    }
}
