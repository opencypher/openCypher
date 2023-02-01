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
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.XmlFile;
import org.opencypher.tools.xml.XmlParser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import javax.xml.parsers.ParserConfigurationException;

import static org.opencypher.tools.xml.XmlParser.xmlParser;

@Element( uri = Grammar.ANNOTATION_XML_NAMESPACE, name = "GrammarAnnotation" )
class GrammarAnnotation extends ProtoGrammar
{
    static final XmlParser<GrammarAnnotation> XML = xmlParser( GrammarAnnotation.class );
    private ProtoGrammar grammar;
    private String name;

    @Attribute
    void grammar( XmlFile grammar ) throws ParserConfigurationException, SAXException, IOException
    {
        this.grammar = parse( grammar );
        if ( name != null )
        { // there are no guarantees about assignment order of attributes
            this.grammar.setName( name );
        }
    }

    @Attribute( optional = true )
    void name( String name )
    {
        if ( grammar != null )
        {
            grammar.setName( name );
        }
        else
        { // there are no guarantees about assignment order of attributes
            this.name = name;
        }
    }

    @Override
    String language()
    {
        return grammar.language();
    }

    @Override
    ProductionNode production( String name )
    {
        return grammar.production( name );
    }

    @Override
    void setName( String name )
    {
        grammar.setName( name );
    }

    @Override
    Grammar resolve( Grammar.Resolver resolver, Set<ResolutionOption> options )
    {
        return grammar.resolve( resolver, options );
    }

    @Child( {SkipProduction.class, InlineProduction.class, SkipReference.class, InlineReference.class, NonTerminalTitle.class, Replace.class, Remove.class,
            Recursively.class, MarkAndSweep.class} )
    @Override
    void apply( Patch patch )
    {
        grammar.apply( patch );
    }

    @Child
    @Override
    void add( ProductionNode production )
    {
        grammar.add( production );
    }

    @Element( uri = Grammar.ANNOTATION_XML_NAMESPACE, name = "replace" )
    static class Replace extends Patch
    {
        final List<ProductionNode> productions = new ArrayList<>();
        final ProtoGrammar grammar;

        Replace( GrammarAnnotation parent )
        {
            this.grammar = parent.grammar;
        }

        @Override
        void apply( Mutator mutator )
        {
            for ( ProductionNode production : productions )
            {
                mutator.replaceProduction( production.name, ( replacement, current ) -> replacement, production );
            }
        }

        @Child
        void add( ProductionNode production )
        {
            productions.add( production );
        }
    }

    @Element( uri = Grammar.ANNOTATION_XML_NAMESPACE, name = "remove" )
    static class Remove extends Patch
    {
        @Attribute
        String production;

        @Override
        void apply( Mutator mutator )
        {
            mutator.removeProduction( production );
        }
    }

    @Element( uri = Grammar.ANNOTATION_XML_NAMESPACE, name = "recursively" )
    static class Recursively extends TransitivePatch
    {
        @Attribute
        String from;
        @Attribute( uri = Grammar.OPENCYPHER_XML_NAMESPACE, optional = true )
        Boolean lexer;
        @Attribute( uri = Grammar.RAILROAD_XML_NAMESPACE, optional = true )
        Boolean skip, inline;

        @Override
        void apply( Mutator mutator )
        {
            applyTransitive( from, mutator );
        }

        @Override
        void applyTo( ProductionNode production )
        {
            if ( lexer != null )
            {
                production.lexer = lexer;
            }
            if ( skip != null )
            {
                production.skip = skip;
            }
            if ( inline != null )
            {
                production.inline = inline;
            }
        }
    }

    @Element( uri = Grammar.ANNOTATION_XML_NAMESPACE, name = "mark-and-sweep" )
    static class MarkAndSweep extends TransitivePatch
    {
        @Attribute
        String root;

        @Override
        void apply( Mutator mutator )
        {
            Set<String> mark = applyTransitive( root, mutator );
            for ( String production : mutator.productions() )
            {
                if ( !mark.contains( production ) )
                {
                    mutator.removeProduction( production );
                }
            }
        }

        @Override
        void applyTo( ProductionNode production )
        {
        }
    }

    @Element( uri = Grammar.RAILROAD_XML_NAMESPACE, name = "skip" )
    static class SkipProduction extends SingleProductionPatch
    {
        @Override
        ProductionNode production( ProductionNode production )
        {
            production.skip = true;
            return production;
        }
    }

    @Element( uri = Grammar.RAILROAD_XML_NAMESPACE, name = "inline" )
    static class InlineProduction extends SingleProductionPatch
    {
        @Override
        ProductionNode production( ProductionNode production )
        {
            production.inline = true;
            return production;
        }
    }

