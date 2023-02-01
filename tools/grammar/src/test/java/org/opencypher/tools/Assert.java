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
import java.util.function.Supplier;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.ComparisonFailure;

import static org.junit.Assert.fail;

public class Assert
{
    public static <T, A> Matcher<T> with( NamedFunction<T, A> attribute, Matcher<A> matcher )
    {
        return new TypeSafeMatcher<T>()
        {
            @Override
            protected boolean matchesSafely( T item )
            {
                return matcher.matches( attribute.apply( item ) );
            }

            @Override
            public void describeTo( Description description )
            {
                description.appendText( attribute.name() ).appendText( " that is " ).appendDescriptionOf( matcher );
            }
        };
    }

    public static void assertEquals( Supplier<String> message, Object expected, Object actual )
    {
        if ( !Objects.equals( expected, actual ) )
        {
            if ( expected instanceof String && actual instanceof String )
            {
                String cleanMessage = message.get();
                if ( cleanMessage == null )
                {
                    cleanMessage = "";
                }
                throw new ComparisonFailure( cleanMessage, (String) expected, (String) actual );
            }
            else
            {
                fail( format( message.get(), expected, actual ) );
            }
        }
    }

    private static String format( String message, Object expected, Object actual )
    {
        String formatted = "";
        if ( message != null && !message.equals( "" ) )
        {
            formatted = message + " ";
        }
        String expectedString = String.valueOf( expected );
        String actualString = String.valueOf( actual );
        if ( expectedString.equals( actualString ) )
        {
            return formatted + "expected: "
                   + formatClassAndValue( expected, expectedString )
                   + " but was: " + formatClassAndValue( actual, actualString );
        }
        else
        {
            return formatted + "expected:<" + expectedString + "> but was:<"
                   + actualString + ">";
        }
    }

    private static String formatClassAndValue( Object value, String valueString )
    {
        String className = value == null ? "null" : value.getClass().getName();
        return className + "<" + valueString + ">";
    }
}
