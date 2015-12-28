package org.opencypher.grammar;

import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GrammarTest
{
    public final @Rule Fixture fixture = new Fixture();

    @Test
    public void shouldParseGrammar() throws Exception
    {
        // when
        Grammar grammar = Grammar.parseXML( Paths.get( fixture.resource( "/somegrammar.xml" ).toURI() ),
                                            Grammar.ParserOption.FAIL_ON_UNKNOWN_XML_ATTRIBUTE );

        // then
        assertNotNull( grammar );
        assertEquals( "SomeLanguage", grammar.language() );
    }

    @Test
    public void shouldParseCypherGrammar() throws Exception
    {
        // given
        Grammar.parseXML( Paths.get("/Users/tobias/code/neo/cypher/grammar/cypher.xml") );
        // when

        // then
    }
}
