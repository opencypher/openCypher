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

import org.opencypher.tools.grammar.RailRoadDiagramPages;
import org.opencypher.tools.grammar.SQLBNF;
import org.opencypher.tools.io.Output;
import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.LocationAware;
import org.opencypher.tools.xml.XmlParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.opencypher.tools.xml.XmlParser.xmlParser;

@Element( uri = WG3Grammar.XML_NAMESPACE, name = "grammar" )
class WG3Grammar extends ProtoGrammar implements LocationAware
{
    public static void main( String[] args ) throws Exception
    {
        for ( String arg : args )
        {
            run( Path.of( arg ) );
        }
    }

    private static void run( Path inputFile ) throws Exception
    {
        Grammar grammar = XML.parse( inputFile ).resolve( ProtoGrammar.NO_RESOLVER, ProtoGrammar.ResolutionOption.ALLOW_ROOTLESS );
        SQLBNF.write( grammar, Output.stdOut() );
        Map<String,Object> properties = new HashMap<>();
        properties.put( "RailRoadDiagramPages.outputDir", inputFile.resolveSibling( "railroads" ) );
        // TODO: don't use SQLBNF yet, the output is broken!
        //properties.put( "RailRoadDiagramPages.bnfFlavour", (RailRoadDiagramPages.BnfFlavour)(SQLBNF::html) );
        Files.list( inputFile.toAbsolutePath().getParent() )
                .filter( path -> path.toString().toUpperCase().endsWith( "GQL.PDF" ) )
                .max( Comparator.comparing( ( Path path ) -> path.getFileName().toString() ) )
                .ifPresent( path -> properties.put( "RailRoadDiagramPages.productionDetailsLink", "../" + path.getFileName().toString() + "#''BNF_{0}''" ) );
        RailRoadDiagramPages.generate( grammar, inputFile.getParent(), Output.stdOut(), properties );
    }

    static final String XML_NAMESPACE = "";
    static final XmlParser<WG3Grammar> XML = xmlParser( WG3Grammar.class );
    Grammar.Builder grammar;

    @Override
    public void location( String path, int lineNumber, int columnNumber )
    {
        int end = path.lastIndexOf( '.' );
        String language = path.substring( path.lastIndexOf( '/' ) + 1, end < 0 ? path.length() : end );
        grammar = Grammar.grammar( language );
    }

    @Child
    void add( BnfDef def )
    {
        def.addTo( grammar );
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
        options.add( ResolutionOption.ALLOW_ROOTLESS ); // WG3 grammar files does not have a way to define the root
        return grammar.resolve( resolver, options );
    }

    @Override
    void add( ProductionNode production )
    {
        grammar.add( production );
    }

    @Override
    void apply( Patch patch )
    {
        grammar.apply( patch );
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "BNFDef" )
    static class BnfDef
    {
        @Attribute( name = "name" )
        String name;
        boolean predicative;
        private Rhs def;

        @Attribute( optional = true )
        void predicative( String predicative )
        {
            this.predicative = "yes".equalsIgnoreCase( predicative );
        }

        @Child
        void setDef( Rhs def )
        {
            if ( this.def != null )
            {
                throw new IllegalStateException( "Body of '" + name + "' already defined." );
            }
            this.def = def;
        }

        void addTo( Grammar.Builder grammar )
        {
            if ( def == null )
            {
                throw new IllegalStateException( "Body of '" + name + "' not defined." );
            }
//            if ( def.terms.isEmpty() )
//            {
//                System.err.println( "<" + name + "> IS EMPTY!!!" );
//            }
            if ( def.seeTheRules )
            {
                grammar.production( name, "SEE THE RULES", def.term() );
            }
            else
            {
                grammar.production( name, def.term() );
            }
        }
    }

    static abstract class Body// implements Term
    {
        final List<Grammar.Term> terms = new ArrayList<>();
        private State state = State.INITIAL;

        @Child
        final void literal( char[] buffer, int start, int length )
        {
            LiteralNode.fromCharacters( buffer, start, length, this::text );
        }

        private void text( LiteralNode literal )
        {
            state.seq( terms, literal, getClass() );
        }

        @Child
        void alt( Alt term )
        {
            state = state.alt( terms, term.term(), getClass() );
        }

        @Child( {BNF.class, Group.class, Monospaced.class, Opt.class, Keyword.class, JsonKeyword.class, TerminalSymbol.class} )
        void seq( Term term )
        {
            state = state.seq( terms, term.term(), getClass() );
        }

        @Child
        void ellipsis( Ellipsis ellipsis )
        {
            if ( state == State.ALT )
            {
                throw new IllegalStateException( "Cannot add <ellipsis> to alt group." );
            }
            int last = terms.size() - 1;
            terms.set( last, Grammar.oneOrMore( terms.get( last ) ) );
        }

