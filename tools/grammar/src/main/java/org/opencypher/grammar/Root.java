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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Comment;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.XmlParser;
import org.xml.sax.SAXException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import static org.opencypher.tools.xml.XmlParser.xmlParser;

@Element(uri = Grammar.XML_NAMESPACE, name = "grammar")
class Root extends ProtoGrammar implements Iterable<ProductionNode>
{
    static final XmlParser<Root> XML = xmlParser( Root.class );

    @Attribute
    String language;
    private final Map<String, ProductionNode> productions = new LinkedHashMap<>();
    private StringBuilder header;
    final Map<String, VocabularyReference> referencedFiles = new HashMap<>();

    @Child
    @Override
    void add( ProductionNode production )
    {
        if ( productions.put( productionKey( production.name ), production ) != null )
        {
            throw new IllegalArgumentException( "Duplicate definition of '" + production.name + "' production" );
        }
    }

    private String productionKey( String name )
    {
        if ( CharacterSetNode.isReserved( name ) )
        {
            throw new IllegalArgumentException( "Invalid production name: '" + name +
                    "', it is reserved for well known character sets." );
        }
        return name.toLowerCase();
    }

    // this is probably naughty, but we need to mark the production, but not a reference
    // special
    void markAsBnfSymbols(String productionName)
    {
        ProductionNode production = productions.get(productionName.toLowerCase());
        if (production == null) {
            throw new IllegalStateException("Can't find production " + productionName); 
        }
        production.bnfsymbols = true;
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

    @Child(Comment.Header.class)
    void addHeader( char[] buffer, int start, int length )
    {
        if ( header == null )
        {
            header = new StringBuilder( length );
        }
        Description.extract( header, buffer, start, length );
    }

    @Override
    String language()
    {
        return language;
    }

    @Override
    ProductionNode production( String name )
    {
        return productions.get( name.toLowerCase() );
    }

    @Override
    void setName( String name )
    {
        language = name;
    }

    final Grammar resolve( Grammar.Resolver referenceResolver, Set<ResolutionOption> options )
    {
        Dependencies dependencies = new Dependencies();
        Set<String> unused = productions.values().stream().map( node -> node.name ).collect( toSet() );
        // find the root production
        if ( !unused.remove( language ) )
        {
            if ( options.contains( ProtoGrammar.ResolutionOption.ALLOW_ROOTLESS ) )
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
        // Filter out legacy productions
        final Set<String> legacyProductions = new HashSet<>();
        final Map<String,ProductionNode> filteredProductions;
        if ( !options.contains( ProtoGrammar.ResolutionOption.INCLUDE_LEGACY ) )
        {
            filteredProductions = new LinkedHashMap<>();
            productions.values().stream()
                    .filter( production1 -> !production1.legacy() )
                    .forEach( production -> filteredProductions.put( production.name().toLowerCase(), production ) );
            productions.values().stream().filter( ProductionNode::legacy ).forEach( production -> legacyProductions.add( production.name.toLowerCase() ) );
        }
        else
        {
            filteredProductions = productions;
        }
        // Resolve non-terminals in all productions; remove references to legacy productions
        ProductionResolver resolver = new ProductionResolver( referenceResolver, filteredProductions, dependencies, unused, options, legacyProductions );
        for ( ProductionNode production : filteredProductions.values() )
        {
            production.resolve( resolver );
        }
        // check for errors
        dependencies.reportMissingProductions();
        // report unused productions
        if ( !unused.isEmpty() && !legacyProductions.containsAll( unused.stream().map( String::toLowerCase ).collect( toSet()) ) )
        {
            if ( !options.contains( ProtoGrammar.ResolutionOption.IGNORE_UNUSED_PRODUCTIONS ) )
            {
                System.err.println( "WARNING! Unused productions:" );
                for ( String name : unused )
                {
                    if ( !legacyProductions.contains( name.toLowerCase() ) )
                    {
                        System.err.println( "\t" + name );
                    }
                }
            }
        }
        // sort productions
        ArrayList<ProductionNode> ordered = new ArrayList<>( filteredProductions.values() );
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

    @Override
    void apply( Patch patch )
    {
        patch.apply( new Mutator()
        {
            @Override
            <P, EX extends Exception> void replaceProduction( String production, ProductionTransformation<P,ProductionNode,EX> transformation, P param ) throws EX
            {
                try
                {
                    productions.compute( productionKey( production ), ( key, old ) ->
                    {
                        if ( old == null )
                        {
                            throw new IllegalArgumentException( "No such production: " + production );
                        }
                        try
                        {
                            return old.transform( transformation, param );
                        }
                        catch ( RuntimeException re )
                        {
                            throw re;
                        }
                        catch ( Exception ex )
                        {
                            throw new ExceptionVector( ex );
                        }
                    } );
                }
                catch ( ExceptionVector ev )
                {
                    @SuppressWarnings( "unchecked" ) EX ex = (EX) ev.getCause();
                    throw ex;
                }
            }

            @Override
            void removeProduction( String production )
            {
                if ( null == productions.remove( productionKey( production ) ) )
                {
                    throw new IllegalArgumentException( "No such production: " + production );
                }
            }

            @Override
            Collection<String> productions()
            {
                return productions.values().stream().map( node -> node.name ).collect( Collectors.toSet() );
            }
        } );
    }

    private static class ExceptionVector extends Error
    {
        ExceptionVector( Exception exception )
        {
            super( exception );
        }

        @Override
        public Throwable fillInStackTrace()
        {
            return this;
        }
    }

    private static final class Grammar implements org.opencypher.grammar.Grammar
    {
        private final String language;
        private final Map<String, ProductionNode> productions;
        private final String header;

        Grammar( Root root, Map<String, ProductionNode> productions )
        {
            this.language = requireNonNull( root.language, "language" );
            this.header = root.header == null ? null : root.header.toString();
            this.productions = productions;
        }

        @Override
        public String language()
        {
            return language;
        }

        @Override
        public String header()
        {
            return header;
        }

        @Override
        public <EX extends Exception> void accept( ProductionVisitor<EX> visitor ) throws EX
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
        public <P, A, R, T, EX extends Exception> T transform(
                ProductionTransformation<P, R, EX> transformation, P param, Collector<R, A, T> collector ) throws EX
        {
            BiConsumer<A, R> accumulator = collector.accumulator();
            A result = collector.supplier().get();
            for ( ProductionNode production : productions.values() )
            {
                accumulator.accept( result, production.transform( transformation, param ) );
            }
            return collector.finisher().apply( result );
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
            return language.equals( that.language ) &&
                   productions.equals( that.productions );
        }
    }
}
