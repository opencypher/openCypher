package org.opencypher.grammar;

import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "seq")
class Sequence extends Container
{
    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformSequence( param, terms() );
    }

    static Node implicit( Node current, Node node )
    {
        if ( current == null )
        {
            current = node;
        }
        else if ( current instanceof Sequence )
        {
            Sequence sequence = (Sequence) current;
            sequence.add( node );
        }
        else
        {
            Sequence sequence = new Sequence();
            sequence.add( current );
            sequence.add( node );
            current = sequence;
        }
        return current;
    }
}
