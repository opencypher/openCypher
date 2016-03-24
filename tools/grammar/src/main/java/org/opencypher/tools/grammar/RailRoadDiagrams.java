/*
 * Copyright (c) 2015-2016 "Neo Technology,"
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
 */
package org.opencypher.tools.grammar;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import javax.xml.stream.XMLStreamException;

import org.opencypher.grammar.Grammar;
import org.opencypher.railroad.Diagram;
import org.opencypher.railroad.SVGShapes;
import org.opencypher.railroad.ShapeRenderer;
import org.opencypher.tools.output.Output;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;

import static org.opencypher.railroad.SVGShapes.svgFile;
import static org.opencypher.tools.Option.dynamicOptions;
import static org.opencypher.tools.grammar.Main.execute;
import static org.opencypher.tools.output.Output.output;

public final class RailRoadDiagrams implements Function<Method, Object>, ShapeRenderer.Linker
{
    public static void write( Grammar grammar, Writer writer ) throws Exception
    {
        write( grammar, output( writer ) );
    }

    public static void write( Grammar grammar, OutputStream stream ) throws Exception
    {
        write( grammar, output( stream ) );
    }

    public static void write( Grammar grammar, Output output ) throws Exception
    {
        new RailRoadDiagrams( System.getProperties() ).generate( grammar, output );
    }

    void generate( Grammar grammar, Output output ) throws XMLStreamException, IOException
    {
        String outputDir = lookup( "outputDir" );
        if ( outputDir == null )
        {
            outputDir = ".";
        }
        Path path = Paths.get( outputDir ).normalize().toAbsolutePath();
        if ( !Files.isDirectory( path ) )
        {
            Files.createDirectories( path );
        }
        FontRenderContext fonts = new FontRenderContext( new AffineTransform(), true, true );
        ShapeRenderer<XMLStreamException> renderer = new ShapeRenderer<>( this,
                fonts, dynamicOptions( ShapeRenderer.Options.class, this ) );
        Diagram.CanvasProvider<SVGShapes, XMLStreamException> canvas = svgFile( name -> {
            Path target = path.resolve( name + ".svg" ).toAbsolutePath();
            output.printf( "Writing Railroad diagram for %s to %s%n", name, target );
            return output( target );
        } );
        for ( Diagram diagram : Diagram.build( grammar, dynamicOptions( Diagram.BuilderOptions.class, this ) ) )
        {
            diagram.render( renderer, canvas );
        }
    }

    public static void main( String... args ) throws Exception
    {
        execute( RailRoadDiagrams::write, args );
    }

    private final Map<?, ?> properties;

    RailRoadDiagrams( Map<?, ?> properties )
    {
        this.properties = properties;
    }

    @Override
    public String referenceLink( String reference )
    {
        return reference + ".svg";
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
            }
        }
        else if ( value == null && type == Font.class )
        {
            String font = lookup( name + ".name" );
            String bold = lookup( name + ".bold" );
            String italic = lookup( name + ".italic" );
            String size = lookup( name + ".size" );
            if ( font == null && bold == null && italic == null && size == null )
            {
                return null;
            }
            if ( font == null )
            {
                font = "sans";
            }
            int style = 0;
            if ( parseBoolean( bold ) )
            {
                style |= Font.BOLD;
            }
            if ( parseBoolean( italic ) )
            {
                style |= Font.ITALIC;
            }
            return new Font( font, style, size == null ? 12 : parseInt( size ) );
        }
        return null;
    }

    private String lookup( String name )
    {
        Object value = get( name );
        return value instanceof String ? (String) value : null;
    }

    private Object get( String name )
    {
        return properties.get( getClass().getSimpleName() + "." + name );
    }
}
