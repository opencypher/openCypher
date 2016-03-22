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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opencypher.tools.output.Output;

import static org.opencypher.tools.output.Output.nowhere;
import static org.opencypher.tools.output.Output.stdOut;

public class VerboseUnit implements TestRule
{
    private final int repetitions;
    private String className, methodName;

    public VerboseUnit()
    {
        this( 0 );
    }

    public VerboseUnit( int repetitions )
    {
        this.repetitions = repetitions;
    }

    @Override
    public Statement apply( Statement base, Description description )
    {
        className = description.getClassName();
        methodName = description.getMethodName();
        return base;
    }

    public void test( Consumer<Output> test )
    {
        wrapped( test ).accept( nowhere() );
    }

    private Consumer<Output> wrapped( Consumer<Output> test )
    {
        if ( /*running from Intellij*/ System.getProperty( "idea.launcher.port" ) != null )
        {
            String command = System.getProperty( "sun.java.command" );
            if (/*we can inspect the command line*/command != null )
            {
                boolean verbose;
                int times;
                if ( command.endsWith( " " + className + "," + methodName ) )
                {
                    verbose = true;
                    times = repetitions;
                }
                else if ( command.endsWith( " " + className ) )
                {
                    verbose = true;
                    times = 0;
                }
                else
                {
                    verbose = false;
                    times = 0;
                }
                if ( verbose )
                {
                    return out -> {
                        Output stdOut = stdOut();
                        stdOut.append( methodName ).append( ": '" );
                        test.accept( stdOut.and( out ) );
                        stdOut.println( "'" );
                        if ( times > 0 )
                        {
                            long time = System.nanoTime();
                            for ( int i = 0; i < times; i++ )
                            {
                                test.accept( out );
                            }
                            time = System.nanoTime() - time;
                            stdOut.printf( "%s: %.3fus/invocation%n", methodName, time / 1000.0 / times );
                        }
                    };
                }
            }
        }
        return test;
    }
}
