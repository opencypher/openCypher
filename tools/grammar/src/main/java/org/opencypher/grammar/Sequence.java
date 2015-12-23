package org.opencypher.grammar;

import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "seq")
class Sequence extends Container
{
    @Override
    public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
    {
        visitor.visitSequence( terms() );
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
