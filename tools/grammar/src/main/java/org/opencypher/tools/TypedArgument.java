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

import java.util.Objects;

public final class TypedArgument
{
    public static TypedArgument typed( Class<?> type, Object value )
    {
        return new TypedArgument( type, value );
    }

    private final Class<?> type;
    private final Object value;

    private TypedArgument( Class<?> type, Object value )
    {
        this.type = Objects.requireNonNull( type, "type" );
        this.value = value;
    }

    public static Class<?>[] types( TypedArgument... arguments )
    {
        Class[] types = new Class[arguments.length];
        for ( int i = 0; i < types.length; i++ )
        {
            types[i] = arguments[i].type;
        }
        return types;
    }

    public static Object[] values( TypedArgument... arguments )
    {
        Object[] values = new Object[arguments.length];
        for ( int i = 0; i < values.length; i++ )
        {
            values[i] = arguments[i].value;
        }
        return values;
    }
}
