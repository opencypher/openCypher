package org.opencypher.grammar;

import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "seq")
class SequenceNode extends Container implements Sequence
{
    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformSequence( param, this );
    }

    static Node implicit( Node current, Node node )
    {
        if ( current == null )
        {
            current = node;
        }
        else if ( current instanceof SequenceNode )
        {
            SequenceNode sequence = (SequenceNode) current;
            sequence.add( node );
        }
        else
        {
            SequenceNode sequence = new SequenceNode();
            sequence.add( current );
            sequence.add( node );
            current = sequence;
        }
        return current;
    }
}
