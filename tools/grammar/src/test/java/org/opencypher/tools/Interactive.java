/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.tools;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Interactive<T, R> implements TestRule
{
    public interface Test<T, R>
    {
        R suite( String className, String methodName, T test ) throws Exception;

        default R singleClass( String className, String methodName, T test ) throws Exception
        {
            return suite( className, methodName, test );
        }

        default R singleMethod( String className, String methodName, T test ) throws Exception
        {
            return singleClass( className, methodName, test );
        }
    }

    public enum Mode
    {
        SUITE, CLASS, METHOD;
    }

    private String className, methodName;
    private final Test<T, R> test;

    public Interactive( Test<T, R> test )
    {
        this.test = test;
    }

    @Override
    public final Statement apply( Statement base, Description description )
    {
        className = description.getClassName();
        methodName = description.getMethodName();
        return base;
    }

    public final R test( T test ) throws Exception
    {
        switch ( mode() )
        {
        case METHOD:
            return this.test.singleMethod( className, methodName, test );
        case CLASS:
            return this.test.singleClass( className, methodName, test );
        default:
            return this.test.suite( className, methodName, test );
        }
    }

    public Mode mode()
    {
        String command = System.getProperty( "sun.java.command" );
        if (/*we can inspect the command line*/command != null )
        {
            if (/*running from Intellij*/command.matches( "com\\.intellij\\.rt\\.(execution\\.)?junit\\.JUnitStarter .*" ) )
            {
                if ( command.endsWith( " " + className + "," + methodName ) )
                {
                    return Mode.METHOD;
                }
                else if ( command.endsWith( " " + className ) )
                {
                    return Mode.CLASS;
                }
            }
        }
        return Mode.SUITE;
    }
}
