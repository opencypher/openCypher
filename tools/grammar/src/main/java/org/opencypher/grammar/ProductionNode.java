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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

import static java.util.Collections.emptyList;

@Element(uri = Grammar.XML_NAMESPACE, name = "production")
final class ProductionNode extends Located implements Production, ReferenceTarget
{
    final String vocabulary;
    @Attribute
    String name;
    @Attribute(optional = true, uri = Grammar.SCOPE_XML_NAMESPACE, name = "rule")
    ScopeRule scopeRule;
    Node definition;
    String description;
    @Attribute(uri = Grammar.RAILROAD_XML_NAMESPACE, optional = true)
    boolean skip, inline;
    @Attribute(uri = Grammar.OPENCYPHER_XML_NAMESPACE, optional = true)
    boolean legacy, lexer, bnfsymbols;
    private List<NonTerminal> references;

    public ProductionNode( Root root )
    {
        this( root.language );
    }

    public ProductionNode( GrammarAnnotation annotation )
    {
        this( annotation.language() );
    }

    public ProductionNode( GrammarAnnotation.Replace replace )
    {
        this( replace.grammar.language() );
    }

    ProductionNode replace( Node definition )
    {
        if ( references != null )
        {
            throw new IllegalStateException( "Cannot replace resolved grammar." );
        }
        ProductionNode node = new ProductionNode( vocabulary );
        node.name = this.name;
        node.definition = definition;
        node.scopeRule = this.scopeRule;
        node.description = this.description;
        node.skip = this.skip;
        node.inline = this.inline;
        node.legacy = this.legacy;
        node.lexer = this.lexer;
        node.bnfsymbols = this.bnfsymbols;
        return node;
    }

    private ProductionNode( String vocabulary )
    {
        this.vocabulary = vocabulary;
    }

    @Child({AlternativesNode.class, SequenceNode.class, LiteralNode.class, CharacterSetNode.class,
            NonTerminalNode.class, OptionalNode.class, RepetitionNode.class})
    void add( Node node )
    {
        definition = SequenceNode.implicit( definition, node.replaceWithVerified() );
    }

    @Child
    void add( Description description )
    {
        if ( this.description != null )
        {
            this.description = description.appendTo( this.description );
        }
        else
        {
            this.description = description.toString();
        }
    }

    @Child
    final void literal( char[] buffer, int start, int length )
    {
        LiteralNode.fromCharacters( buffer, start, length, this::add );
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public String description()
    {
        return description;
    }

    @Override
    public <Scope> Scope scope( Scope scope, ScopeRule.Transformation<Scope> transition )
    {
        return scopeRule == null ? scope : scopeRule.transform( scope, transition );
    }

    <EX extends Exception> void accept( ProductionVisitor<EX> visitor ) throws EX
    {
        visitor.visitProduction( this );
    }

    <R, P, EX extends Exception> R transform( ProductionTransformation<P, R, EX> transformation, P param ) throws EX
    {
        return transformation.transformProduction( param, this );
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return definition().transform( transformation, param );
    }

    @Override
    public boolean skip()
    {
        return skip;
    }

    @Override
    public boolean inline()
    {
        return inline;
    }

    @Override
    public boolean legacy()
    {
        return legacy;
    }

    @Override
    public boolean lexer()
    {
        return lexer;
    }

    public boolean bnfsymbols() {
        return bnfsymbols;
    }

    void addReference( NonTerminalNode nonTerminal )
    {
        if ( references == null )
        {
            references = new ArrayList<>();
        }
        references.add( nonTerminal );
    }

    @Override
    public Collection<NonTerminal> references()
    {
        return references == null ? emptyList() : references;
    }

    @Override
    public Node definition()
    {
        return definition == null ? Node.epsilon() : definition;
    }

    void resolve( ProductionResolver resolver )
    {
        if ( definition != null )
        {
            definition.resolve( this, resolver );
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode( name );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj.getClass() != ProductionNode.class )
        {
            return false;
        }
        ProductionNode that = (ProductionNode) obj;
        return Objects.equals( this.name, that.name ) &&
               Objects.equals( this.vocabulary, that.vocabulary ) &&
               Objects.equals( this.scopeRule, that.scopeRule ) &&
               Objects.equals( this.description, that.description ) &&
               Objects.equals( this.definition, that.definition );
    }

    @Override
    public String toString()
    {
        return "Production{" + vocabulary + " / " + name + " = " + definition + "}";
    }

    @Override
    public ProductionNode production()
    {
        return this;
    }

    @Override
    public <T> T resolve( NonTerminalNode nonTerminal, NonTerminal.ReferenceResolver<T> resolver )
    {
        return resolver.resolveProduction( this );
    }
}
