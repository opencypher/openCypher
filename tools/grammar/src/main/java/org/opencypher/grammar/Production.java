package org.opencypher.grammar;

import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "production")
final class Production extends Located
{
    final String vocabulary;
    @Attribute
    String name;
    @Attribute(optional = true, uri = Grammar.SCOPE_XML_NAMESPACE, name = "rule")
    ScopeRule scopeRule;
    Node definition;
    String description;

    public Production( Root root )
    {
        this.vocabulary = root.language;
    }

    @Child({Alternatives.class, Sequence.class, Literal.class, Characters.class, NonTerminal.class, Optional.class,
            Repetition.class})
    void add( Node node )
    {
        definition = Sequence.implicit( definition, node.replaceWithVerified() );
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
        Literal.fromCharacters( buffer, start, length, this::add );
    }

    <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
    {
        visitor.visitProduction( name, definition() );
    }

    <R, P, EX extends Exception> R transform( ProductionTransformation<P, R, EX> transformation, P param ) throws EX
    {
        return transformation.transformProduction( param, name, definition() );
    }

    <T, P, EX extends Exception> T transformNonTerminal( TermTransformation<P, T, EX> transformation, P param )
            throws EX
    {
        return transformation.transformNonTerminal( param, name, definition() );
    }

    private Node definition()
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
        if ( obj.getClass() != Production.class )
        {
            return false;
        }
        Production that = (Production) obj;
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
