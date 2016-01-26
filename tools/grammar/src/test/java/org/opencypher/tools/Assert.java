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
