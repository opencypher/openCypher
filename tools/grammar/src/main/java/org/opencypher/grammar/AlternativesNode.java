package org.opencypher.grammar;

import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "alt")
class AlternativesNode extends Container implements Alternatives
{
    private BiasedTerms eligible;

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformAlternatives( param, this );
    }

    @Override
    public BiasedTerms eligibleForGeneration()
    {
        if ( eligible == null )
        {
            eligible = new Nodes( nodes.stream().filter( Node::isEligibleForGeneration ) );
        }
        return eligible;
    }
}
