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

import java.util.Objects;

/**
 * Utility class used in {@link Reflection} to specify the type of a parameter along with the corresponding argument
 * value. This allows passing an instance of a more derived type, or passing null and still having the type of the
 * parameter.
 */
public final class TypedArgument
{
    /**
     * Factory method.
     *
     * @param type  the type of the parameter.
     * @param value the value of the argument.
     * @return a new instance.
     */
    public static TypedArgument typed( Class<?> type, Object value )
    {
        type = Objects.requireNonNull( type, "type" );
        if ( value != null && !type.isInstance( value ) )
        {
            throw new IllegalArgumentException(
                    value + " (a " + value.getClass().getName() + ") is not an instance of " + type.getName() );
        }
        return new TypedArgument( type, value );
    }

    private final Class<?> type;
    private final Object value;

    private TypedArgument( Class<?> type, Object value )
    {
        this.type = type;
        this.value = value;
    }

    /**
     * Extract the parameter types from an array of {@link TypedArgument}s.
     *
     * @param arguments the arguments to extract the types from.
     * @return the types of the arguments.
     */
    public static Class<?>[] types( TypedArgument... arguments )
    {
        Class[] types = new Class[arguments.length];
        for ( int i = 0; i < types.length; i++ )
        {
            types[i] = arguments[i].type;
        }
        return types;
    }

    /**
     * Extract the argument values from an array of {@link TypedArgument}s.
     *
     * @param arguments the arguments to extract the values from.
     * @return the values of the arguments.
     */
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
