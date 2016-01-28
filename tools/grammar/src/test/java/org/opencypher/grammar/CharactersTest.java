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
            CharacterSetNode charset = CharacterSetNode.codePoint( cp );

            // then
            assertNotNull( charset );
            String name = charset.set;
            assertNotNull( name );

            // when
            int codePoint = CharacterSetNode.codePoint( name );

            // then
            assertEquals( cp, codePoint );
        }
    }
}