    @Element( uri = Grammar.RAILROAD_XML_NAMESPACE, name = "skipNonTerminal" )
    static class SkipReference extends SingleProductionPatch
    {
        @Attribute
        String ref;

        @Override
        Node nonTerminal( NonTerminalNode nonTerminal )
        {
            if ( ref.equals( nonTerminal.ref ) )
            {
                nonTerminal.skip = true;
            }
            return nonTerminal;
        }
    }

    @Element( uri = Grammar.RAILROAD_XML_NAMESPACE, name = "inlineNonTerminal" )
    static class InlineReference extends SingleProductionPatch
    {
        @Attribute
        String ref;

        @Override
        Node nonTerminal( NonTerminalNode nonTerminal )
        {
            if ( ref.equals( nonTerminal.ref ) )
            {
                nonTerminal.inline = true;
            }
            return nonTerminal;
        }
    }

    @Element( uri = Grammar.RAILROAD_XML_NAMESPACE, name = "nonTerminalTitle" )
    static class NonTerminalTitle extends SingleProductionPatch
    {
        @Attribute
        String ref;

        @Attribute
        String title;

        @Override
        Node nonTerminal( NonTerminalNode nonTerminal )
        {
            if ( ref.equals( nonTerminal.ref ) )
            {
                nonTerminal.title = title;
            }
            return nonTerminal;
        }
    }

