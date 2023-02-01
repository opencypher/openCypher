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
package org.opencypher.tools.xml;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.explicitCastArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodType.methodType;

final class AttributeHandler
{
    final String uri, name;
    final boolean optional;
    private final MethodHandle setter;

    AttributeHandler( String uri, String name, boolean optional, MethodHandle setter )
    {
        this.uri = uri;
        this.name = name;
        this.optional = optional;
        this.setter = setter;
    }

    @Override
    public String toString()
    {
        return String.format( "%sAttribute{uri='%s', name='%s'}", optional ? "Optional" : "", uri, name );
    }

    public boolean matches( String uri, String name )
    {
        return this.uri.equalsIgnoreCase( uri ) && this.name.equalsIgnoreCase( name );
    }

    public void apply( Object target, Resolver resolver, String value )
    {
        try
        {
            setter.invokeWithArguments( target, resolver, value );
        }
        catch ( RuntimeException | Error e )
        {
            throw e;
        }
        catch ( Throwable throwable )
        {
            throw new RuntimeException( throwable );
        }
    }

    private static final Map<Class<?>, Function<MethodHandle, MethodHandle>> CONVERSION = new ConcurrentHashMap<>();
    private static final MethodHandle ENUM_VALUE_OF = Reference.<Class, String, Enum>biFunction( Enum::valueOf ).mh();
    private static final MethodHandle UPPER_STRING = Reference.<String, String>function( String::toUpperCase ).mh();

    public static MethodHandle conversion( Class<?> type, MethodHandle setter )
    {
        return CONVERSION.computeIfAbsent( type, AttributeHandler::conversion ).apply( setter );
    }

    static
    {
        CONVERSION.put( String.class, ( mh ) -> dropArguments( mh, 1, Resolver.class ) );
        CONVERSION.put( int.class, conversion( Reference.<String>toInt( Integer::parseInt ) ) );
        CONVERSION.put( Integer.class, conversion( Reference.<String, Integer>function( Integer::valueOf ) ) );
        CONVERSION.put( boolean.class, conversion( Reference.<String>toBool( Boolean::parseBoolean ) ) );
        CONVERSION.put( Boolean.class, conversion( Reference.<String, Boolean>function( Boolean::valueOf ) ) );
        CONVERSION.put( long.class, conversion( Reference.<String>toLong( Long::parseLong ) ) );
        CONVERSION.put( Long.class, conversion( Reference.<String, Long>function( Long::valueOf ) ) );
        CONVERSION.put( double.class, conversion( Reference.<String>toDouble( Double::parseDouble ) ) );
        CONVERSION.put( Double.class, conversion( Reference.<String, Double>function( Double::valueOf ) ) );
        Resolver.initialize( CONVERSION::put );
    }

    private static Function<MethodHandle, MethodHandle> conversion( Reference reference )
    {
        MethodHandle filter = reference.mh();
        return conversion( filter );
    }

    private static Function<MethodHandle, MethodHandle> conversion( Class<?> type )
    {
        if ( type.isEnum() )
        {
            return enumConversion( type );
        }
        throw new IllegalArgumentException( "Unsupported field type: " + type );
    }

    private static Function<MethodHandle, MethodHandle> enumConversion( Class<?> enumType )
    {
        return conversion( explicitCastArguments(
                filterArguments( ENUM_VALUE_OF.bindTo( enumType ), 0, UPPER_STRING ),
                methodType( enumType, String.class ) ) );
    }

    private static Function<MethodHandle, MethodHandle> conversion( MethodHandle filter )
    {
        return mh -> dropArguments( filterArguments( mh, 1, filter ), 1, Resolver.class );
    }
}
