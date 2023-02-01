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
package org.opencypher.generator;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.grammar.Grammar;
import org.xml.sax.SAXException;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

public abstract class GeneratorFactory<T>
{
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Replacement
    {
        String[] value() default {};
    }

    /**
     * Creates a new context object for use by the generator.
     *
     * The context object may implement {@link ScopeListener} - TODO: is this a good design?
     * TODO: should scope notification be part of tree building or string generation?
     * -- An idea: we could make the Scope notion create a nested context object, so new context for new scope...
     *
     * @return a new context object for a generator session.
     */
    protected abstract T newContext();

    public final Generator generatorResource( String resource, Grammar.ParserOption... options )
            throws ParserConfigurationException, SAXException, IOException
    {
        URL url = getClass().getResource( resource );
        if ( url == null )
        {
            throw new IllegalArgumentException( "No such resource: " + resource );
        }
        try
        {
            return generator( Paths.get( url.toURI() ), options );
        }
        catch ( URISyntaxException e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    public final Generator generator( Path path, Grammar.ParserOption... options )
            throws IOException, SAXException, ParserConfigurationException
    {
        return generator( Grammar.parseXML( path, options ) );
    }

    public final Generator generator( Grammar grammar )
    {
        return new Generator( choices(), grammar, this::newContext, replacements );
    }

    protected Choices choices()
    {
        return Choices.SIMPLE;
    }

    private final ProductionReplacement<T>[] replacements;

    protected GeneratorFactory()
    {
        this.replacements = replacements();
    }

    public final ProductionReplacement<T> replacement( String production )
    {
        requireNonNull( production, "production" );
        return Stream.of( replacements ).filter( repl -> production.equals( repl.production() ) ).findFirst().get();
    }

    private static final Class[] ARGS = {ProductionReplacement.Context.class};

    private ProductionReplacement<T>[] replacements()
    {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        List<ProductionReplacement<T>> replacements = new ArrayList<>();
        Type contextType = null;
        for ( Class<?> cls = getClass(); cls != GeneratorFactory.class; cls = cls.getSuperclass() )
        {
            for ( Method method : cls.getDeclaredMethods() )
            {
                if ( "newContext".equals( method.getName() ) &&
                     method.getParameterCount() == 0 &&
                     !isStatic( method.getModifiers() ) &&
                     !method.isBridge() )
                {
                    if ( contextType != null )
                    {
                        throw new IllegalArgumentException(
                                "Could not find context type, too many newContext() methods!" );
                    }
                    contextType = method.getGenericReturnType();
                }
            }
            if ( contextType != null )
            {
                break;
            }
        }
        if ( contextType == null )
        {
            throw new IllegalArgumentException( "Could not find context type, no newContext() method found." );
        }
        for ( Method method : getClass().getMethods() )
        {
            Replacement annotation = method.getAnnotation( Replacement.class );
            if ( annotation != null )
            {
                if ( isStatic( method.getModifiers() ) )
                {
                    throw new IllegalArgumentException( "Replacement method must not be static." );
                }
                if ( !Arrays.equals( method.getParameterTypes(), ARGS ) )
                {
                    throw new IllegalArgumentException(
                            "Replacement method parameter list must be " + Arrays.toString( ARGS ) );
                }
                Type paramType = method.getGenericParameterTypes()[0];
                if ( !(paramType instanceof ParameterizedType) )
                {
                    throw new IllegalArgumentException( "Replacement method parameter must be parameterized." );
                }
                if ( !contextType.equals( ((ParameterizedType) paramType).getActualTypeArguments()[0] ) )
                {
                    throw new IllegalArgumentException(
                            "Replacement method parameter must match the context type: " + contextType +
                            ", was: " + ((ParameterizedType) paramType).getActualTypeArguments()[0] );
                }
                if ( method.getReturnType() != void.class )
                {
                    throw new IllegalArgumentException(
                            "Replacement method should not return a value (must declare void)." );
                }
                String[] productions = annotation.value();
                if ( productions == null || productions.length == 0 )
                {
                    productions = new String[]{method.getName()};
                }
                MethodHandle mh;
                try
                {
                    mh = lookup.unreflect( method );
                }
                catch ( IllegalAccessException e )
                {
                    throw new IllegalArgumentException( "Replacement method must be accessible (public)." );
                }
                mh = mh.bindTo( this );
                for ( String production : productions )
                {
                    replacements.add( replacement( production, mh ) );
                }
            }
        }
        @SuppressWarnings("unchecked")
        ProductionReplacement<T>[] array = replacements.toArray( new ProductionReplacement[replacements.size()] );
        return array;
    }

    private static <T> ProductionReplacement<T> replacement( String production, MethodHandle target )
    {
        return new ProductionReplacement<T>()
        {
            @Override
            public String production()
            {
                return production;
            }

            @Override
            public void replace( Context<T> context )
            {
                try
                {
                    target.invokeExact( context );
                }
                catch ( RuntimeException | Error e )
                {
                    throw e;
                }
                catch ( Throwable e )
                {
                    throw new RuntimeException( e );
                }
            }
        };
    }
}
