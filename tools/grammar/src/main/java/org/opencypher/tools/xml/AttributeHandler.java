package org.opencypher.tools.xml;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.filterArguments;

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

    public void apply( Object target, String value )
    {
        try
        {
            setter.invokeWithArguments( target, value );
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

    private static final Map<Class<?>, Function<MethodHandle, MethodHandle>> CONVERSION = new HashMap<>();

    public static MethodHandle conversion( Class<?> type, MethodHandle setter )
    {
        return CONVERSION.getOrDefault( type, ( mh ) -> {
            throw new IllegalArgumentException( "Unsupported field type: " + mh.type().parameterArray()[1] );
        } ).apply( setter );
    }

    static
    {
        CONVERSION.put( String.class, Function.identity() );
        CONVERSION.put( int.class, conversion( Reference.<String>toInt( Integer::parseInt ) ) );
        CONVERSION.put( Integer.class, conversion( Reference.<String, Integer>function( Integer::valueOf ) ) );
        CONVERSION.put( boolean.class, conversion( Reference.<String>toBool( Boolean::parseBoolean ) ) );
        CONVERSION.put( Boolean.class, conversion( Reference.<String, Boolean>function( Boolean::valueOf ) ) );
        CONVERSION.put( long.class, conversion( Reference.<String>toLong( Long::parseLong ) ) );
        CONVERSION.put( Long.class, conversion( Reference.<String, Long>function( Long::valueOf ) ) );
        CONVERSION.put( double.class, conversion( Reference.<String>toDouble( Double::parseDouble ) ) );
        CONVERSION.put( Double.class, conversion( Reference.<String, Double>function( Double::valueOf ) ) );
    }

    private static Function<MethodHandle, MethodHandle> conversion( Reference reference )
    {
        MethodHandle filter = reference.mh();
        return ( mh ) -> filterArguments( mh, 1, filter );
    }
}
