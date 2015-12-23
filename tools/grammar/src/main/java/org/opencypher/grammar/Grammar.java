package org.opencypher.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.tools.xml.XmlParser;
import org.xml.sax.SAXException;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

public interface Grammar
{
    String XML_NAMESPACE = "http://thobe.org/grammar";

    static Grammar parseXML( InputStream input, XmlParser.Option... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        return Root.XML.parse( input, options ).resolve( identity() );
    }

    String language();

    boolean caseSensitiveByDefault();

    String productionDescription( String production );

    <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX;

    static Builder grammar( String language, Option... options )
    {
        return new Builder( language );
    }

    static Term literal( String value )
    {
        Literal literal = new Literal();
        literal.value = requireNonNull( value, "literal value" );
        return literal;
    }

    static Term nonTerminal( String production )
    {
        NonTerminal nonTerminal = new NonTerminal();
        nonTerminal.ref = production;
        return nonTerminal;
    }

    static Term optional( Term first, Term... more )
    {
        return sequence( first, more ).addTo( new Optional() );
    }

    static Term oneOf( Term first, Term... alternatives )
    {
        if ( alternatives == null || alternatives.length == 0 )
        {
            return first;
        }
        return new Alternatives().addAll( first, alternatives );
    }

    static Term zeroOrMore( Term first, Term... more )
    {
        return sequence( first, more ).addTo( new Repetition() );
    }

    static Term oneOrMore( Term first, Term... more )
    {
        return atLeast( 1, first, more );
    }

    static Term atLeast( int times, Term first, Term... more )
    {
        Repetition repetition = new Repetition();
        repetition.min = times;
        return sequence( first, more ).addTo( repetition );
    }

    static Term repeat( int times, Term first, Term... more )
    {
        Repetition repetition = new Repetition();
        repetition.min = repetition.max = times;
        return sequence( first, more ).addTo( repetition );
    }

    static Term repeat( int min, int max, Term first, Term... more )
    {
        Repetition repetition = new Repetition();
        repetition.min = min;
        repetition.max = max;
        return sequence( first, more ).addTo( repetition );
    }

    static Term sequence( Term first, Term... more )
    {
        if ( more == null || more.length == 0 )
        {
            return first;
        }
        return new Sequence().addAll( first, more );
    }

    class Builder extends Root
    {
        private Builder( String language )
        {
            this.language = requireNonNull( language, "language name" );
        }

        public Builder production( String name, Term first, Term... alternatives )
        {
            Production production = new Production();
            production.name = name;
            Grammar.oneOf( first, alternatives ).addTo( production );
            add( production );
            return this;
        }

        public Grammar build()
        {
            return resolve( HashMap::new );
        }
    }

    abstract class Term
    {
        public abstract <EX extends Exception> void accept( GrammarVisitor<EX> visitor ) throws EX;

        abstract Container addTo( Container container );

        abstract Sequenced addTo( Sequenced sequenced );

        abstract Production addTo( Production production );
    }

    abstract class Option
    {
        abstract void apply( Root grammar );
    }
}
