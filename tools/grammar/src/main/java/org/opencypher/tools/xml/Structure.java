package org.opencypher.tools.xml;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Collections.singletonList;

class Structure
{
    public static Structure tree( Class<?> root )
    {
        return new Lookup().get( root );
    }

    private final Lookup lookup;
    private final Class<?> type;
    private final Element element;
    private final Map<Class<?>, Constructor<?>> constructors;
    private final AttributeHandler[] attributes;
    private final CharactersHandler characters;
    private final List<NestedChild> nested;
    private NodeBuilder[] children;

    private Structure( Lookup lookup, Class<?> type, Element element, Map<Class<?>, Constructor<?>> constructors,
                       List<Nested> children, List<Attr> attributes )
    {
        this.lookup = lookup;
        this.type = type;
        this.element = element;
        this.constructors = constructors;
        this.attributes = new AttributeHandler[attributes.size()];
        for ( int i = 0; i < this.attributes.length; i++ )
        {
            this.attributes[i] = attributes.get( i ).handler( element.uri() );
        }
        CharactersHandler characters = null;
        this.nested = new ArrayList<>( children.size() );
        for ( Nested child : children )
        {
            if ( child instanceof NestedText )
            {
                if ( characters != null )
                {
                    throw new IllegalStateException( "Multiple text handling methods." );
                }
                characters = ((NestedText) child).handler;
            }
            else
            {
                this.nested.add( (NestedChild) child );
            }
        }
        this.characters = characters != null ? characters : ( target, buffer, start, length ) -> {
        };
    }

    public NodeBuilder factory( Class<?> parent, BiConsumer<Object, Object> handler )
    {
        if ( parent != null )
        {
            for ( Class<?> type = parent; type != Object.class; type = type.getSuperclass() )
            {
                Constructor<?> constructor = constructors.get( type );
                if ( constructor != null )
                {
                    return newFactory( constructor, handler );
                }
            }
        }
        Constructor<?> constructor = constructors.get( null );
        if ( constructor != null )
        {
            return newFactory( constructor, handler );
        }
        throw new IllegalStateException( "No constructor of " + type + " for parent: " + parent );
    }

    private NodeBuilder newFactory( Constructor<?> constructor, BiConsumer<Object, Object> handler )
    {
        MethodHandle create;
        try
        {
            constructor.setAccessible( true );
            create = MethodHandles.publicLookup().unreflectConstructor( constructor );
        }
        catch ( IllegalAccessException e )
        {
            throw new IllegalStateException( e );
        }
        if ( constructor.getParameterCount() == 0 )
        {
            create = MethodHandles.dropArguments( create, 0, Object.class );
        }
        NodeBuilder[] children = null;
        if ( this.children == null )
        {
            this.children = children = new NodeBuilder[this.nested.size()];
        }
        NodeBuilder builder = new NodeBuilder(
                element.uri(), element.name(), attributes, characters, this.children, factory( create ), handler );
        if ( children != null )
        {
            for ( int i = 0; i < children.length; i++ )
            {
                NestedChild nested = this.nested.get( i );
                children[i] = lookup.get( nested.type ).factory( type, nested.adder );
            }
        }
        return builder;
    }

    private static Function<Object, Object> factory( MethodHandle create )
    {
        return ( parent ) -> {
            try
            {
                return create.invokeWithArguments( parent );
            }
            catch ( RuntimeException | Error e )
            {
                throw e;
            }
            catch ( Throwable throwable )
            {
                throw new RuntimeException( throwable );
            }
        };
    }

    private static class Lookup extends ClassValue<Structure>
    {
        final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        final ClassValue<List<Attr>> attributes = new HierarchicalClassValue<>(
                Class::getDeclaredFields, Attribute.class, this::createAttribute );
        final ClassValue<List<Nested>> children = new HierarchicalClassValue<>(
                Class::getDeclaredMethods, Child.class, this::createNestedChild );

        @Override
        protected Structure computeValue( Class<?> type )
        {
            Element element = type.getAnnotation( Element.class );
            if ( element == null )
            {
                throw new IllegalArgumentException( "Not an element: " + type );
            }
            return new Structure( this, type, element, constructors( type ), children.get( type ),
                                  attributes.get( type ) );
        }

        private Map<Class<?>, Constructor<?>> constructors( Class<?> type )
        {
            Map<Class<?>, Constructor<?>> constructors = new HashMap<>();
            for ( Constructor<?> constructor : type.getDeclaredConstructors() )
            {
                if ( !Modifier.isPrivate( constructor.getModifiers() ) )
                {
                    if ( constructor.getParameterCount() == 0 )
                    {
                        constructors.put( null, constructor );
                    }
                    else if ( constructor.getParameterCount() == 1 )
                    {
                        for ( Class<?> base = constructor.getParameterTypes()[0];
                              base != Object.class; base = base.getSuperclass() )
                        {
                            constructors.putIfAbsent( base, constructor );
                        }
                    }
                }
            }
            if ( constructors.isEmpty() )
            {
                throw new IllegalArgumentException( "Cannot construct: " + type );
            }
            return constructors;
        }

