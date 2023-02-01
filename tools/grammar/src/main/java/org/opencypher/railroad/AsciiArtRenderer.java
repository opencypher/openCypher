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

import java.util.Collection;
import java.util.List;

public class AsciiArtRenderer extends PositionedText.Renderer
{
    @Override
    public Size sizeOfBullet()
    {
        return new Size( 1, 1, 1 );
    }

    /**
     * <b>Example:</b>
     * {@code o}
     */
    @Override
    public void renderBullet( PositionedText target, double x, double y )
    {
        target.add( x, y, "o" );
    }

    @Override
    public Size sizeOfNothing()
    {
        return new Size( 1, 1, 1 );
    }

    /**
     * <b>Example:</b>
     * {@code >}
     */
    @Override
    public void renderNothing( PositionedText target, double x, double y, boolean forward )
    {
        target.add( x, y, ">" );
    }

    @Override
    public Size sizeOfText( String text )
    {
        return new Size( text.length() + 2, 1, 1 );
    }

    /**
     * <b>Example:</b>
     * {@code (FOO)}
     */
    @Override
    public void renderText( PositionedText target, double x, double y, String text )
    {
        target.add( x, y, "(" + text + ")" );
    }

    @Override
    public Size sizeOfAnyCase( String text )
    {
        return new Size( text.length() + 2, 1, 1 );
    }

    /**
     * <b>Example:</b>
     * {@code /FOO/}
     */
    @Override
    public void renderAnyCase( PositionedText target, double x, double y, String text )
    {
        target.add( x, y, "/" + text + "/" );
    }

    @Override
    public Size sizeOfReference( String name )
    {
        return new Size( name.length() + 2, 1, 1 );
    }

    /**
     * <b>Example:</b>
     * {@code |foo|}
     */
    @Override
    public void renderReference( PositionedText canvas, double x, double y, String target, String name )
    {
        canvas.add( x, y, "|" + name + "|" );
    }

    @Override
    public Size sizeOfCharset( String text )
    {
        return new Size( text.length(), 1, 1 );
    }

    /**
     * <b>Examples:</b>
     * <ul>
     * <li>{@code [:ID_Start:]}
     * <li>{@code [^a-z]}
     * </ul>
     */
    @Override
    public void renderCharset( PositionedText target, double x, double y, String text, String set )
    {
        target.add( x, y, text );
    }

    @Override
    public Size sizeOfLine( Collection<Diagram.Figure> sequence )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    /**
     * <b>Example:</b>
     * <pre><code>
     * o-&gt;|alpha|-&gt;(,)-&gt;|beta|-&gt;o
     * </code></pre>
     */
    @Override
    public void renderLine( PositionedText target, double x, double y,
                            Size size, List<Diagram.Figure> sequence, boolean forward )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Size sizeOfBranch( Collection<Diagram.Figure> branches )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    /**
     * <b>Examples:</b>
     * <pre><code>
     * o-+--&gt;|one|--+-&gt;o
     *   |          |
     *   +--&gt;|two|--+
     *   |          |
     *   +-&gt;|three|-+
     * </code></pre>
     */
    @Override
    public void renderBranch( PositionedText target, double x, double y,
                              Size size, Collection<Diagram.Figure> branches, boolean forward )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    @Override
    public Size sizeOfLoop( Diagram.Figure forward, Diagram.Figure backward, String description )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }

    /**
     * <b>Example:</b>
     * <pre><code>
     *   +--|two|&lt;-+
     *   |         |
     * o-+-&gt;|one|--+-&gt;o
     * </code></pre>
     */
    @Override
    public void renderLoop( PositionedText target, double x, double y, Size size, Diagram.Figure forward,
                            Diagram.Figure backward, String description, boolean forwardDirection )
    {
        throw new UnsupportedOperationException( "not implemented" );
    }
}
