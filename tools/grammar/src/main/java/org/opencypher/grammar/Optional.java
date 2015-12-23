package org.opencypher.grammar;

import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "opt")
final class Optional extends Sequenced
{
    @Override
    <EX extends Exception> void accept( Node term, GrammarVisitor<EX> visitor ) throws EX
    {
        visitor.visitOptional( term );
    }
}
