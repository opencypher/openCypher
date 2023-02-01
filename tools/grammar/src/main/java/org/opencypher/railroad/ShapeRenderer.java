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
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.opencypher.tools.Option;

import static org.opencypher.railroad.ShapeRenderer.Shapes.Style.OUTLINE;

public final class ShapeRenderer<EX extends Exception>
        implements Diagram.Renderer<ShapeRenderer.Shapes<? extends EX>, TextGlyphs, EX>
{
    public interface Options
    { // <pre>
        // fonts
        default Font   productionFont()        { return plainFont(); }
        default Font   textFont()              { return boldFont(); }
        default Font   anyCaseFont()           { return plainFont(); }
        default Font   charsetFont()           { return plainFont(); }
        default Font   loopDescriptionFont()   { return italicFont(); }
        // default fonts
        default Font   plainFont()             { return new Font( "Verdana", Font.PLAIN, 10 ); }
        default Font   boldFont()              { return new Font( "Verdana", Font.BOLD, 10 ); }
        default Font   italicFont()            { return new Font( "Verdana", Font.ITALIC, 10 ); }
        // diagram margins
        default double diagramMargin()         { return 8; }
        // arrow and bullet styles
        default double bulletRadius()          { return 2.5; }
        default double arrowWidth()            { return 9; }
        default double arrowHeight()           { return 7; }
        default double arrowIndent()           { return 1d/3d; }
        default double arrowBefore()           { return 8; }
        default double arrowAfter()            { return 0; }
        // token styles
        default double tokenMargin()           { return 1; }
        default double tokenPadding()          { return 1; }
        default double nonTerminalPadding()    { return 12; }
        // branch styles
        default double branchSpacing()         { return 8; }
        default double branchRadius()          { return 7; }
        default double branchBefore()          { return 0; }
        default double branchAfter()           { return 4; }
        // loop styles
        default double loopSpacing()           { return 8; }
        default double loopRadius()            { return 7; }
        default double loopBefore()            { return 6; }
        default double loopAfter()             { return 6; }
        default double loopDescriptionMargin() { return 2; }
    } //</pre>

    public interface Linker
    {
        String referenceLink( String reference );

        default String charsetLink( String charset )
        {
            return null;
        }
    }

    public interface Shapes<EX extends Exception>
    {
        default void begin() throws EX
        {
        }

        void roundRect( Style style, double x, double y, double width, double height, double diameter ) throws EX;

        void rect( Style style, double x, double y, double width, double height ) throws EX;

        void arc( Style style, double cx, double cy, double radius, double start, double extent ) throws EX;

        default void line( Style style, double x1, double y1, double x2, double y2 ) throws EX
        {
            try ( Path<EX> path = path( style ) )
            {
                path.moveTo( x1, y1 );
                path.lineTo( x2, y2 );
            }
        }

        default void polygon( Style style, Point... points ) throws EX
        {
            switch ( points.length )
            {
            case 0:
            case 1:
                break;
            case 2:
                line( style, points[0].x, points[0].y, points[1].x, points[1].y );
                break;
            default:
                try ( Path<EX> path = path( style ) )
                {
                    for ( int i = 0; i < points.length; i++ )
                    {
                        Point point = points[i];
                        if ( i == 0 )
                        {
                            path.moveTo( point.x, point.y );
                        }
                        else
                        {
                            if ( i == points.length - 1 && points[0].equals( point ) )
                            {
                                path.closePath();
                            }
                            else
                            {
                                path.lineTo( point.x, point.y );
                            }
                        }
                    }
                }
                break;
            }
        }

        void text( TextGlyphs text, double x, double y ) throws EX;

        Path<EX> path( Style style );

        default void end() throws EX
        {
        }

        default Group<EX> group( String link ) throws EX
        {
            return new Group<EX>()
            {
                @Override
                public void roundRect( Style style, double x, double y, double width, double height, double diameter )
                        throws EX
                {
                    Shapes.this.roundRect( style, x, y, width, height, diameter );
                }

                @Override
                public void rect( Style style, double x, double y, double width, double height ) throws EX
                {
                    Shapes.this.rect( style, x, y, width, height );
                }

                @Override
                public void arc( Style style, double cx, double cy, double radius, double start,
                                 double extent ) throws EX
                {
                    Shapes.this.arc( style, cx, cy, radius, start, extent );
                }

                @Override
                public void text( TextGlyphs text, double x, double y ) throws EX
                {
                    Shapes.this.text( text, x, y );
                }

                @Override
                public Path<EX> path( Style style )
                {
                    return Shapes.this.path( style );
                }

                @Override
                public void close()
                {
                }
            };
        }

        interface Group<EX extends Exception> extends AutoCloseable
        {
            void roundRect( Style style, double x, double y, double width, double height, double diameter ) throws EX;

            void rect( Style style, double x, double y, double width, double height ) throws EX;

            void arc( Style style, double cx, double cy, double radius, double start, double extent ) throws EX;

            default void line( Style style, double x1, double y1, double x2, double y2 ) throws EX
            {
                try ( Path<EX> path = path( style ) )
                {
                    path.moveTo( x1, y1 );
                    path.lineTo( x2, y2 );
                }
            }

            default void polygon( Style style, Point... points ) throws EX
            {
                switch ( points.length )
                {
                case 0:
                case 1:
                    break;
                case 2:
                    line( style, points[0].x, points[0].y, points[1].x, points[1].y );
                    break;
                default:
                    try ( Path<EX> path = path( style ) )
                    {
                        for ( int i = 0; i < points.length; i++ )
                        {
                            Point point = points[i];
                            if ( i == 0 )
                            {
                                path.moveTo( point.x, point.y );
                            }
                            else
                            {
                                if ( i == points.length - 1 && points[0].equals( point ) )
                                {
                                    path.closePath();
                                }
                                else
                                {
                                    path.lineTo( point.x, point.y );
                                }
                            }
                        }
                    }
                    break;
                }
            }

            void text( TextGlyphs text, double x, double y ) throws EX;

            Path<EX> path( Style style );

            @Override
            void close() throws EX;
        }

        interface Path<EX extends Exception> extends AutoCloseable
        {
            void arc( double rx, double ry, double xAxisRotation, boolean largeArc, boolean sweep, double x, double y );

            void closePath();

            void moveTo( double x, double y );

            void lineTo( double x, double y );

            void quadTo( double x1, double y1, double x2, double y2 );

            void cubicTo( double x1, double y1, double x2, double y2, double x3, double y3 );

            @Override
            void close() throws EX;
        }

        final class Point
        {
            public final double x, y;

            private Point( double x, double y )
            {
                this.x = x;
                this.y = y;
            }

            @Override
            public String toString()
            {
                return String.format( "Point{x=%s, y=%s}", x, y );
            }

            @Override
            public boolean equals( Object o )
            {
                if ( this == o )
                {
                    return true;
                }
                if ( o == null || getClass() != o.getClass() )
                {
                    return false;
                }
                Point point = (Point) o;
                return Double.compare( point.x, x ) == 0 &&
                       Double.compare( point.y, y ) == 0;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash( x, y );
            }
        }

        final class Style
        {
            public static final Style FILL = new Style( true, false ), OUTLINE = new Style( false, true );

            final boolean fill, stroke;

            private Style( boolean fill, boolean stroke )
            {
                this.fill = fill;
                this.stroke = stroke;
            }
        }
    }

    private static Shapes.Point point( double x, double y )
    {
        return new Shapes.Point( x, y );
    }

    private final Linker linker;
    private final FontRenderContext frc;
    private final Font productionFont, textFont, anyCaseFont, charsetFont, loopDescriptionFont;
    private final double diagramMargin;
    private final double bulletRadius, arrowWidth, arrowHeight, arrowIndent, arrowBefore, arrowAfter;
    private final double tokenPadding, tokenMargin, nonTerminalPadding;
    private final double branchSpacing, branchRadius, branchBefore, branchAfter;
    private final double loopSpacing, loopRadius, loopBefore, loopAfter, loopDescriptionMargin;
    private final Size bulletSize, arrowSize;

    @SafeVarargs
    ShapeRenderer( Option<? super Options>... options )
    {
        this( link -> null,
              new FontRenderContext( new AffineTransform(), true, true ),
              Option.options( Options.class, options ) );
    }

    public ShapeRenderer( Linker linker, FontRenderContext frc, Options options )
    {
        this.linker = linker;
        this.frc = frc;
        // Options
        this.diagramMargin = options.diagramMargin();
        // - fonts
        this.productionFont = options.productionFont();
        this.textFont = options.textFont();
        this.anyCaseFont = options.anyCaseFont();
        this.charsetFont = options.charsetFont();
        this.loopDescriptionFont = options.loopDescriptionFont();
        // - bullet style
        this.bulletRadius = options.bulletRadius();
        this.bulletSize = new Size( bulletRadius * 2, bulletRadius * 2, bulletRadius );
        // - arrow style
        this.arrowWidth = options.arrowWidth();
        this.arrowHeight = options.arrowHeight();
        this.arrowSize = new Size( arrowWidth, arrowHeight, arrowHeight / 2 );
        this.arrowIndent = options.arrowIndent();
        this.arrowBefore = options.arrowBefore();
        this.arrowAfter = options.arrowAfter();
        // - token styles
        this.tokenPadding = options.tokenPadding();
        this.tokenMargin = options.tokenMargin();
        this.nonTerminalPadding = options.nonTerminalPadding();
        // - branch styles
        this.branchSpacing = options.branchSpacing();
        this.branchRadius = options.branchRadius();
        this.branchBefore = options.branchBefore();
        this.branchAfter = options.branchAfter();
        // - loop styles
        this.loopSpacing = options.loopSpacing();
        this.loopRadius = options.loopRadius();
        this.loopBefore = options.loopBefore();
        this.loopAfter = options.loopAfter();
        this.loopDescriptionMargin = options.loopDescriptionMargin();
    }

    @Override
    public Size diagramSize( Size root )
    {
        return new Size(
                root.width + 2 * diagramMargin, root.height + 2 * diagramMargin, root.linePosition + diagramMargin );
    }

    @Override
    public void renderDiagram( String name, Shapes<? extends EX> shapes, Diagram.Figure root ) throws EX
    {
        shapes.begin();
        root.render( shapes, diagramMargin, diagramMargin, this, true );
        shapes.end();
    }

    @Override
    public TextGlyphs renderText( String type, String text )
    {
        return new TextGlyphs( text, font( type ), frc );
    }

    private Font font( String type )
    {
        switch ( type )
        {
        case "anycase":
            return anyCaseFont;
        case "charset":
            return charsetFont;
        case "reference":
            return productionFont;
        case "text":
            return textFont;
        case "loop":
            return loopDescriptionFont;
        default:
            throw new IllegalArgumentException( "Unknown text type: " + type );
        }
    }

    @Override
    public Size sizeOfBullet()
    {
        return bulletSize;
    }

    @Override
    public void renderBullet( Shapes<? extends EX> shapes, double x, double y ) throws EX
    {
        shapes.arc( OUTLINE, x + bulletRadius, y + bulletRadius, bulletRadius, 0, 360 );
    }

    @Override
    public Size sizeOfNothing()
    {
        return arrowSize;
    }

    @Override
    public void renderNothing( Shapes<? extends EX> shapes, double x, double y, boolean forward ) throws EX
    {
        arrow( shapes, x, y, forward );
    }

    private void arrow( Shapes<? extends EX> shapes, double x, double y, boolean forward ) throws EX
    {
        // TODO: draw butts on the arrows
        double width = arrowWidth, height = arrowHeight, linePos = height / 2, indent = arrowIndent * width;
        if ( forward )
        {
            shapes.polygon(
                    Shapes.Style.FILL, point( x + indent, y + linePos ),
                    point( x, y ),
                    point( x + width, y + linePos ),
                    point( x, y + height ),
                    point( x + indent, y + linePos ) );
        }
        else
        {
            shapes.polygon(
                    Shapes.Style.FILL, point( x + width - indent, y + linePos ),
                    point( x + width, y ),
                    point( x, y + linePos ),
                    point( x + width, y + height ),
                    point( x + width - indent, y + linePos ) );
        }
    }

    private void line( Shapes<? extends EX> shapes, double x1, double y1, double x2, double y2 ) throws EX
    {
        if ( x1 != x2 || y1 != y2 )
        {
            shapes.line( OUTLINE, x1, y1, x2, y2 );
        }
    }

    @Override
    public Size sizeOfText( TextGlyphs text )
    {
        double margin = tokenMargin, hPadding = tokenPadding, vPadding = tokenPadding;
        double diameter = text.getHeight() + vPadding * 2;
        double height = text.getHeight() + vPadding * 2 + margin * 2;
        double width = text.getWidth() + hPadding * 2 + margin * 2 + diameter;
        return new Size( width, height, height / 2 );
    }

    @Override
    public void renderText( Shapes<? extends EX> shapes, double x, double y, TextGlyphs text ) throws EX
    {
        double margin = tokenMargin, hPadding = tokenPadding, vPadding = tokenPadding;
        double width = text.getWidth(), height = text.getHeight();
        double diameter = vPadding + height + vPadding;
        shapes.text(
                text,
                x + margin + diameter / 2 + hPadding,
                y + margin + vPadding );
        shapes.roundRect(
                OUTLINE, x + margin, y + margin,
                width + diameter + hPadding * 2,
                height + vPadding * 2,
                diameter );
    }

    @Override
    public Size sizeOfAnyCase( TextGlyphs textGlyphs )
    {
        return sizeOfText( textGlyphs );
    }

    @Override
    public void renderAnyCase( Shapes<? extends EX> shapes, double x, double y, TextGlyphs textGlyphs ) throws EX
    {
        renderText( shapes, x, y, textGlyphs );
    }

    @Override
    public Size sizeOfReference( TextGlyphs name )
    {
        double margin = tokenMargin, hPadding = tokenPadding + nonTerminalPadding, vPadding = tokenPadding;
        double height = name.getHeight() + vPadding * 2 + margin * 2;
        double width = name.getWidth() + hPadding * 2 + margin * 2;
        return new Size( width, height, height / 2 );
    }

    @Override
    public void renderReference( Shapes<? extends EX> shapes, double x, double y, String target, TextGlyphs name ) throws EX
    {
        double margin = tokenMargin, hPadding = tokenPadding + nonTerminalPadding, vPadding = tokenPadding;
        double width = name.getWidth(), height = name.getHeight();
        Shapes.Group<? extends EX> group = shapes.group( linker.referenceLink( target ) );
        group.text(
                name,
                x + margin + hPadding,
                y + margin + vPadding );
        group.rect(
                OUTLINE, x + margin, y + margin,
                width + hPadding * 2,
                height + vPadding * 2 );
        group.close();
    }

    @Override
    public Size sizeOfCharset( TextGlyphs textGlyphs )
    {
        return sizeOfText( textGlyphs );
    }

    @Override
    public void renderCharset( Shapes<? extends EX> shapes, double x, double y, TextGlyphs text, String set ) throws EX
    {
        double margin = tokenMargin, hPadding = tokenPadding, vPadding = tokenPadding;
        double width = text.getWidth(), height = text.getHeight();
        double radius = (vPadding + height + vPadding) / 2;
        Shapes.Group<? extends EX> group = shapes.group( linker.charsetLink( set ) );
        group.text(
                text,
                x + margin + radius + hPadding,
                y + margin + vPadding );
        hexagon( group, x + margin, y + margin, radius, width + hPadding * 2 );
        group.close();
    }

    private void hexagon( Shapes.Group<? extends EX> shapes, double x, double y, double r, double w ) throws EX
    {
        shapes.polygon(
                OUTLINE,
                point( x, y + r ),
                point( x + r, y ),
                point( x + r + w, y ),
                point( x + r + w + r, y + r ),
                point( x + r + w, y + 2 * r ),
                point( x + r, y + 2 * r ),
                point( x, y + r ) );
    }

    private static class LineSize
    {
        double width, hBefore, hAfter;

        LineSize( Size size )
        {
            width = size.width;
            hBefore = size.linePosition;
            hAfter = size.height - size.linePosition;
        }

        LineSize( LineSize l, LineSize r )
        {
            width = l.width + r.width;
            hBefore = Math.max( l.hBefore, r.hBefore );
            hAfter = Math.max( l.hAfter, r.hAfter );
        }
    }

    @Override
    public Size sizeOfLine( Collection<Diagram.Figure> sequence )
    {
        double arrowWidth = arrowBefore + this.arrowWidth + arrowAfter;
        LineSize size = sequence
                .stream()
                .map( figure -> new LineSize( figure.size( this ) ) )
                .reduce( LineSize::new )
                .orElseThrow( () -> new IllegalStateException( "Empty sequence!" ) );
        return new Size(
                size.width + (sequence.size() - 1) * arrowWidth,
                size.hBefore + size.hAfter,
                size.hBefore );
    }

    @Override
    public void renderLine( Shapes<? extends EX> shapes, double x, double y, Size size, List<Diagram.Figure> sequence,
                            boolean forward ) throws EX
    {
        double arrowBefore = this.arrowBefore, arrowAfter = this.arrowAfter;
        Iterator<Diagram.Figure> figures = sequence.iterator();
        if ( !forward )
        {
            figures = reversed( sequence );
            arrowBefore = arrowAfter;
            arrowAfter = this.arrowBefore;
        }
        double currentX = x;
        while ( figures.hasNext() )
        {
            Diagram.Figure figure = figures.next();
            Size fSize = figure.size( this );
            figure.render( shapes, currentX, y + size.linePosition - fSize.linePosition, this, forward );
            currentX += fSize.width;
            if ( figures.hasNext() )
            {
                line( shapes, currentX, y + size.linePosition, currentX + arrowBefore + arrowIndent * arrowWidth,
                      y + size.linePosition );
                currentX += arrowBefore;
                arrow( shapes, currentX, y + size.linePosition - arrowHeight / 2, forward );
                currentX += arrowWidth;
                line( shapes, currentX, y + size.linePosition, currentX + arrowAfter, y + size.linePosition );
                currentX += arrowAfter;
            }
        }
    }

    private static Iterator<Diagram.Figure> reversed( List<Diagram.Figure> sequence )
    {
        return new Iterator<Diagram.Figure>()
        {
            int i = sequence.size();

            @Override
            public boolean hasNext()
            {
                return i > 0;
            }

            @Override
            public Diagram.Figure next()
            {
                return sequence.get( --i );
            }
        };
    }

    @Override
    public Size sizeOfBranch( Collection<Diagram.Figure> branches )
    {
        Size size = branches.stream()
                            .map( figure -> figure.size( this ) )
                            .reduce( ( l, r ) -> new Size(
                                    Math.max( l.width, r.width ), // the width of the widest figure
                                    l.height + r.height + branchSpacing, // accumulate the height of all figures
                                    l.linePosition ) ) // the linePosition of the first figure
                            .orElseThrow( () -> new IllegalStateException( "Empty branch" ) );
        return new Size(
                branchRadius * 4 + branchBefore + size.width + branchAfter + arrowWidth * 2,
                size.height, size.linePosition );
    }

    @Override
    public void renderBranch( Shapes<? extends EX> shapes, double x, double y, Size size,
                              Collection<Diagram.Figure> branches,
                              boolean forward ) throws EX
    {
        double maxWidth = size.width - (branchRadius * 4 + branchBefore + branchAfter + arrowWidth * 2);
        double centerX = x + maxWidth / 2;
        double before = branchBefore, after = branchAfter, radius = branchRadius, spacing = branchSpacing;
        if ( !forward )
        {
            before = after;
            after = branchBefore;
        }
        double currentY = y, lineEndY = y;
        boolean first = true;
        for ( Iterator<Diagram.Figure> figures = branches.iterator(); figures.hasNext(); )
        {
            Diagram.Figure figure = figures.next();
            Size fSize = figure.size( this );
            boolean drawArrows = !figure.isNothing();
            // # Don't draw arrows if c is a loop and its component is not Nothing;
            // # the arrows tend to appear superfluous in such a case
            // if isinstance(c, rr.Loop) and not isinstance(c.component, rr.Nothing):
            //     draw_arrows = False
            if ( !first )
            {
                shapes.arc( OUTLINE, x + 2 * radius, currentY + fSize.linePosition - radius, radius, 180, 90 );
            }
            if ( drawArrows )
            {
                arrow( shapes, x + radius * 2, currentY + fSize.linePosition - arrowHeight / 2, forward );
            }
            else
            {
                line( shapes, x + radius * 2, currentY + fSize.linePosition,
                      x + radius * 2 + arrowWidth, currentY + fSize.linePosition );
            }
            line( shapes, x + radius * 2 + arrowWidth, currentY + fSize.linePosition,
                  x + radius * 2 + arrowWidth + before, currentY + fSize.linePosition );
            double left = x + radius * 2 + arrowWidth + before;
            double figureX = centerX - fSize.width / 2 + radius * 2 + arrowWidth + before;

            line( shapes, left, currentY + fSize.linePosition,
                  figureX, currentY + fSize.linePosition );
            figure.render( shapes, figureX, currentY, this, forward );
            line( shapes, figureX + fSize.width, currentY + fSize.linePosition,
                  left + maxWidth + after, currentY + fSize.linePosition );
            if ( drawArrows )
            {
                arrow( shapes, left + maxWidth + after, currentY + fSize.linePosition - arrowHeight / 2, forward );
            }
            else
            {
                line( shapes, left + maxWidth + after, currentY + fSize.linePosition,
                      left + maxWidth + after + arrowWidth, currentY + fSize.linePosition );
            }
            if ( !first )
            {
                shapes.arc( OUTLINE, left + maxWidth + after + arrowWidth,
                            currentY + fSize.linePosition - radius,
                            radius, 270, 90 );
            }
            if ( !figures.hasNext() )
            {
                lineEndY = currentY + fSize.linePosition - radius;
            }
            currentY += spacing + fSize.height;
            first = false;
        }
        // lines to the left
        shapes.arc( OUTLINE, x, y + size.linePosition + radius, radius, 0, 90 );
        line( shapes, x + radius, y + size.linePosition + radius, x + radius, lineEndY );
        line( shapes, x, y + size.linePosition, x + radius * 2, y + size.linePosition );
        // lines to the right
        double endX = x + radius * 2 + arrowWidth + before + maxWidth + after + arrowWidth;
        line( shapes, endX, y + size.linePosition, endX + radius * 2, y + size.linePosition );
        shapes.arc( OUTLINE, endX + 2 * radius, y + size.linePosition + radius, radius, 90, 90 );
        line( shapes, endX + radius, y + size.linePosition + radius,
              endX + radius, lineEndY );
    }

    @Override
    public Size sizeOfLoop( Diagram.Figure forward, Diagram.Figure backward, TextGlyphs description )
    {
        Size fSize = forward.size( this ), bSize = backward.size( this );
        double width = Math.max( fSize.width, bSize.width ), height = fSize.height + loopSpacing + bSize.height;
        if ( description != null )
        {
            width = Math.max( width, description.getWidth() + loopDescriptionMargin );
            height += description.getHeight() + loopSpacing;
        }
        width = loopRadius * 2 + arrowWidth + loopBefore + width + loopAfter + arrowWidth + loopRadius * 2;
        double linePos = /*bSize.height + loopSpacing +*/ fSize.linePosition;
        return new Size( width, height, linePos );
    }

    @Override
    public void renderLoop( Shapes<? extends EX> shapes, double x, double y, Size size, Diagram.Figure forward,
                            Diagram.Figure backward, TextGlyphs description, boolean fwd ) throws EX
    {
        Size fSize = forward.size( this ), bSize = backward.size( this );
        boolean fArrow = !forward.isNothing(), bArrow = !backward.isNothing();
        double spacing = loopSpacing, radius = loopRadius, before = loopBefore;
        double dY = y + fSize.height + spacing;
        double width = size.width, linePos = size.linePosition;
        double maxWidth = Math.max( fSize.width, bSize.width );
        if ( description != null )
        {
            dY += description.getHeight() + spacing;
            maxWidth = Math.max( maxWidth, description.getWidth() + loopDescriptionMargin );
        }
        double centerX = x + radius * 2 + arrowWidth + before + maxWidth / 2;

        // draw the forward edge
        line( shapes, x, y + linePos, x + radius * 2, y + linePos );
        arrowOrLine( shapes, x + radius * 2, y + linePos - arrowHeight / 2, arrowWidth, arrowHeight, fwd, fArrow );
        line( shapes, x + radius * 2 + arrowWidth, y + linePos, centerX - fSize.width / 2, y + linePos );
        forward.render( shapes, centerX - fSize.width / 2, y, this, fwd );
        line( shapes, centerX + fSize.width / 2, y + linePos, x + width - radius * 2 - arrowWidth, y + linePos );
        arrowOrLine( shapes, x + width - radius * 2 - arrowWidth, y + linePos - arrowHeight / 2, arrowWidth,
                     arrowHeight, fwd, fArrow );
        line( shapes, x + width - radius * 2, y + linePos, x + width, y + linePos );

        // draw the line up from the backward edge
        shapes.arc( OUTLINE, x + 2 * radius, y + linePos + radius, radius, 90, 90 ); // upper left
        line( shapes, x + radius, y + linePos + radius,
              x + radius, dY + bSize.linePosition - radius );
        shapes.arc( OUTLINE, x + 2 * radius, dY + bSize.linePosition - radius, radius, 180, 90 ); // lower left

        // draw the backward edge
        arrowOrLine( shapes,
                     x + radius * 2,
                     dY + bSize.linePosition - arrowHeight / 2,
                     arrowWidth, arrowHeight, !fwd, bArrow );
        line( shapes, x + radius * 2 + arrowWidth, dY + bSize.linePosition,
              centerX - bSize.width / 2, dY + bSize.linePosition );
        backward.render( shapes, centerX - bSize.width / 2, dY, this, !fwd );
        line( shapes, centerX + bSize.width / 2, dY + bSize.linePosition,
              x + width - radius * 2 - arrowWidth, dY + bSize.linePosition );
        arrowOrLine( shapes, x + width - radius * 2 - arrowWidth, dY + bSize.linePosition - arrowHeight / 2, arrowWidth,
                     arrowHeight, !fwd, bArrow );

        // draw the line down to the backward edge
        shapes.arc( OUTLINE, x + width - radius * 2, dY + bSize.linePosition - radius, radius, 270, 90 ); // lower right
        line( shapes, x + width - radius, dY + bSize.linePosition - radius,
              x + width - radius, y + linePos + radius );
        shapes.arc( OUTLINE, x + width - radius * 2, y + linePos + radius, radius, 0, 90 ); // upper right

        if ( description != null )
        {
            shapes.text(
                    description,
                    x + width - description.getWidth() - radius - loopDescriptionMargin,
                    y + fSize.height + spacing );
        }
    }

    private void arrowOrLine( Shapes<? extends EX> shapes, double x, double y, double width, double height,
                              boolean forward, boolean arrow ) throws EX
    {
        if ( arrow )
        {
            arrow( shapes, x, y, forward );
        }
        else
        {
            line( shapes, x, y + height / 2, x + width, y + height / 2 );
        }
    }
}
