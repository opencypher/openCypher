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
package org.opencypher.tools.grammar;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.opencypher.grammar.Grammar;
import org.opencypher.tools.Option;
import org.opencypher.tools.io.Output;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;

import static org.opencypher.tools.Option.dynamicOptions;
import static org.opencypher.tools.Reflection.lambdaClass;
import static org.opencypher.tools.Reflection.pathOf;
import static org.opencypher.tools.grammar.Main.execute;

/**
 * Base class for a command line tool. Handles configuration parameters.
 */
abstract class Tool implements Function<Method, Object>
{
    private static final String FX_FONT = "javafx.scene.text.Font";
    private static final Method FX_FONT_METHOD;
    private final String prefix;
    private final Map<?, ?> properties;
    private final Path workingDir;

    Tool( Path workingDir, Map<?, ?> properties )
    {
        this.workingDir = workingDir;
        this.prefix = getClass().getSimpleName() + ".";
        this.properties = properties;
    }

    static
    {
        Method method;
        try
        {
            method = Class.forName( FX_FONT ).getMethod( "font", String.class );
        }
        catch ( Throwable e )
        {
            method = null;
        }
        FX_FONT_METHOD = method;
    }

    interface Constructor<T> extends Serializable
    {
        T create( Path workingDir, Map<?, ?> properties );
    }

    interface Entry<T>
    {
        void invoke( T tool, Grammar grammar, Output output ) throws Exception;
    }

    protected static <T extends Tool> void main( Constructor<T> constructor, Entry<T> entry, String... args )
            throws Exception
    {
        execute( new Main()
        {
            @Override
            public void write( Grammar grammar, Path workingDir, OutputStream out ) throws Exception
            {
                entry.invoke( constructor.create( workingDir, System.getProperties() ), grammar, Output.output( out ) );
            }

            @Override
            public String usage( BiFunction<String, String, String> usage )
            {
                Class<?> implClass = lambdaClass( constructor );
                return usage.apply( pathOf( implClass ), implClass.getName() );
            }
        }, args );
    }

    @SafeVarargs
    protected final <T> T options( Class<T> type, Option<T>... options )
    {
        return dynamicOptions( type, this, options );
    }

    protected final Path outputDir() throws IOException
    {
        Object outputDir = get( "outputDir" );
        Path path;
        if ( outputDir instanceof Path )
        {
            path = (Path) outputDir;
        }
        else if ( outputDir instanceof String )
        {
            path = Paths.get( (String) outputDir );
        }
        else
        {
            path = Paths.get( "." );
        }
        Path output = path.normalize().toAbsolutePath();
        if ( !Files.isDirectory( output ) )
        {
            Files.createDirectories( output );
        }
        else
        {
            Object clearOutputDir = get( "clearOutputDir" );
            boolean clear = false;
            if ( clearOutputDir instanceof Boolean )
            {
                clear = (Boolean) clearOutputDir;
            }
            else if ( clearOutputDir instanceof String )
            {
                clear = parseBoolean( (String) clearOutputDir );
            }
            if ( clear )
            {
                clearDirectory( output );
            }
        }
        return output;
    }

    private String lookup( String name )
    {
        Object value = get( name );
        return value instanceof String ? (String) value : null;
    }

    private Object get( String name )
    {
        return properties.get( prefix + name );
    }

    @Override
    public final Object apply( Method key )
    {
        Class<?> type = key.getReturnType();
        String name = key.getName();
        Object value = get( name );
        if ( type.isInstance( value ) )
        {
            return value;
        }
        else if ( value instanceof String )
        {
            String param = (String) value;
            switch ( type.getName() )
            {
            case "float":
                return parseFloat( param );
            case "double":
                return parseDouble( param );
            case "long":
                return parseLong( param );
            case "int":
                return parseInt( param );
            case "short":
                return parseShort( param );
            case "byte":
                return parseByte( param );
            case "boolean":
                return parseBoolean( param );
            case "java.awt.Font":
                return awtFont( key, param );
            case "java.nio.file.Path":
                return path( param );
            case FX_FONT:
                return fxFont( key, param );
            default:
                return transform( type, param );
            }
        }
        else if ( value == null )
        {
            if ( type == java.awt.Font.class )
            {
                return awtFont( key, lookup( name + ".name" ) );
            }
            else if ( FX_FONT.equals( type.getName() ) )
            {
                return fxFont( key, lookup( name + ".family" ) );
            }
        }
        return null;
    }

    protected <T> T transform( Class<T> type, String value )
    {
        return null;
    }

    private Path path( String path )
    {
        return workingDir.resolve( path );
    }

    private java.awt.Font awtFont( Method method, String font )
    {
        String name = method.getName();
        String bold = lookup( name + ".bold" );
        String italic = lookup( name + ".italic" );
        String size = lookup( name + ".size" );
        if ( font == null && bold == null && italic == null && size == null )
        {
            return null;
        }
        if ( font == null || size == null )
        {
            try
            {
                java.awt.Font def = (java.awt.Font) method.invoke( Option.options( method.getDeclaringClass() ) );
                if ( font == null )
                {
                    font = def.getName();
                }
                if ( size == null )
                {
                    size = "" + def.getSize();
                }
            }
            catch ( IllegalAccessException | InvocationTargetException e )
            {
                if ( font == null )
                {
                    font = "sans";
                }
                if ( size == null )
                {
                    size = "10";
                }
            }
        }
        int style = 0;
        if ( parseBoolean( bold ) )
        {
            style |= java.awt.Font.BOLD;
        }
        if ( parseBoolean( italic ) )
        {
            style |= java.awt.Font.ITALIC;
        }
        return new java.awt.Font( font, style, parseInt( size ) );
    }

    private Object fxFont( Method method, String family )
    {
        try
        {
            return FX_FONT_METHOD.invoke( null, family );
        }
        catch ( RuntimeException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new UnsupportedOperationException( "Failed to invoke JavaFX font factory", e );
        }
    }

    private static void clearDirectory( final Path output ) throws IOException
    {
        Files.walkFileTree( output, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException
            {
                Files.delete( file );
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException
            {
                if ( exc != null )
                {
                    return FileVisitResult.TERMINATE;
                }
                else if ( !dir.equals( output ) )
                {
                    Files.delete( dir );
                }
                return FileVisitResult.CONTINUE;
            }
        } );
    }
}
