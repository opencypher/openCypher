package org.opencypher.tools.xml;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.filterReturnValue;

interface Reference extends Serializable
{
    static <T, R> Function<T, R> function( Function<T, R> reference )
    {
        return reference;
    }

    static <T> ToIntFunction<T> toInt( ToIntFunction<T> reference )
    {
        return reference;
    }

    static <T> ToLongFunction<T> toLong( ToLongFunction<T> reference )
    {
        return reference;
    }

    static <T> ToBoolFunction<T> toBool( ToBoolFunction<T> reference )
    {
        return reference;
    }

    static <T> ToDoubleFunction<T> toDouble( ToDoubleFunction<T> reference )
    {
        return reference;
    }

    interface Function<T, R> extends Reference
    {
        R apply( T t );

        default <V> Function<T, V> then( Function<? super R, ? extends V> after )
        {
            return new Function<T, V>()
            {
                @Override
                public V apply( T value )
                {
                    return after.apply( Function.this.apply( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( Function.this.mh(), after.mh() );
                }
            };
        }
    }

    interface IntFunction<R> extends Reference
    {
        R apply( int value );
    }

    interface ToIntFunction<T> extends Reference
    {
        int applyAsInt( T value );

        default <R> Function<T, R> then( IntFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsInt( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToIntFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface LongFunction<R> extends Reference
    {
        R apply( long value );
    }

    interface ToLongFunction<T> extends Reference
    {
        long applyAsLong( T value );

        default <R> Function<T, R> then( LongFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsLong( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToLongFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface BoolFunction<R> extends Reference
    {
        R apply( boolean value );
    }

    interface ToBoolFunction<T> extends Reference
    {
        boolean applyAsBool( T value );

        default <R> Function<T, R> then( BoolFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsBool( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToBoolFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface DoubleFunction<R> extends Reference
    {
        R apply( double value );
    }

    interface ToDoubleFunction<T> extends Reference
    {
        double applyAsDouble( T value );

        default <R> Function<T, R> then( DoubleFunction<R> after )
        {
            return new Function<T, R>()
            {
                @Override
                public R apply( T value )
                {
                    return after.apply( applyAsDouble( value ) );
                }

                @Override
                public MethodHandle mh()
                {
                    return filterReturnValue( ToDoubleFunction.this.mh(), after.mh() );
                }
            };
        }
    }

    interface BiFunction<T, U, R> extends Reference
    {
        R apply( T t, U u );
    }

    default MethodHandle mh()
    {
        try
        {
            Method replace = getClass().getDeclaredMethod( "writeReplace" );
            replace.setAccessible( true );
            SerializedLambda lambda = (SerializedLambda) replace.invoke( this );
            Class<?> impl = Class.forName( lambda.getImplClass().replace( '/', '.' ) );
            switch ( lambda.getImplMethodKind() )
            {
            case MethodHandleInfo.REF_invokeStatic:
                return MethodHandles.lookup().findStatic(
                        impl, lambda.getImplMethodName(), MethodType.fromMethodDescriptorString(
                                lambda.getImplMethodSignature(), impl.getClassLoader() ) );
            case MethodHandleInfo.REF_invokeVirtual:
                return MethodHandles.lookup().findVirtual(
                        impl, lambda.getImplMethodName(), MethodType.fromMethodDescriptorString(
                                lambda.getImplMethodSignature(), impl.getClassLoader() ) );
            default:
                throw new UnsupportedOperationException( "only static and virtual methods supported" );
            }
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
