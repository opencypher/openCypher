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
package org.opencypher.tools.grammar;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.transform.TransformerException;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.GrammarVisitor;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.tools.output.Output;
import org.opencypher.tools.xml.XmlGenerator;
import org.xml.sax.SAXException;

public class Xml extends XmlGenerator implements GrammarVisitor<SAXException>
{
    public static void write( Grammar grammar, Writer writer ) throws TransformerException
    {
        generate( new Xml( grammar ), writer );
    }

    public static void write( Grammar grammar, OutputStream stream ) throws TransformerException
    {
        generate( new Xml( grammar ), stream );
    }

    public static void write( Grammar grammar, Output output ) throws TransformerException
    {
        write( grammar, output.writer() );
    }

    public static void main( String... args ) throws Exception
    {
        Main.execute( Xml::write, args );
    }

    private final Grammar grammar;

    private Xml( Grammar grammar )
    {
        this.grammar = grammar;
    }

    @Override
    protected void generate() throws SAXException
    {
        startDocument();
        startPrefixMapping( "", Grammar.XML_NAMESPACE );
        startElement( "grammar", attribute( "language", grammar.language() ) );
        grammar.accept( this );
        endElement( "grammar" );
        endPrefixMapping( "" );
        endDocument();
    }

    @Override
    public void visitProduction( Production production ) throws SAXException
    {
        startElement( "production", attribute( "name", production.name() ) );
        String description = production.description();
        if ( description != null )
        {
            startElement( "description" );
            characters( description );
            endElement( "description" );
        }
        production.definition().accept( this );
        endElement( "production" );
    }

    @Override
    public void visitAlternatives( Alternatives alternatives ) throws SAXException
    {
        startElement( "alt" );
        for ( Grammar.Term term : alternatives )
        {
            term.accept( this );
        }
        endElement( "alt" );
    }

    @Override
    public void visitSequence( Sequence sequence ) throws SAXException
    {
        startElement( "seq" );
        for ( Grammar.Term term : sequence )
        {
            term.accept( this );
        }
        endElement( "seq" );
    }

    @Override
    public void visitLiteral( Literal value ) throws SAXException
    {
            startElement( "literal", attribute( "value", value.toString() ) );
            endElement( "literal" );
    }

    @Override
    public void visitCharacters( CharacterSet characters ) throws SAXException
    {
        class Writer implements CharacterSet.DefinitionVisitor.NamedSetVisitor<SAXException>, AutoCloseable
        {
            Output.Readable set;

            @Override
            public CharacterSet.ExclusionVisitor<SAXException> visitSet( String name ) throws SAXException
            {
                if ( set != null )
                {
                    throw new IllegalStateException();
                }
                set = Output.nowhere(); // mark as done
                startElement( "character", attribute( "set", name ) );
                return new CharacterSet.ExclusionVisitor<SAXException>()
                {
                    @Override
                    public void excludeCodePoint( int cp ) throws SAXException
                    {
                        startElement( "except", attribute( "codePoint", Integer.toString( cp ) ) );
                        endElement( "except" );
                    }

                    @Override
                    public void excludeRange( int start, int end ) throws SAXException
                    {
                        startElement( "except", attribute( "set", String.format( "&#x%04X;-&#x%04X;", start, end  ) ) );
                        endElement( "except" );
                    }

                    @Override
                    public void close() throws SAXException
                    {
                        endElement( "character" );
                    }
                };
            }

            @Override
            public void visitCodePoint( int cp ) throws SAXException
            {
                format( "&#x%04X;", cp );
            }

            @Override
            public void visitRange( int start, int end ) throws SAXException
            {
                format( "&#x%04X;-&#x%04X;", start, end );
            }

            @Override
            public void close() throws SAXException
            {
                if ( set == null )
                {
                    throw new IllegalStateException();
                }
                if ( set.length() > 0 )
                {
                    set.append( ']' );
                    startElement( "character", attribute( "set", set.toString() ) );
                    endElement( "character" );
                    set = Output.nowhere(); // mark as done
                }
            }

            void format( String format, Object... arguments )
            {
                if ( set == null )
                {
                    (set = Output.stringBuilder()).append( '[' );
                }
                if ( set.length() == 0 )
                {
                    throw new IllegalStateException();
                }
                set.format( format, arguments );
            }
        }
        try ( Writer writer = new Writer() )
        {
            characters.accept( writer );
        }
    }

    @Override
    public void visitNonTerminal( NonTerminal nonTerminal ) throws SAXException
    {
        startElement( "non-terminal", attribute( "ref", nonTerminal.productionName() ) );
        endElement( "non-terminal" );
    }

    @Override
    public void visitOptional( Optional optional ) throws SAXException
    {
        startElement( "opt" );
        optional.term().accept( this );
        endElement( "opt" );
    }

    @Override
    public void visitRepetition( Repetition repetition ) throws SAXException
    {
        if ( repetition.minTimes() > 0 )
        {
            AttributesBuilder attributes = attribute( "min", "" + repetition.minTimes() );
            if ( repetition.limited() )
            {
                attributes = attributes.attribute( "max", "" + repetition.maxTimes() );
            }
            startElement( "repeat", attributes );
        }
        else if ( repetition.limited() )
        {
            startElement( "repeat", attribute( "max", "" + repetition.maxTimes() ) );
        }
        else
        {
            startElement( "repeat" );
        }
        repetition.term().accept( this );
        endElement( "repeat" );
    }
}
