package org.opencypher.tools.grammar;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.TransformerException;

import org.opencypher.grammar.Exclusion;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.GrammarVisitor;
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
    public void visitProduction( String production, Grammar.Term definition ) throws SAXException
    {
        startElement( "production", attribute( "name", production )
                .attribute( "case-sensitive", Boolean.toString( grammar.caseSensitiveByDefault() ) ) );
        String description = grammar.productionDescription( production );
        if ( description != null )
        {
            startElement( "description" );
            println( description );
            endElement( description );
        }
        definition.accept( this );
        endElement( "production" );
    }

    @Override
    public void visitAlternatives( Collection<Grammar.Term> alternatives ) throws SAXException
    {
        startElement( "alt" );
        for ( Grammar.Term term : alternatives )
        {
            term.accept( this );
        }
        endElement( "alt" );
    }

    @Override
    public void visitSequence( Collection<Grammar.Term> sequence ) throws SAXException
    {
        startElement( "seq" );
        for ( Grammar.Term term : sequence )
        {
            term.accept( this );
        }
        endElement( "seq" );
    }

    @Override
    public void visitLiteral( String value ) throws SAXException
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
            startElement( "literal", attribute( "value", value ) );
            endElement( "literal" );
        }
        else
        {
            println( value );
        }
    }

    @Override
    public void visitCharacters( String wellKnownSetName, List<Exclusion> exclusions ) throws SAXException
    {
        startElement( "character", attribute( "set", wellKnownSetName ) );
        for ( Exclusion exclusion : exclusions )
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
    public void visitNonTerminal( String productionName, Grammar.Term productionDef ) throws SAXException
    {
        startElement( "non-terminal", attribute( "ref", productionName ) );
        endElement( "non-terminal" );
    }

    @Override
    public void visitOptional( Grammar.Term term ) throws SAXException
    {
        startElement( "opt" );
        term.accept( this );
        endElement( "opt" );
    }

    @Override
    public void visitRepetition( int min, Integer max, Grammar.Term term ) throws SAXException
    {
        if ( min > 0 )
        {
            AttributesBuilder attributes = attribute( "min", "" + min );
            if ( max != null )
            {
                attributes = attributes.attribute( "max", max.toString() );
            }
            startElement( "repeat", attributes );
        }
        else if ( max != null )
        {
            startElement( "repeat", attribute( "max", max.toString() ) );
        }
        else
        {
            startElement( "repeat" );
        }
        term.accept( this );
        endElement( "repeat" );
    }
}
