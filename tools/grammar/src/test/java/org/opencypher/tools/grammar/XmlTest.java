package org.opencypher.tools.grammar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;

import static org.junit.Assert.assertEquals;
import static org.opencypher.grammar.GrammarVisitor.production;

public class XmlTest
{
    public final @Rule Fixture fixture = new Fixture();

    @Test
    public void shouldProduceSameGrammarWhenParsingOutput() throws Exception
    {
        // given
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Grammar first = fixture.grammarResource( "/somegrammar.xml" );

        // when
        Xml.write( first, out );

        // then
        Grammar second = Grammar.parseXML( new ByteArrayInputStream( out.toByteArray() ) );
        try
        {
            assertEquals( first, second );
        }
        catch ( Throwable e )
        {
            Map<String, Grammar.Term> before = new HashMap<>();
            first.accept( production( before::put ) );
            second.accept( production( ( name, def ) -> {
                try
                {
                    assertEquals( before.get( name ), def );
                }
                catch ( Throwable x )
                {
                    e.addSuppressed( x );
                }
            } ) );
            throw e;
        }
    }
}
