package org.opencypher.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.XmlParser;
import org.xml.sax.SAXException;

import static java.util.Objects.requireNonNull;

import static org.opencypher.tools.xml.XmlParser.xmlParser;

@Element(uri = Grammar.XML_NAMESPACE, name = "grammar")
class Root implements Iterable<ProductionNode>
{
    enum ResolutionOption
    {
        ALLOW_ROOTLESS,
        SKIP_UNUSED_PRODUCTIONS,
        IGNORE_UNUSED_PRODUCTIONS,
    }

    static final XmlParser<Root> XML = xmlParser( Root.class );

    @Attribute
    String language;
    @Attribute(name = "case-sensitive", optional = true)
    boolean caseSensitive = true;
    private final Map<String, ProductionNode> productions = new LinkedHashMap<>();
    final Map<String, VocabularyReference> referencedFiles = new HashMap<>();

    @Child
    void add( ProductionNode production )
    {
        if ( CharacterSetNode.isReserved( production.name ) )
        {
            throw new IllegalArgumentException( "Invalid production name: '" + production.name +
                                                "', it is reserved for well known character sets." );
        }
        if ( productions.put( production.name, production ) != null )
        {
            throw new IllegalArgumentException( "Duplicate definition of '" + production.name + "' production" );
        }
    }

    @Child
    void addVocabulary( VocabularyReference vocabulary ) throws ParserConfigurationException, SAXException, IOException
    {
        referencedFiles.put( vocabulary.path(), vocabulary );
        for ( ProductionNode production : vocabulary.resolve() )
        {
            add( production );
        }
    }

    final Grammar resolve( ResolutionOption... config )
    {
        Set<ResolutionOption> options = EnumSet.noneOf( ResolutionOption.class );
        if ( config != null )
        {
            Collections.addAll( options, config );
        }
        Dependencies dependencies = new Dependencies();
        Set<String> unused = new HashSet<>( productions.keySet() );
        // find the root production
        if ( !unused.remove( language ) )
        {
            if ( options.contains( ResolutionOption.ALLOW_ROOTLESS ) )
            {
                productions.values().stream()
                           .filter( production -> Objects.equals( production.vocabulary, language ) )
                           .forEach( production -> unused.remove( production.name ) );
            }
            else
            {
                dependencies.missingProduction( language, new ProductionNode( this ) );
            }
        }
        // Resolve non-terminals in all productions
        ProductionResolver resolver = new ProductionResolver( productions, dependencies, unused );
        for ( ProductionNode production : productions.values() )
        {
            production.resolve( resolver );
        }
        // check for errors
        dependencies.reportMissingProductions();
        // filter out unused productions
        if ( !unused.isEmpty() )
        {
            if ( options.contains( ResolutionOption.SKIP_UNUSED_PRODUCTIONS ) )
            {
                while ( false && !unused.isEmpty() )
                {
                    for ( String name : new ArrayList<>( unused ) )
                    {
                        ProductionNode production = productions.remove( name );
                    }
                }
            }
            else if ( !options.contains( ResolutionOption.IGNORE_UNUSED_PRODUCTIONS ) )
            {
                System.err.println( "WARNING! Unused productions:" );
                for ( String name : unused )
                {
                    System.err.println( "\t" + name );
                }
            }
        }
        // sort productions
        ArrayList<ProductionNode> ordered = new ArrayList<>( productions.values() );
        for ( VocabularyReference reference : new ArrayList<>( referencedFiles.values() ) )
        {
            reference.flattenTo( referencedFiles );
        }
        ordered.sort( Located.comparator( referencedFiles ) );
        Map<String, ProductionNode> productions = new LinkedHashMap<>();
        for ( ProductionNode production : ordered )
        {
            productions.put( production.name, production );
        }
        // create grammar
        return new Grammar( this, productions );
    }

    @Override
    public Iterator<ProductionNode> iterator()
    {
        return productions.values().iterator();
    }

    private static final class Grammar implements org.opencypher.grammar.Grammar
    {
        private final String language;
        private final Map<String, ProductionNode> productions;
        private final boolean caseSensitive;

        Grammar( Root root, Map<String, ProductionNode> productions )
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
        public <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX
        {
            for ( ProductionNode production : productions.values() )
            {
                production.accept( visitor );
            }
        }

        @Override
        public boolean hasProduction( String name )
        {
            return productions.containsKey( name );
        }

        @Override
        public <P, R, EX extends Exception> R transform(
                String name, ProductionTransformation<P, R, EX> transformation, P param ) throws EX
        {
            ProductionNode production = productions.get( name );
            if ( production == null )
            {
                throw new IllegalArgumentException(
                        "The grammar for " + language + " has no production for: " + name );
            }
            return production.transform( transformation, param );
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
