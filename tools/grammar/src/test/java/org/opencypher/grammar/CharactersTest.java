package org.opencypher.grammar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CharactersTest
{
    @Test
    public void shouldTranslateControlCharacters() throws Exception
    {
        // given
        for ( int cp = 0; cp <= 0x20; cp++ )
        {
            // when
            Characters charset = Characters.codePoint( cp );

            // then
            assertNotNull( charset );
            String name = charset.set;
            assertNotNull( name );

            // when
            int codePoint = Characters.codePoint( name );

            // then
            assertEquals( cp, codePoint );
        }
    }
}
