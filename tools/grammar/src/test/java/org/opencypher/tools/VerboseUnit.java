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
package org.opencypher.tools;

import java.util.function.Consumer;

import org.opencypher.tools.output.Output;

import static org.opencypher.tools.output.Output.nowhere;
import static org.opencypher.tools.output.Output.stdOut;

public class VerboseUnit extends Interactive<Consumer<Output>, Void>
{
    public VerboseUnit( int repetitions )
    {
        super( new Test<Consumer<Output>, Void>()
        {
            @Override
            public Void suite( String className, String methodName, Consumer<Output> test )
            {
                test.accept( nowhere() );
                return null;
            }

            @Override
            public Void singleClass( String className, String methodName, Consumer<Output> test )
            {
                execute( methodName, test, 0 );
                return null;
            }

            @Override
            public Void singleMethod( String className, String methodName, Consumer<Output> test )
            {
                execute( methodName, test, repetitions );
                return null;
            }
        } );
    }

    private static void execute( String methodName, Consumer<Output> test, int times )
    {
        Output out = stdOut();
        out.append( methodName ).append( ": '" );
        test.accept( out );
        out.println( "'" );
        if ( times > 0 )
        {
            long time = System.nanoTime();
            for ( int i = 0; i < times; i++ )
            {
                test.accept( nowhere() );
            }
            time = System.nanoTime() - time;
            out.printf( "%s: %.3fus/invocation%n", methodName, time / 1000.0 / times );
        }
    }
}
