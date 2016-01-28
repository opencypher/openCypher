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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LiteralTest
{
    @Test
    public void shouldReplaceControlCharacterLiteralWithCharacterReference() throws Exception
    {
        // given
        StringBuilder value = new StringBuilder( 1 );
        for ( int cp = 0; cp < 0x20; cp++ )
        {
            value.setLength( 0 );
            value.appendCodePoint( cp );
            LiteralNode literal = new LiteralNode();
            literal.value = value.toString();

            // when
            Node replaced = literal.replaceWithVerified();

            // then
            assertThat( replaced, instanceOf( CharacterSetNode.class ) );
            int codepoint = CharacterSetNode.codePoint( ((CharacterSetNode) replaced).set );
            assertEquals( cp, codepoint );
        }
    }
}
