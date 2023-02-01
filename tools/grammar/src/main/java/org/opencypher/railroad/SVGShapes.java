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
package org.opencypher.railroad;

import java.awt.Font;
import java.util.Formatter;
import java.util.Locale;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import org.opencypher.tools.io.Output;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SVGShapes implements ShapeRenderer.Shapes<XMLStreamException>
{
    public static final DOMFactory SVG_DOM = new DOMFactory();

    public static Diagram.CanvasProvider<SVGShapes, XMLStreamException> svgFile( Function<String, Output> output )
    {
        return ( name, width, height ) -> new SVGShapes(
                new StreamResult( output.apply( name ).writer() ), width, height );
    }

    public static final class DOMFactory
            implements Diagram.CanvasProvider<SVGShapes, XMLStreamException>, Function<SVGShapes, Document>
    {
        @Override
        public final Document apply( SVGShapes svg )
        {
            Node node = ((DOMResult) svg.result).getNode();
            if ( node instanceof Document )
            {
                return (Document) node;
            }
            else
            {
                return node.getOwnerDocument();
            }
        }

        @Override
        public final SVGShapes newCanvas( String name, double width, double height ) throws XMLStreamException
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware( true );
                DocumentBuilder builder = factory.newDocumentBuilder();
                return new SVGShapes( new DOMResult( builder.newDocument() ), width, height );
            }
            catch ( Exception e )
            {
                throw new XMLStreamException( e );
            }
        }
    }

    private class SVGPath implements Path<XMLStreamException>
    {
        private static final String ARC = "A";
        private static final String CLOSE = "Z";
        private static final String CUBIC_TO = "C";
        private static final String MOVE = "M";
        private static final String LINE_TO = "L";
        private static final String VERTICAL_LINE_TO = "V";
        private static final String HORIZONTAL_LINE_TO = "H";
        private static final String QUAD_TO = "Q";
        private static final String SMOOTH_QUAD_TO = "T";
        private final Style style;
        private final StringBuilder path = new StringBuilder();
        double x, y;

        SVGPath( Style style )
        {
            this.style = style;
        }

        @Override
        public void arc( double rx, double ry, double rotation, boolean largeArc, boolean sweep, double x, double y )
        {
            append( ARC );
            point( rx, ry );
            path.append( ' ' ).append( rotation )
                .append( ' ' ).append( largeArc ? '1' : '0' )
                .append( ' ' ).append( sweep ? '1' : '0' );
            point( x, y );
        }

        @Override
        public void closePath()
        {
            append( CLOSE );
        }

        @Override
        public void moveTo( double x, double y )
        {
            append( MOVE );
            point( x, y );
        }

        @Override
        public void lineTo( double x, double y )
        {
            append( LINE_TO );
            point( x, y );
        }

        @Override
        public void quadTo( double x1, double y1, double x2, double y2 )
        {
            append( QUAD_TO );
            point( x1, y1 );
            point( x2, y2 );
        }

        @Override
        public void cubicTo( double x1, double y1, double x2, double y2, double x3, double y3 )
        {
            append( CUBIC_TO );
            point( x1, y1 );
            point( x2, y2 );
            point( x3, y3 );
        }

        @Override
        public void close() throws XMLStreamException
        {
            startTag( PATH );
            writeStyle( style );
            attribute( "d", path.toString() );
            endTag();
        }

        private void append( String command )
        {
            if ( path.length() != 0 )
            {
                path.append( ' ' );
            }
            path.append( command );
        }

        private void point( double x, double y )
        {
            this.x = x;
            this.y = y;
            path.append( ' ' );
            path.append( x );
            path.append( ' ' );
            path.append( y );
        }
    }

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg",
            XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace",
            XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
    private static final String SVG = "svg", G = "g", LINE = "line", RECT = "rect", PATH = "path", TEXT = "text", CIRCLE = "circle";

    private final XMLStreamWriter writer;
    private final Result result;
    private final double width;
    private final double height;

    private SVGShapes( Result result, double width, double height ) throws XMLStreamException
    {
        this.result = result;
        this.width = width;
        this.height = height;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        this.writer = factory.createXMLStreamWriter( result );
    }

    @Override
    public void begin() throws XMLStreamException
    {
        writer.setDefaultNamespace( SVG_NAMESPACE );
        writer.writeStartDocument();
        startTag( SVG );
        writer.writeNamespace( "svg", SVG_NAMESPACE );
        writer.setPrefix( "svg", SVG_NAMESPACE );
        writer.writeNamespace( "xml", XML_NAMESPACE );
        writer.setPrefix( "xml", XML_NAMESPACE );
        writer.writeNamespace( "xlink", XLINK_NAMESPACE );
        writer.setPrefix( "xlink", XLINK_NAMESPACE );
        attribute( "width", format( "%.3f", width ) );
        attribute( "height", format( "%.3f", height ) );
        attribute( "style", "stroke:black;" );
        startTag( G );
    }

    @Override
    public void roundRect( Style style, double x, double y, double width, double height, double diameter )
            throws XMLStreamException
    {
        startTag( RECT );
        attribute( "x", format( "%.3f", x ) );
        attribute( "y", format( "%.3f", y ) );
        attribute( "width", format( "%.3f", width ) );
        attribute( "height", format( "%.3f", height ) );
        attribute( "rx", format( "%.3f", diameter / 2 ) );
        attribute( "ry", format( "%.3f", diameter / 2 ) );
        writeStyle( style );
        endTag();
    }

    @Override
    public void rect( Style style, double x, double y, double width, double height ) throws XMLStreamException
    {
        startTag( RECT );
        attribute( "x", format( "%.3f", x ) );
        attribute( "y", format( "%.3f", y ) );
        attribute( "width", format( "%.3f", width ) );
        attribute( "height", format( "%.3f", height ) );
        writeStyle( style );
        endTag();
    }

    @Override
    public void arc( Style style, double cx, double cy, double radius, double start, double extent )
            throws XMLStreamException
    {
        if ( radius == 0 )
        {
            return;
        }
        if ( extent >= 360 || extent <= -360 )
        {
            startTag( CIRCLE );
            attribute( "r", format( "%.3f", radius ) );
            attribute( "cx", format( "%.3f", cx ) );
            attribute( "cy", format( "%.3f", cy ) );
            writeStyle( style );
            endTag();
        }
        else
        {
            double s = Math.toRadians( -start ), e = Math.toRadians( -start - extent );
            double sx = cx + Math.cos( s ) * radius, sy = cy + Math.sin( s ) * radius;
            double ex = cx + Math.cos( e ) * radius, ey = cy + Math.sin( e ) * radius;
            boolean large, sweep;
            if ( extent > 0 )
            {
                large = extent > 180;
                sweep = false;
            }
            else
            {
                large = extent < -180;
                sweep = true;
            }
            try ( SVGPath path = path( style ) )
            {
                path.moveTo( sx, sy );
                path.arc( radius, radius, 0, large, sweep, ex, ey );
            }
        }
    }

    @Override
    public void line( Style style, double x1, double y1, double x2, double y2 ) throws XMLStreamException
    {
        startTag( LINE );
        attribute( "x1", format( "%.3f", x1 ) );
        attribute( "y1", format( "%.3f", y1 ) );
        attribute( "x2", format( "%.3f", x2 ) );
        attribute( "y2", format( "%.3f", y2 ) );
        endTag();
    }

    @Override
    public void text( TextGlyphs text, double x, double y ) throws XMLStreamException
    {
        startTag( TEXT );
        attribute( "x", format( "%.3f", text.offsetX( x ) ) );
        attribute( "y", format( "%.3f", text.offsetY( y ) ) );
        writeStyle( text.getFont() );
        writer.writeAttribute( "xml", XML_NAMESPACE, "space", "preserve" );
        writer.writeCharacters( text.text() );
        endTag();
    }

    @Override
    public SVGPath path( Style style )
    {
        return new SVGPath( style );
    }

    @Override
    public void end() throws XMLStreamException
    {
        endTag();
        endTag();
        writer.writeEndDocument();
        writer.close();
    }

    @Override
    public Group<XMLStreamException> group( String link ) throws XMLStreamException
    {
        if ( link != null )
        {
            startTag( "a" );
            writer.writeAttribute( "xlink", XLINK_NAMESPACE, "href", link );
            attribute( "target", "_parent" );
        }
        else
        {
            startTag( "g" );
        }
        return new Group<XMLStreamException>()
        {
            @Override
            public void roundRect( Style style, double x, double y, double width, double height,
                                   double diameter ) throws XMLStreamException
            {
                SVGShapes.this.roundRect( style, x, y, width, height, diameter );
            }

            @Override
            public void rect( Style style, double x, double y, double width, double height )
                    throws XMLStreamException
            {
                SVGShapes.this.rect( style, x, y, width, height );
            }

            @Override
            public void arc( Style style, double cx, double cy, double radius, double start,
                             double extent ) throws XMLStreamException
            {
                SVGShapes.this.arc( style, cx, cy, radius, start, extent );
            }

            @Override
            public void text( TextGlyphs text, double x, double y ) throws XMLStreamException
            {
                SVGShapes.this.text( text, x, y );
            }

            @Override
            public Path<XMLStreamException> path( Style style )
            {
                return new SVGPath( style );
            }

            @Override
            public void close() throws XMLStreamException
            {
                endTag();
            }
        };
    }

    private void writeStyle( Font font ) throws XMLStreamException
    {
        StringBuilder style = new StringBuilder();
        style.append( "font-family:" ).append( font.getName() ).append( ';' );
        style.append( " font-size:" ).append( font.getSize() ).append( "px;" );
        if ( font.isBold() )
        {
            style.append( " font-weight:bold;" );
        }
        else
        {
            style.append( " font-weight:normal;" );
        }
        if ( font.isItalic() )
        {
            style.append( " font-style:italic;" );
        }
        else
        {
            style.append( " font-style:normal;" );
        }
        style.append( " stroke:none;" );
        attribute( "style", style.toString() );
    }

    private void writeStyle( Style style ) throws XMLStreamException
    {
        if ( style.fill )
        {
            attribute( "style", "stroke:none;" );
        }
        else
        {
            attribute( "style", "fill:none;" );
        }
    }

    private void startTag( String tag ) throws XMLStreamException
    {
        writer.writeStartElement( "svg", tag, SVG_NAMESPACE );
    }

    private void attribute( String name, String value ) throws XMLStreamException
    {
//        writer.writeAttribute( "svg", SVG_NAMESPACE, name, value );
        writer.writeAttribute( name, value );
    }

    private void endTag() throws XMLStreamException
    {
        writer.writeEndElement();
    }

    private static String format( String format, Object... args )
    {
        return String.format( Locale.US, format, args );
    }
}
