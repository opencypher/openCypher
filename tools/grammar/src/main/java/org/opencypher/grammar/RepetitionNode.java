/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.grammar;

import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "repeat")
class RepetitionNode extends Sequenced implements Repetition
{
    @Attribute(optional = true)
    int min;
    @Attribute(optional = true)
    Integer max;
    @Attribute(optional = true, uri = Grammar.GENERATOR_XML_NAMESPACE)
    Integer norm;

    @Override
    <T, P, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param, Node term )
            throws EX
    {
        return transformation.transformRepetition( param, this );
    }

    @Override
    public int minTimes()
    {
        return min;
    }

    @Override
    public boolean limited()
    {
        return max != null;
    }

    @Override
    public int maxTimes()
    {
        if ( max == null )
        {
            throw new IllegalStateException( "Unlimited repetition" );
        }
        return max;
    }

    @Override
    public int norm()
    {
        if ( norm == null )
        {
            return min;
        }
        return norm;
    }

    @Override
    int attributeHash()
    {
        return Objects.hash( min, max );
    }

    @Override
    boolean attributeEquals( Sequenced obj )
    {
        RepetitionNode that = (RepetitionNode) obj;
        return Objects.equals( this.min, that.min ) && Objects.equals( this.max, that.max );
    }

    @Override
    void attributeString( StringBuilder result )
    {
        if ( min > 0 )
        {
            result.append( "{min=" ).append( min );
        }
        if ( max != null )
        {
            result.append( min > 0 ? "{max=" : ",max=" ).append( max );
        }
        if ( min > 0 || max != null )
        {
            result.append( '}' );
        }
    }

    @Override
    RepetitionNode copy()
    {
        RepetitionNode repetition = new RepetitionNode();
        repetition.min = this.min;
        repetition.max = this.max;
        repetition.norm = this.norm;
        return repetition;
    }
}
