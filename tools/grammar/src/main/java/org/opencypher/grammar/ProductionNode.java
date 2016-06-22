/*
 * Copyright (c) 2015-2016 "Neo Technology,"
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

import static java.util.Collections.emptyList;

@Element(uri = Grammar.XML_NAMESPACE, name = "production")
final class ProductionNode extends Located implements Production
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
    boolean legacy;
    private List<NonTerminal> references;

    public ProductionNode( Root root )
    {
        this.vocabulary = root.language;
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
}
