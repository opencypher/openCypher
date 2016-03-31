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
package org.opencypher.tools.io;

import java.io.Writer;

import org.junit.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.opencypher.tools.io.Output.multiplex;
import static org.opencypher.tools.io.Output.nowhere;
import static org.opencypher.tools.io.Output.output;
import static org.opencypher.tools.io.Output.stdErr;
import static org.opencypher.tools.io.Output.stdOut;
import static org.opencypher.tools.io.Output.stringBuilder;

public class OutputTest
{
    @Test
    public void multiplexOfSingleOutputYieldsSameOutput() throws Exception
    {
        // given
        for ( Output output : new Output[]{nowhere(), stdOut(), stdErr(), stringBuilder()} )
        {
            // when
            Output multiplexed = multiplex( output );

            // then
            assertSame( output, multiplexed );
        }
    }

    @Test
    public void multiplexWithNowhereYieldsSameOutput() throws Exception
    {
        // given
        for ( Output output : new Output[]{nowhere(), stdOut(), stdErr(), stringBuilder()} )
        {
            {   // when
                Output multiplexed = multiplex( output, nowhere() );

                // then
                assertSame( "nowhere() last", output, multiplexed );
            }
            {   // when
                Output multiplexed = multiplex( nowhere(), output );

                // then
                assertSame( "nowhere() first", output, multiplexed );
            }
        }
    }

    @Test
    public void multiplexShouldDeduplicateInput() throws Exception
    {
        // when
        Output multiplexed = multiplex( stdOut(), stdOut() );

        // then
        assertEquals( stdOut(), multiplexed );
    }

    @Test
    public void multiplexShouldFlattenMultiplexedOutput() throws Exception
    {
        // given
        Output a = stdOut(), b = stringBuilder(), c = stringBuilder(), d = stdErr();
        Output one = multiplex( a, b );
        Output two = multiplex( c, d );

        // when
        Output multiplexed = multiplex( a, one, two, d );

        // then
        assertThat( multiplexed, instanceOf( MultiplexedOutput.class ) );
        Output[] output = ((MultiplexedOutput) multiplexed).output;
        assertEquals( 4, output.length );
        assertThat( output, allOf(
                arrayWithSize( 4 ),
                arrayContainingInAnyOrder( a, b, c, d ),
                not( arrayContaining( instanceOf( MultiplexedOutput.class ) ) ) ) );
    }

    @Test
    public void shouldUnwrapWrappingWriter() throws Exception
    {
        // given
        Output output = stringBuilder();
        Writer writer = output.writer();

        // when
        Output result = output( writer );

        // then
        assertSame( output, result );
        assertThat( writer, instanceOf( OutputWriter.class ) );
    }
}
