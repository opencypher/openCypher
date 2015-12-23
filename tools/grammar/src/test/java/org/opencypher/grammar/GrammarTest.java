package org.opencypher.grammar;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opencypher.tools.xml.XmlParser.Option.FAIL_ON_UNKNOWN_ATTRIBUTE;

public class GrammarTest
{
    public final @Rule Fixture fixture = new Fixture();

    @Test
    public void shouldParseGrammar() throws Exception
    {
        // when
        Grammar grammar = Grammar.parseXML( fixture.resourceStream( "/somegrammar.xml" ), FAIL_ON_UNKNOWN_ATTRIBUTE );

        // then
        assertNotNull( grammar );
        assertEquals( "SomeLanguage", grammar.language() );
    }
}
