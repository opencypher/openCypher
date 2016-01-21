package org.opencypher.tools.grammar;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.transform.TransformerException;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Exclusion;
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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Xml extends XmlGenerator
        implements GrammarVisitor<SAXException>, Exclusion.Visitor<Attributes, RuntimeException>
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
        startElement( "production", attribute( "name", production.name() )
                .attribute( "case-sensitive", Boolean.toString( grammar.caseSensitiveByDefault() ) ) );
        String description = production.description();
        if ( description != null )
        {
            startElement( "description" );
            println( description );
            endElement( description );
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
        boolean whitespace = false;
        for ( int i = 0, len = value.length(), cp; i < len; i += Character.charCount( cp ) )
        {
            if ( Character.isWhitespace( cp = value.codePointAt( i ) ) || cp == '\\' )
            {
                whitespace = true;
                break;
            }
        }
        if ( whitespace )
        {
            startElement( "literal", attribute( "value", value.toString() ) );
            endElement( "literal" );
        }
        else
        {
            println( value );
        }
    }

    @Override
    public void visitCharacters( CharacterSet characters ) throws SAXException
    {
        startElement( "character", attribute( "set", characters.setName() ) );
        for ( Exclusion exclusion : characters.exclusions() )
        {
            startElement( "except", exclusion.accept( this ) );
            endElement( "except" );
        }
        endElement( "character" );
    }

    @Override
    public Attributes excludeLiteral( String literal ) throws RuntimeException
    {
        return attribute( "literal", literal );
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
