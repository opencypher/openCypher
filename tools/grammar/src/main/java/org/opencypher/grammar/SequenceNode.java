/*
 * Copyright (c) 2015-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
