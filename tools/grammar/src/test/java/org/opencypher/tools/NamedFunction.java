package org.opencypher.tools;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

@FunctionalInterface
public interface NamedFunction<T, R> extends Serializable
{
    R apply( T t );

    default String name()
    {
        try
        {
            Method replace = getClass().getDeclaredMethod( "writeReplace" );
            replace.setAccessible( true );
            SerializedLambda lambda = (SerializedLambda) replace.invoke( this );
            return lambda.getImplMethodName();
        }
        catch ( RuntimeException | Error e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
