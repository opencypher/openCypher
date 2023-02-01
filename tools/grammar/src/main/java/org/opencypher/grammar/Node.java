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

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.LocationAware;

import static java.lang.System.identityHashCode;

abstract class Node extends Grammar.Term implements LocationAware
{
    private String path;
    private int lineNumber;
    private int columnNumber;
    @Attribute(optional = true, uri = Grammar.GENERATOR_XML_NAMESPACE)
    double bias = 1.0;

    @Override
    public final void location( String path, int lineNumber, int columnNumber )
    {
        this.path = path;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    @Override
    final ProductionNode addTo( ProductionNode production )
    {
        production.add( defensiveCopy() );
        return production;
    }

    @Override
    final Container addTo( Container container )
    {
        container.add( defensiveCopy() );
        return container;
    }

    @Override
    final Sequenced addTo( Sequenced sequenced )
    {
        sequenced.add( defensiveCopy() );
        return sequenced;
    }

    boolean resolve( ProductionNode origin, ProductionResolver resolver )
    {
        return true;
    }

    Node replaceWithVerified()
    {
        return this;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals( Object obj );

    @Override
    public abstract String toString();

    boolean isEligibleForGeneration()
    {
        return bias > 0;
    }

    /**
     * Override to return a defensive copy of this node, to prevent further mutation.
     *
     * Only nodes with publicly mutable state need to override this method.
     *
     * @return a shallow copy of this node.
     */
    Node defensiveCopy()
    {
        return this;
    }

    boolean isEpsilon()
    {
        return false;
    }

    static Node epsilon()
    {
        return new Node()
        {
            @Override
            boolean isEpsilon()
            {
                return true;
            }

            @Override
            public int hashCode()
            {
                return identityHashCode( this );
            }

            @Override
            public boolean equals( Object obj )
            {
                return this.getClass() == obj.getClass();
            }

            @Override
            public String toString()
            {
                return "EPSILON";
            }

            @Override
            public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param )
                    throws EX
            {
                return transformation.transformEpsilon( param );
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <EX extends Exception> TermTransformation<TermVisitor<EX>, Void, EX> visit()
    {
        return VISIT;
    }

    private static final TermTransformation VISIT = new TermTransformation<TermVisitor, Void, Exception>()
    {
        @Override
        public Void transformAlternatives( TermVisitor visitor, Alternatives alternatives )
                throws Exception
        {
            visitor.visitAlternatives( alternatives );
            return null;
        }

        @Override
        public Void transformSequence( TermVisitor visitor, Sequence sequence )
                throws Exception
        {
            visitor.visitSequence( sequence );
            return null;
        }

        @Override
        public Void transformLiteral( TermVisitor visitor, Literal value )
                throws Exception
        {
            visitor.visitLiteral( value );
            return null;
        }

        @Override
        public Void transformNonTerminal( TermVisitor visitor, NonTerminal nonTerminal )
                throws Exception
        {
            visitor.visitNonTerminal( nonTerminal );
            return null;
        }

        @Override
        public Void transformOptional( TermVisitor visitor, Optional optional )
                throws Exception
        {
            visitor.visitOptional( optional );
            return null;
        }

        @Override
        public Void transformRepetition( TermVisitor visitor, Repetition repetition )
                throws Exception
        {
            visitor.visitRepetition( repetition );
            return null;
        }

        @Override
        public Void transformEpsilon( TermVisitor visitor )
                throws Exception
        {
            visitor.visitEpsilon();
            return null;
        }

        @Override
        public Void transformCharacters( TermVisitor visitor, CharacterSet characters )
                throws Exception
        {
            visitor.visitCharacters( characters );
            return null;
        }
    };
}