    static abstract class SingleProductionPatch extends Patch
            implements ProductionTransformation<Void,ProductionNode,RuntimeException>, TermTransformation<Void,Node,RuntimeException>
    {
        @Attribute
        String production;

        @Override
        void apply( Mutator mutator )
        {
            mutator.replaceProduction( production, this, null );
        }

        @Override
        public final ProductionNode transformProduction( Void param, Production production ) throws RuntimeException
        {
            if ( production instanceof ProductionNode )
            {
                ProductionNode node = (ProductionNode) production;
                return production( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle productions of type " + production.getClass() );
            }
        }

        ProductionNode production( ProductionNode production )
        {
            Node definition = production.transform( (TermTransformation<Void,Node,RuntimeException>) this, null );
            if ( definition != production.definition )
            {
                return production.replace( definition );
            }
            else
            {
                return production;
            }
        }

        @Override
        public final Node transformAlternatives( Void param, Alternatives alternatives ) throws RuntimeException
        {
            if ( alternatives instanceof AlternativesNode )
            {
                AlternativesNode node = (AlternativesNode) alternatives;
                return alternatives( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle nodes of of type " + alternatives.getClass() );
            }
        }

        Node alternatives( AlternativesNode alternatives )
        {
            List<Node> terms = transformCollection( alternatives );
            if ( terms != null )
            {
                AlternativesNode replacement = new AlternativesNode();
                replacement.nodes.addAll( terms );
                return replacement;
            }
            else
            {
                return alternatives;
            }
        }

        @Override
        public final Node transformSequence( Void param, Sequence sequence ) throws RuntimeException
        {
            if ( sequence instanceof SequenceNode )
            {
                SequenceNode node = (SequenceNode) sequence;
                return sequence( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle nodes of of type " + sequence.getClass() );
            }
        }

        Node sequence( SequenceNode sequence )
        {
            List<Node> terms = transformCollection( sequence );
            if ( terms != null )
            {
                SequenceNode replacement = new SequenceNode();
                replacement.nodes.addAll( terms );
                return replacement;
            }
            else
            {
                return sequence;
            }
        }

        private List<Node> transformCollection( Container container )
        {
            List<Node> terms = null;
            int term = 0;
            for ( Grammar.Term contents : container )
            {
                Node replacement = contents.transform( this, null );
                if ( replacement != contents && terms == null )
                {
                    terms = new ArrayList<>( container.terms() );
                    for ( int i = 0; i < term; i++ )
                    {
                        terms.add( container.nodes.get( i ) );
                    }
                }
                if ( terms != null )
                {
                    if ( replacement != null )
                    {
                        terms.add( replacement );
                    }
                }
                else
                {
                    term++;
                }
            }
            return terms;
        }

        @Override
        public final Node transformLiteral( Void param, Literal literal ) throws RuntimeException
        {
            if ( literal instanceof LiteralNode )
            {
                LiteralNode node = (LiteralNode) literal;
                return literal( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle nodes of of type " + literal.getClass() );
            }
        }

        Node literal( LiteralNode literal )
        {
            return literal;
        }

        @Override
        public final Node transformNonTerminal( Void param, NonTerminal nonTerminal ) throws RuntimeException
        {
            if ( nonTerminal instanceof NonTerminalNode )
            {
                NonTerminalNode node = (NonTerminalNode) nonTerminal;
                return nonTerminal( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle nodes of of type " + nonTerminal.getClass() );
            }
        }

        Node nonTerminal( NonTerminalNode nonTerminal )
        {
            return nonTerminal;
        }

        @Override
        public final Node transformOptional( Void param, Optional optional ) throws RuntimeException
        {
            if ( optional instanceof OptionalNode )
            {
                OptionalNode node = (OptionalNode) optional;
                return optional( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle nodes of of type " + optional.getClass() );
            }
        }

        Node optional( OptionalNode optional )
        {
            return optional.replaceTerm( optional.term().transform( this, null ) );
        }

        @Override
        public final Node transformRepetition( Void param, Repetition repetition ) throws RuntimeException
        {
            if ( repetition instanceof RepetitionNode )
            {
                RepetitionNode node = (RepetitionNode) repetition;
                return repetition( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle nodes of of type " + repetition.getClass() );
            }
        }

        Node repetition( RepetitionNode repetition )
        {
            return repetition.replaceTerm( repetition.term().transform( this, null ) );
        }

        @Override
        public final Node transformEpsilon( Void param ) throws RuntimeException
        {
            return epsilon();
        }

        Node epsilon()
        {
            return Node.epsilon();
        }

        @Override
        public final Node transformCharacters( Void param, CharacterSet characters ) throws RuntimeException
        {
            if ( characters instanceof CharacterSetNode )
            {
                CharacterSetNode node = (CharacterSetNode) characters;
                return characterSet( node );
            }
            else
            {
                throw new IllegalStateException( "Cannot handle nodes of of type " + characters.getClass() );
            }
        }

        Node characterSet( CharacterSetNode characterSet )
        {
            return characterSet;
        }
    }

    static abstract class TransitivePatch extends Patch
    {
        final Set<String> applyTransitive( String start, Mutator mutator )
        {
            Queue<String> queue = new LinkedList<>();
            queue.add( start );
            Transformation transformation = new Transformation();
            for ( String production; null != (production = queue.poll()); )
            {
                if ( !transformation.done( production ) )
                {
                    transformation.transform( mutator, production, queue );
                }
            }
            return transformation.done;
        }

        abstract void applyTo( ProductionNode production );

        private final class Transformation
                implements ProductionTransformation<Queue<String>,ProductionNode,RuntimeException>, TermTransformation<Queue<String>,Void,RuntimeException>
        {
            final Set<String> done = new HashSet<>();

            boolean done( String production )
            {
                return done.contains( production );
            }

            void transform( Mutator mutator, String production, Queue<String> queue )
            {
                mutator.replaceProduction( production, this, queue );
            }

            @Override
            public ProductionNode transformProduction( Queue<String> queue, Production production ) throws RuntimeException
            {
                ProductionNode node = (ProductionNode) production;
                done.add( node.name );
                applyTo( node );
                production.definition().transform( this, queue );
                return node;
            }

            @Override
            public Void transformNonTerminal( Queue<String> queue, NonTerminal nonTerminal ) throws RuntimeException
            {
                String name = ((NonTerminalNode) nonTerminal).ref;
                if ( !done( name ) )
                {
                    queue.add( name );
                }
                return null;
            }

            @Override
            public Void transformAlternatives( Queue<String> queue, Alternatives alternatives ) throws RuntimeException
            {
                for ( Grammar.Term term : alternatives )
                {
                    term.transform( this, queue );
                }
                return null;
            }

            @Override
            public Void transformSequence( Queue<String> queue, Sequence sequence ) throws RuntimeException
            {
                for ( Grammar.Term term : sequence )
                {
                    term.transform( this, queue );
                }
                return null;
            }

            @Override
            public Void transformOptional( Queue<String> queue, Optional optional ) throws RuntimeException
            {
                optional.term().transform( this, queue );
                return null;
            }

            @Override
            public Void transformRepetition( Queue<String> queue, Repetition repetition ) throws RuntimeException
            {
                repetition.term().transform( this, queue );
                return null;
            }

            @Override
            public Void transformLiteral( Queue<String> param, Literal literal ) throws RuntimeException
            {
                return null;
            }

            @Override
            public Void transformCharacters( Queue<String> queue, CharacterSet characters ) throws RuntimeException
            {
                return null;
            }

            @Override
            public Void transformEpsilon( Queue<String> queue ) throws RuntimeException
            {
                return null;
            }
        }
    }
}
