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
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.tools.xml.XmlFile;
import org.opencypher.tools.xml.XmlParser;
import org.xml.sax.SAXException;

import static java.util.Objects.requireNonNull;

public interface Grammar
{
    String XML_NAMESPACE = "http://opencypher.org/grammar";
    String SCOPE_XML_NAMESPACE = "http://opencypher.org/scope";
    String GENERATOR_XML_NAMESPACE = "http://opencypher.org/stringgeneration";
    String RAILROAD_XML_NAMESPACE = "http://opencypher.org/railroad";
    String OPENCYPHER_XML_NAMESPACE = "http://opencypher.org/opencypher";
    String ANNOTATION_XML_NAMESPACE = XML_NAMESPACE + "/annotation";

    static Grammar parseXML( Path input, ParserOption... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        return ProtoGrammar.parse( input, ParserOption.xml( options ) )
                       .resolve( ProtoGrammar.NO_RESOLVER, ParserOption.resolve( options ) );
    }

    static Grammar parseXML( Reader input, ParserOption... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        return ProtoGrammar.parse( input, ParserOption.xml( options ) )
                       .resolve( ProtoGrammar.NO_RESOLVER, ParserOption.resolve( options ) );
    }

    static Grammar parseXML( InputStream input, ParserOption... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        return ProtoGrammar.parse( input, ParserOption.xml( options ) )
                       .resolve( ProtoGrammar.NO_RESOLVER, ParserOption.resolve( options ) );
    }

    String language();

    String header();

    <EX extends Exception> void accept( ProductionVisitor<EX> visitor ) throws EX;

    boolean hasProduction( String name );

    <P, R, EX extends Exception> R transform( String production, ProductionTransformation<P, R, EX> xform, P param )
            throws EX;

    default <P, EX extends Exception> void transform( ProductionTransformation<P, Void, EX> transformation, P param )
            throws EX
    {
        transform( transformation, param, Collector.of( () -> null, ( a, b ) -> {}, ( a, b ) -> null ) );
    }

    <P, A, R, T, EX extends Exception> T transform(
            ProductionTransformation<P, R, EX> transformation, P param, Collector<R, A, T> collector ) throws EX;

    default boolean any( Predicate<Production> predicate )
    {
        return transform(
                (ProductionTransformation<Void,Boolean,RuntimeException>) ( param, production ) -> predicate.test( production ),
                null,
                Collectors.reducing( Boolean.FALSE, ( a, b ) -> a || b ) );
    }

    default Production production( String name )
    {
        return transform( name, ( param, production ) -> production, null );
    }

    static Builder grammar( String language, Option... options )
    {
        Builder builder = new Builder( language );
        if ( options != null )
        {
            for ( Option option : options )
            {
                option.apply( builder );
            }
        }
        return builder;
    }

    static Term epsilon()
    {
        return Node.epsilon();
    }
    

    static Term caseInsensitive( String value )
    {
        LiteralNode literal = new LiteralNode();
        literal.value = requireNonNull( value, "literal value" );
        literal.caseSensitive = false;
        return literal;
    }

    static Term literal( String value )
    {
        LiteralNode literal = new LiteralNode();
        literal.value = requireNonNull( value, "literal value" );
        literal.caseSensitive = true;
        return literal;
    }

    final class CharacterSet extends CharacterSetNode
    {
        private CharacterSet( String set )
        {
            set( set );
        }

        public CharacterSet except( int... codePoints )
        {
            for ( int codePoint : codePoints )
            {
                exclude( codePoint );
            }
            return this;
        }
    }

    static CharacterSet charactersOfSet( String name )
    {
        return new CharacterSet( requireNonNull( name, "character set name" ) );
    }

    static CharacterSet anyCharacter()
    {
        return new CharacterSet( CharacterSetNode.DEFAULT_SET );
    }

    static Term nonTerminal( String production )
    {
        NonTerminalNode nonTerminal = new NonTerminalNode();
        nonTerminal.ref = production;
        return nonTerminal;
    }

    static Term optional( Term first, Term... more )
    {
        return sequence( first, more ).addTo( new OptionalNode() );
    }

    static Term oneOf( Term first, Term... alternatives )
    {
        Objects.requireNonNull( first, "first term" );
        if ( alternatives == null || alternatives.length == 0 )
        {
            return first;
        }
        return new AlternativesNode().addAll( first, alternatives );
    }

    static Term zeroOrMore( Term first, Term... more )
    {
        return sequence( first, more ).addTo( new RepetitionNode() );
    }

    static Term oneOrMore( Term first, Term... more )
    {
        return atLeast( 1, first, more );
    }

    static Term atLeast( int times, Term first, Term... more )
    {
        RepetitionNode repetition = new RepetitionNode();
        repetition.min = times;
        return sequence( first, more ).addTo( repetition );
    }

    static Term repeat( int times, Term first, Term... more )
    {
        RepetitionNode repetition = new RepetitionNode();
        repetition.min = repetition.max = times;
        return sequence( first, more ).addTo( repetition );
    }

    static Term repeat( int min, int max, Term first, Term... more )
    {
        RepetitionNode repetition = new RepetitionNode();
        repetition.min = min;
        repetition.max = max;
        return sequence( first, more ).addTo( repetition );
    }

    static Term sequence( Term first, Term... more )
    {
        Objects.requireNonNull( first, "first term" );
        if ( more == null || more.length == 0 )
        {
            return first;
        }
        return new SequenceNode().addAll( first, more );
    }

