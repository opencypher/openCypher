/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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

import java.io.IOException;

import cucumber.api.junit.Cucumber;
import org.junit.runners.model.InitializationError;
import org.opencypher.tools.grammar.Antlr4TestUtils;

public class InitFunction extends Cucumber
{
    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws IOException if there is a problem
     * @throws org.junit.runners.model.InitializationError if there is another problem
     */
    public InitFunction( Class clazz ) throws InitializationError, IOException
    {
        super( init( clazz ) );
    }

    private static Class init( Class clazz )
    {
        org.opencypher.tools.tck.validateQueryGrammar.f_$eq( Antlr4TestUtils::parse );
        return clazz;
    }
}
