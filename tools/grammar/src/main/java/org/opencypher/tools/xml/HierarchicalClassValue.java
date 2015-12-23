package org.opencypher.tools.xml;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

final class HierarchicalClassValue<T, M extends AccessibleObject, A extends Annotation> extends ClassValue<List<T>>
{
    private final Function<Class<?>, M[]> accessor;
    private final Class<A> annotation;
    private final BiFunction<M, A, Collection<T>> factory;

    HierarchicalClassValue( Function<Class<?>, M[]> accessor, Class<A> annotation,
                            BiFunction<M, A, Collection<T>> factory )
    {
        this.accessor = accessor;
        this.annotation = annotation;
        this.factory = factory;
    }

    @Override
    protected final List<T> computeValue( Class<?> type )
    {
        Class<?> parent = type.getSuperclass();
        List<T> result = new ArrayList<>();
        if ( parent != Object.class )
        {
            result.addAll( get( parent ) );
        }
        for ( M member : accessor.apply( type ) )
        {
            A annotation = member.getAnnotation( this.annotation );
            if ( annotation != null )
            {
                result.addAll( factory.apply( member, annotation ) );
            }
        }
        return result;
    }
}