        private Collection<Nested> createNestedChild( Method method, Child child )
        {
            if ( method.getReturnType() == void.class )
            {
                Class<?>[] types = child.value();
                if ( method.getParameterCount() == 3 )
                {
                    Class<?>[] parameter = method.getParameterTypes();
                    if ( parameter[0] == char[].class && parameter[1] == int.class && parameter[2] == int.class )
                    {
                        return singletonList( NestedText.charBuffer( invoker( method ) ) );
                    }
                }
                if ( method.getParameterCount() == 1 )
                {
                    Class<?> base = method.getParameterTypes()[0];
                    if ( base == String.class )
                    {
                        return singletonList( NestedText.string( invoker( method ) ) );
                    }
                    else
                    {
                        if ( types.length == 0 )
                        {
                            types = new Class[]{base};
                        }
                        Nested[] result = new Nested[types.length];
                        for ( int i = 0; i < types.length; i++ )
                        {
                            Class<?> type = types[i];
                            if ( type.getAnnotation( Element.class ) == null || !base.isAssignableFrom( type ) )
                            {
                                throw new IllegalArgumentException( "Invalid child type: " + type );
                            }
                            result[i] = new NestedChild( type, invoker( method ) );
                        }
                        return Arrays.asList( result );
                    }
                }
            }
            throw new IllegalArgumentException( "Invalid @Child method: " + method );
        }

        private Collection<Attr> createAttribute( Field field, Attribute attribute )
        {
            String uri = attribute.uri();
            if ( uri.isEmpty() )
            {
                uri = null;
            }
            else if ( !attribute.optional() )
            {
                throw new IllegalArgumentException(
                        "Only optional attributes may define a namespace other then the namespace of the entity." );
            }
            String name = attribute.name();
            if ( name.isEmpty() )
            {
                name = field.getName();
            }
            return singletonList( new Attr( uri, name, attribute.optional(), setter( field ) ) );
        }

        private MethodHandle invoker( Method method )
        {
            try
            {
                method.setAccessible( true );
                return lookup.unreflect( method );
            }
            catch ( IllegalAccessException e )
            {
                throw new IllegalStateException( "Method should have been accessible", e );
            }
        }

        private MethodHandle setter( Field field )
        {
            MethodHandle setter;
            try
            {
                field.setAccessible( true );
                setter = lookup.unreflectSetter( field );
            }
            catch ( IllegalAccessException e )
            {
                throw new IllegalStateException( e );
            }
            return AttributeHandler.conversion( field.getType(), setter );
        }
    }

    private static abstract class Nested
    {
    }

    private static class NestedText extends Nested
    {
        private final CharactersHandler handler;

        NestedText( CharactersHandler handler )
        {
            this.handler = handler;
        }

        static NestedText charBuffer( MethodHandle method )
        {
            return new NestedText( ( target, buffer, start, length ) -> {
                try
                {
                    method.invokeWithArguments( target, buffer, start, length );
                }
                catch ( RuntimeException | Error e )
                {
                    throw e;
                }
                catch ( Throwable throwable )
                {
                    throw new RuntimeException( throwable );
                }
            } );
        }

        static NestedText string( MethodHandle method )
        {
            return new NestedText( ( target, buffer, start, length ) -> {
                try
                {
                    method.invokeExact( target, new String( buffer, start, length ) );
                }
                catch ( RuntimeException | Error e )
                {
                    throw e;
                }
                catch ( Throwable throwable )
                {
                    throw new RuntimeException( throwable );
                }
            } );
        }
    }

    private static class NestedChild extends Nested
    {
        /** The type of the child node. */
        private final Class<?> type;
        /** (Unbound) Method of the parent node, adding the child node to it. */
        private final BiConsumer<Object, Object> adder;

        NestedChild( Class<?> type, MethodHandle adder )
        {
            this.type = type;
            this.adder = adder( adder );
        }

        static BiConsumer<Object, Object> adder( MethodHandle adder )
        {
            return ( parent, child ) -> {
                try
                {
                    adder.invokeWithArguments( parent, child );
                }
                catch ( RuntimeException | Error e )
                {
                    throw e;
                }
                catch ( Throwable throwable )
                {
                    throw new RuntimeException( throwable );
                }
            };
        }
    }

    private static class Attr
    {
        private final String uri;
        private final String name;
        private final boolean optional;
        /** (Unbound) Method for setting the attribute value onto the attributed node. */
        private final MethodHandle setter;

        Attr( String uri, String name, boolean optional, MethodHandle setter )
        {
            this.uri = uri;
            this.name = name;
            this.optional = optional;
            this.setter = setter;
        }

        public AttributeHandler handler( String elementUri )
        {
            return new AttributeHandler( uri == null ? elementUri : uri, name, optional, setter );
        }
    }
}
