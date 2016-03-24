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
package org.opencypher.generator;

import java.io.Serializable;
import java.util.function.Consumer;

import org.opencypher.tools.Reflection;
import org.opencypher.tools.output.Output;

public interface ProductionReplacement<T> extends Serializable
{
    interface Context<T>
    {
        Node node();

        void generateDefault();

        T context();

        void write( CharSequence str );

        void write( int codePoint );

        Output output();
    }

    void replace( Context<T> context );

    static <T> ProductionReplacement<T> replace( String production, Consumer<Context<T>> replacement )
    {
        return new ProductionReplacement<T>()
        {
            @Override
            public String production()
            {
                return production;
            }

            @Override
            public void replace( Context<T> term )
            {
                replacement.accept( term );
            }
        };
    }

    default String production()
    {
        return Reflection.lambdaParameterName( this );
    }
}
