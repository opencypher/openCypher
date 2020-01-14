/*
 * Copyright (c) 2015-2020 "Neo Technology,"
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

import java.util.NoSuchElementException;
import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

import static java.util.Objects.requireNonNull;

@Element(uri = Grammar.XML_NAMESPACE, name = "non-terminal")
final class NonTerminalNode extends Node implements NonTerminal
{
    @Attribute
    String ref;
    @Attribute(uri = Grammar.RAILROAD_XML_NAMESPACE, optional = true)
    Boolean skip, inline;
    @Attribute(uri = Grammar.RAILROAD_XML_NAMESPACE, optional = true)
    String title;
    private ProductionNode production;
    private int index = -1;
    private ProductionNode origin;

    @Override
    public ProductionNode production()
    {
        if ( production == null )
        {
            throw new NoSuchElementException( ref );
        }
        return production;
    }

    @Override
    public boolean skip()
    {
        return skip == null ? production().skip : skip;
    }

    @Override
    public boolean inline()
    {
        return inline == null ? production().inline : inline;
    }

    @Override
    public String title()
    {
        return title == null ? production().name : title;
    }

    @Override
    public Production declaringProduction()
    {
        return origin;
    }

    @Override
    boolean resolve( ProductionNode origin, ProductionResolver resolver )
    {
        production = resolver.resolveProduction( origin, requireNonNull( ref, "non-terminal reference" ) );
        if ( production != null )
        {
            production.addReference( this );
            if ( index < 0 )
            {
                this.origin = origin;
                index = resolver.nextNonTerminalIndex();
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode( ref );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj.getClass() != NonTerminalNode.class )
        {
            return false;
        }
        NonTerminalNode that = (NonTerminalNode) obj;
        return /*this.production == that.production &&*/ Objects.equals( this.ref, that.ref );
    }

    @Override
    public String toString()
    {
        return "NonTerminal{" + ref + "}";
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformNonTerminal( param, this );
    }
}