        public Grammar.Term term()
        {
            if ( terms.size() == 1 )
            {
                return terms.get( 0 );
            }
            return state.term( terms, getClass() );
        }

        enum State
        {   // <pre>
            INITIAL
            {
                @Override
                Grammar.Term term( List<Grammar.Term> terms, Class<? extends Body> type )
                {
                    if (true) // TODO: temporary measure? Ultimately, I believe there should not be empty production rules.
                    {
                        return Grammar.epsilon();
                    }
                    throw new IllegalStateException( "Cannot create term from empty group in <" + type.getSimpleName().toLowerCase() + ">." );
                }
            },
            ALT
            {
                @Override
                State seq( List<Grammar.Term> terms, Grammar.Term term, Class<? extends Body> type )
                {
                    throw new IllegalStateException( "Cannot add <" + term.getClass().getSimpleName().toLowerCase() + "> to alt group in <" + type.getSimpleName().toLowerCase() + ">." );
                }

                @Override
                Grammar.Term term( List<Grammar.Term> terms, Class<? extends Body> type )
                {
                    return new AlternativesNode().addAll( terms );
                }
            },
            SEQ
            {
                @Override
                State alt( List<Grammar.Term> terms, Grammar.Term term, Class<? extends Body> type )
                {
                    throw new IllegalStateException( "Cannot add <alt> to sequential group in <" + type.getSimpleName().toLowerCase() + ">.");
                }

                @Override
                Grammar.Term term( List<Grammar.Term> terms, Class<? extends Body> aClass)
                {
                    return new SequenceNode().addAll( terms );
                }
            },
            ; // </pre>

            State alt( List<Grammar.Term> terms, Grammar.Term term, Class<? extends Body> aClass )
            {
                terms.add( term );
                return ALT;
            }

            State seq( List<Grammar.Term> terms, Grammar.Term term, Class<? extends Body> aClass )
            {
                terms.add( term );
                return SEQ;
            }

            abstract Grammar.Term term( List<Grammar.Term> terms, Class<? extends Body> aClass );
        }
    }

    interface Term
    {
        Grammar.Term term();
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "rhs" )
    static class Rhs extends Body
    {
        boolean seeTheRules;

        @Child
        void seeTheRules( SeeTheRules seeTheRules )
        {
            this.seeTheRules = true;
        }
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "BNF" )
    static class BNF implements Term
    {
        @Attribute
        String name;
        @Attribute( optional = true )
        String part;
        @Attribute( optional = true )
        String standard;

        @Override
        public Grammar.Term term()
        {
            NonTerminalNode nonTerminal = new NonTerminalNode();
            nonTerminal.ref = name;
            if (standard != null || part != null )
            {
                nonTerminal.externalReference( new WG3Reference( standard, part, name ) );
            }
            return nonTerminal;
        }
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "alt" )
    static class Alt extends Body implements Term
    {
        AllAltsReference allAltsFrom;

        @Child
        void allAltsFrom( AllAltsReference reference )
        {
            this.allAltsFrom = reference;
        }
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "group" )
    static class Group extends Body implements Term
    {
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "mono" )
    static class Monospaced extends Body implements Term
    {
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "opt" )
    static class Opt extends Body implements Term
    {
        @Override
        public Grammar.Term term()
        {
            OptionalNode opt = new OptionalNode();
            opt.add( (Node)super.term() );
            return opt;
        }
    }

    static abstract class Literal implements Term
    {
        private Node literal;

        @Child
        final void literal( char[] buffer, int start, int length )
        {
            LiteralNode.fromCharacters( buffer, start, length, this::add );
        }

        private void add( LiteralNode literal )
        {
            annotate( literal );
            if ( this.literal != null )
            {
                SequenceNode seq;
                if ( this.literal instanceof SequenceNode )
                {
                    seq = (SequenceNode) this.literal;
                }
                else
                {
                    seq = new SequenceNode();
                    seq.add( this.literal );
                    this.literal = seq;
                }
                seq.add( literal );
            }
            else
            {
                this.literal = literal;
            }
        }

        void annotate( LiteralNode literal )
        {
        }

        @Override
        public Grammar.Term term()
        {
            if ( literal == null )
            {
                throw new IllegalStateException( "No keyword!" );
            }
            return literal;
        }
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "kw" )
    static class Keyword extends Literal
    {
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "sjkw" )
    static class JsonKeyword extends Literal
    {
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "terminalsymbol" )
    static class TerminalSymbol extends Literal
    {
        @Override
        void annotate( LiteralNode literal )
        {
            literal.caseSensitive = true;
        }
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "seeTheRules" )
    static class SeeTheRules
    {
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "allAltsFrom" )
    static class AllAltsReference
    {
        @Attribute
        String part;
        @Attribute( optional = true )
        String standard;
    }

    @Element( uri = WG3Grammar.XML_NAMESPACE, name = "ellipsis" )
    static class Ellipsis
    {
    }
}
