package org.opencypher.generator;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.function.Consumer;

import org.opencypher.tools.output.Output;

import static java.util.Arrays.asList;

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
        try
        {
            Method replaceMethod = getClass().getDeclaredMethod( "writeReplace" );
            replaceMethod.setAccessible( true );
            SerializedLambda lambda = (SerializedLambda) replaceMethod.invoke( this );
            Class<?> implClass = Class.forName( lambda.getImplClass().replaceAll( "/", "." ) );
            Parameter parameter = asList( implClass.getDeclaredMethods() )
                    .stream()
                    .filter( method -> Objects.equals( method.getName(), lambda.getImplMethodName() ) )
                    .findFirst()
                    .orElseThrow( () -> new IllegalStateException( "Unable to find implementation method." ) )
                    .getParameters()[0];
            if ( !parameter.isNamePresent() )
            {
                throw new IllegalStateException( "Name data not present." );
            }
            return parameter.getName();
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Reflection failed." );
        }
    }
}
