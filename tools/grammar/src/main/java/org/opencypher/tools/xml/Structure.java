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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
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

import org.opencypher.tools.Reflection;

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
    private final CharactersHandler characters, comments, headers;
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
        Map<Class<?>, CharactersHandler> characters = null;
        this.nested = new ArrayList<>( children.size() );
        for ( Nested child : children )
        {
            if ( child instanceof NestedText )
            {
                if ( characters == null )
                {
                    characters = new HashMap<>();
                }
                NestedText text = (NestedText) child;
                if ( null != characters.put( text.type, text.handler ) )
                {
                    throw new IllegalStateException(
                            "Multiple text handling methods for type " + text.type.getSimpleName() );
                }
            }
            else
            {
                this.nested.add( (NestedChild) child );
            }
        }
        this.characters = textHandler( String.class, characters );
        this.comments = textHandler( Comment.class, characters );
        this.headers = textHandler( Comment.Header.class, characters );
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
                element.uri(), element.name(), attributes,
                characters, comments, headers,
                this.children, factory( create ), handler );
        // the array must be assigned before performing recursive lookup to eliminate recursive loops
        // children != null is the recursion breaker.
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
        return ( parent ) -> Reflection.invoke( create, parent );
    }

    private static class Lookup extends ClassValue<Structure>
    {
        final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        final ClassValue<List<Attr>> attributeFields = new HierarchicalClassValue<>(
                Class::getDeclaredFields, Attribute.class, this::createFieldAttribute );
        final ClassValue<List<Attr>> attributeMethods = new HierarchicalClassValue<>(
                Class::getDeclaredMethods, Attribute.class, this::createMethodAttribute );
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
            List<Attr> attributes, fields = attributeFields.get( type ), methods = attributeMethods.get( type );
            if ( methods.isEmpty() )
            {
                attributes = fields;
            }
            else if ( fields.isEmpty() )
            {
                attributes = methods;
            }
            else
            {
                (attributes = new ArrayList<>( methods )).addAll( fields );
            }
            return new Structure( this, type, element, constructors( type ), children.get( type ), attributes );
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
                        constructors.put( constructor.getParameterTypes()[0], constructor );
//                        for ( Class<?> base = constructor.getParameterTypes()[0];
//                              base != Object.class; base = base.getSuperclass() )
//                        {
//                            constructors.putIfAbsent( base, constructor );
//                        }
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
                        return textChild( types, charBuffer( invoker( method ) ) );
                    }
                }
                if ( method.getParameterCount() == 1 )
                {
                    Class<?> base = method.getParameterTypes()[0];
                    if ( base == String.class )
                    {
                        return textChild( types, string( invoker( method ) ) );
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

        private Collection<Nested> textChild( Class<?>[] types, CharactersHandler handler )
        {
            if ( types.length == 0 || (types.length == 1 && types[0] == String.class) )
            {
                return singletonList( new NestedText( String.class, handler ) );
            }
            for ( Class<?> type : types )
            {
                if ( type != Comment.class && type != Comment.Header.class )
                {
                    throw new IllegalArgumentException( "Invalid text @Child type: " + type );
                }
            }
            NestedText[] result = new NestedText[types.length];
            for ( int i = 0; i < result.length; i++ )
            {
                result[i] = new NestedText( types[i], handler );
            }
            return Arrays.asList( result );
        }

        private Collection<Attr> createMethodAttribute( Method method, Attribute attribute )
        {
            if ( method.getParameterCount() != 1 )
            {
                throw new IllegalArgumentException( "Bad attribute method: " + method );
            }
            return createAttribute( method, attribute, AttributeHandler.conversion(
                    method.getParameterTypes()[0], invoker( method ) ) );
        }

        private Collection<Attr> createFieldAttribute( Field field, Attribute attribute )
        {
            return createAttribute( field, attribute, setter( field ) );
        }

        private <T extends Member> Collection<Attr> createAttribute( T target, Attribute attribute, MethodHandle set )
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
                name = target.getName();
            }
            return singletonList( new Attr( uri, name, attribute.optional(), set ) );
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
        private final Class<?> type;
        private final CharactersHandler handler;

        NestedText( Class<?> type, CharactersHandler handler )
        {
            this.type = type;
            this.handler = handler;
        }
    }

    static CharactersHandler string( MethodHandle method )
    {
        return ( target, buffer, start, length ) -> {
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
        };
    }

    static CharactersHandler charBuffer( MethodHandle method )
    {
        return ( target, buffer, start, length ) -> {
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
        };
    }

    private static CharactersHandler textHandler( Class<?> type, Map<Class<?>, CharactersHandler> characters )
    {
        CharactersHandler handler = characters == null ? null : characters.get( type );
        return handler != null ? handler : ( target, buffer, start, length ) -> {
        };
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
