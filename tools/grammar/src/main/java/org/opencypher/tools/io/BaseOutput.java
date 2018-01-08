/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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

import static java.util.Objects.deepEquals;
import static java.util.Objects.hash;

abstract class BaseOutput<Target> implements Output
{
    final Target output;

    BaseOutput( Target output )
    {
        assert output != null : "null output";
        this.output = output;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "( " + output + " )";
    }

    @Override
    public final boolean equals( Object that )
    {
        return this == that || (
                that != null &&
                this.getClass() == that.getClass() &&
                deepEquals( output, ((BaseOutput<?>) that).output ));
    }

    @Override
    public final int hashCode()
    {
        return hash( output );
    }
}
