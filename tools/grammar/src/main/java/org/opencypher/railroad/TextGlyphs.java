/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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
package org.opencypher.railroad;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

public final class TextGlyphs
{
    private final String text;
    private final GlyphVector glyphs;
    private Rectangle2D bounds;

    public TextGlyphs( String text, Font font, FontRenderContext renderContext )
    {
        this.text = text;
        this.glyphs = font.createGlyphVector( renderContext, text );
    }

    public String text()
    {
        return text;
    }

    public Font getFont()
    {
        return glyphs.getFont();
    }

    public double getWidth()
    {
        return bounds().getWidth();
    }

    public double getHeight()
    {
        return bounds().getHeight();
    }

    public Shape outline( double x, double y )
    {
        return glyphs.getOutline( offsetX( x ), offsetY( y ) );
    }

    public float offsetX( double x )
    {
        return (float) (x - bounds().getMinX());
    }

    public float offsetY( double y )
    {
        return (float) (y - bounds().getMinY());
    }

    public Iterator<Shape> outlines( double x, double y )
    {
        float xOff = offsetX( x ), yOff = offsetY( y );
        int n = glyphs.getNumGlyphs();
        return new Iterator<Shape>()
        {
            int i;

            @Override
            public boolean hasNext()
            {
                return i < n;
            }

            @Override
            public Shape next()
            {
                return glyphs.getGlyphOutline( i++, xOff, yOff );
            }
        };
    }

    @Override
    public String toString()
    {
        return "TextGlyphs[" + text + "]";
    }

    private Rectangle2D bounds()
    {
        if ( bounds == null )
        {
            bounds = glyphs.getLogicalBounds();
        }
        return bounds;
    }
}
