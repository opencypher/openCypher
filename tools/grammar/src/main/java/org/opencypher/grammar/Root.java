package org.opencypher.grammar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.XmlParser;

import static java.util.Objects.requireNonNull;

import static org.opencypher.tools.xml.XmlParser.xmlParser;

@Element(uri = Grammar.XML_NAMESPACE, name = "grammar")
class Root
{
    static final XmlParser<Root> XML = xmlParser( Root.class );

    @Attribute
    String language;
    @Attribute(name = "case-sensitive", optional = true)
    boolean caseSensitive = true;
    private final Map<String, Production> productions = new LinkedHashMap<>();

    @Child
    void add( Production production )
    {
        if ( productions.put( production.name, production ) != null )
        {
            throw new IllegalArgumentException( "Duplicate definition of '" + production.name + "' production" );
        }
    }

    final Grammar resolve( Function<Map<String, Production>, Map<String, Production>> copy )
    {
        LogicalErrors errors = new LogicalErrors();
        for ( Production production : productions.values() )
        {
            production.resolve( productions, errors );
        }
        errors.report();
        return new Grammar( this, copy.apply( productions ) );
    }

    private static final class Grammar implements org.opencypher.grammar.Grammar
    {
        private final String language;
        private final Map<String, Production> productions;
        private final boolean caseSensitive;

        Grammar( Root root, Map<String, Production> productions )
        {
            this.language = requireNonNull( root.language, "language" );
            this.caseSensitive = root.caseSensitive;
            this.productions = productions;
        }

        @Override
        public String language()
        {
            return language;
        }

        @Override
        public boolean caseSensitiveByDefault()
        {
            return caseSensitive;
        }

        @Override
        public String productionDescription( String name )
        {
            Production production = productions.get( name );
            if ( production == null )
            {
                throw new NoSuchElementException();
            }
            return production.description;
        }

        @Override
        public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
        {
            for ( Production production : productions.values() )
            {
                production.accept( visitor );
            }
        }

        @Override
        public String toString()
        {
            return "Grammar{" + language + "}";
        }

        @Override
        public int hashCode()
        {
            return language.hashCode();
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj.getClass() != Grammar.class )
            {
                return false;
            }
            Grammar that = (Grammar) obj;
            return this.caseSensitive == that.caseSensitive &&
                   language.equals( that.language ) &&
                   productions.equals( that.productions );
        }
    }
}