    class Builder extends Root
    {
        public enum Option
        {
            IGNORE_UNUSED_PRODUCTIONS( ProtoGrammar.ResolutionOption.IGNORE_UNUSED_PRODUCTIONS ),
            ALLOW_ROOTLESS( ProtoGrammar.ResolutionOption.ALLOW_ROOTLESS );
            private final ProtoGrammar.ResolutionOption option;

            Option( ProtoGrammar.ResolutionOption option )
            {
                this.option = option;
            }
        }

        private Builder( String language )
        {
            this.language = requireNonNull( language, "language name" );
        }

        public Builder production( String name, Term first, Term... alternatives )
        {
            return production(name, null, first, alternatives);
        }

        public Builder production( String name, String description, Term first, Term... alternatives )
        {
            ProductionNode production = new ProductionNode( this );
            production.name = requireNonNull( name, "name" );
            if (description != null) {
                production.description = description;
            }
            Grammar.oneOf( first, alternatives ).addTo( production );
            add( production );
            return this;
        }

        public Grammar build( Option... options )
        {
            return resolve( ProtoGrammar.NO_RESOLVER,
                    (options == null ? Stream.<Option>empty() : Stream.of( options ))
                            .map( x -> x.option )
                            .toArray( ProtoGrammar.ResolutionOption[]::new ) );
        }
    }

    abstract class Term
    {
        public final <EX extends Exception> void accept( TermVisitor<EX> visitor ) throws EX
        {
            transform( Node.visit(), visitor );
        }

        public abstract <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param )
                throws EX;

        abstract Container addTo( Container container );

        abstract Sequenced addTo( Sequenced sequenced );

        abstract ProductionNode addTo( ProductionNode production );
    }

    abstract class Option
    {
        abstract void apply( Root grammar );
    }

    enum ParserOption
    {
        FAIL_ON_UNKNOWN_XML_ATTRIBUTE( XmlParser.Option.FAIL_ON_UNKNOWN_ATTRIBUTE ),
        SKIP_UNUSED_PRODUCTIONS( ProtoGrammar.ResolutionOption.SKIP_UNUSED_PRODUCTIONS ),
        ALLOW_ROOTLESS_GRAMMAR( ProtoGrammar.ResolutionOption.ALLOW_ROOTLESS ),
        INCLUDE_LEGACY( ProtoGrammar.ResolutionOption.INCLUDE_LEGACY );

        private final Object option;

        ParserOption( ProtoGrammar.ResolutionOption option )
        {
            this.option = option;
        }

        ParserOption( XmlParser.Option option )
        {
            this.option = option;
        }

        public static ParserOption[] from( Properties properties )
        {
            Set<ParserOption> result = EnumSet.noneOf( ParserOption.class );
            for ( ParserOption option : values() )
            {
                if ( Boolean.parseBoolean( properties.getProperty( option.name() ) ) )
                {
                    result.add( option );
                }
            }
            return result.toArray( new ParserOption[result.size()] );
        }

        private static XmlParser.Option[] xml( ParserOption[] options )
        {
            return options( XmlParser.Option.class, options );
        }

        private static ProtoGrammar.ResolutionOption[] resolve( ParserOption[] options )
        {
            return options( ProtoGrammar.ResolutionOption.class, options );
        }

        private static <T> T[] options( Class<T> type, ParserOption... options )
        {
            if ( options == null || options.length == 0 )
            {
                return null;
            }
            List<T> collected = Stream.of( options )
                                      .flatMap( ( the ) -> type.isInstance( the.option )
                                                           ? Stream.of( type.cast( the.option ) )
                                                           : Stream.empty() )
                                      .collect( Collectors.<T>toList() );
            @SuppressWarnings("unchecked")
            T[] result = (T[]) Array.newInstance( type, collected.size() );
            return collected.toArray( result );
        }
    }

    final class Unresolved
    {
        private volatile ProtoGrammar grammar;

        public static Unresolved parseXML( XmlFile xml ) throws ParserConfigurationException, SAXException, IOException
        {
            return new Unresolved( ProtoGrammar.parse( xml ) );
        }

        public Grammar resolve( Resolver resolver )
        {
            ProtoGrammar grammar;
            synchronized ( this )
            {
                grammar = grammar();
//                this.grammar = null;
            }
            return grammar.resolve( resolver );
        }

        public synchronized Production production( String name )
        {
            return new Production( grammar().production( name ) );
        }

        private Unresolved( ProtoGrammar grammar )
        {
            this.grammar = grammar;
        }

        private ProtoGrammar grammar()
        {
            ProtoGrammar grammar = this.grammar;
            if ( grammar == null )
            {
                throw new IllegalStateException( "Grammar has been resolved" );
            }
            return grammar;
        }

        public static final class Production
        {
            final ProductionNode node;

            private Production( ProductionNode node )
            {
                this.node = node;
            }
        }
    }

    interface Resolver
    {
        Unresolved.Production resolve( ForeignReference reference );

        static Resolver combine( Resolver... resolvers )
        {
            if ( resolvers == null )
            {
                return combine();
            }
            else if ( resolvers.length == 1 )
            {
                return resolvers[0];
            }
            return referece ->
            {
                for ( Resolver resolver : resolvers )
                {
                    Unresolved.Production result = referece.resolve( resolver );
                    if ( result != null )
                    {
                        return result;
                    }
                }
                return null;
            };
        }

        interface WG3 extends Resolver
        {
            @Override
            default Unresolved.Production resolve( ForeignReference referece )
            {
                return referece.resolve( this );
            }

            Unresolved.Production resolve( String standard, String part, String nonTerminal );
        }
    }
}
