/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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

import java.util.Objects;

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

    @Override
    boolean attributeEquals( Container that )
    {
        return Objects.equals( eligible, ((AlternativesNode) that).eligible );
    }
}
