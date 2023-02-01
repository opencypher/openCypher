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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.ProductionTransformation;

import static java.util.Objects.requireNonNull;

import static org.opencypher.tools.Functions.flatList;

/**
 * Logical representation of a rail road diagram.
 */
public final class Diagram
{
    public interface BuilderOptions
    { // <pre>
        default boolean expandAnyCase() { return false; }
        default boolean skipNone() { return false; }
        default boolean inlineNone() { return false; }
        default boolean optimizeDiagram() { return true; }
      //</pre>

        default boolean shouldSkip( Production production )
        {
            return (production.skip() || production.inline()) && !skipNone();
        }
    }

    public interface Renderer<Canvas, Text, EX extends Exception>
    {
        default Size diagramSize( Size rootSize )
        {
            return rootSize;
        }

        void renderDiagram( String name, Canvas canvas, Figure root ) throws EX;

        Text renderText( String type, String text );

        Size sizeOfBullet();

        void renderBullet( Canvas canvas, double x, double y ) throws EX;

        Size sizeOfNothing();

        void renderNothing( Canvas canvas, double x, double y, boolean forward ) throws EX;

        Size sizeOfText( Text text );

        void renderText( Canvas canvas, double x, double y, Text text ) throws EX;

        Size sizeOfAnyCase( Text text );

        void renderAnyCase( Canvas canvas, double x, double y, Text text ) throws EX;

        Size sizeOfReference( Text name );

        void renderReference( Canvas canvas, double x, double y, String target, Text name ) throws EX;

        Size sizeOfCharset( Text text );

        void renderCharset( Canvas canvas, double x, double y, Text text, String set ) throws EX;

        Size sizeOfLine( Collection<Figure> sequence );

        void renderLine( Canvas canvas, double x, double y, Size size, List<Figure> sequence, boolean forward )
                throws EX;

        Size sizeOfBranch( Collection<Figure> branches );

        void renderBranch( Canvas canvas, double x, double y, Size size, Collection<Figure> branches, boolean forward )
                throws EX;

        Size sizeOfLoop( Figure forward, Figure backward, Text description );

        void renderLoop( Canvas canvas, double x, double y, Size size, Figure forward, Figure backward,
                         Text description, boolean forwardDirection ) throws EX;
    }

    public interface CanvasProvider<Canvas, EX extends Exception>
    {
        Canvas newCanvas( String name, double width, double height ) throws EX;
    }

    public static List<Diagram> build( Grammar grammar, BuilderOptions options )
    {
        return grammar.transform( PRODUCTION, options, flatList() );
    }

    public static Diagram build( Production production, BuilderOptions options )
    {
        return new Diagram( production.name(), FigureBuilder.build( production.definition(), options ) );
    }

    public <Canvas, EX extends Exception, R extends Renderer<Canvas, ?, EX> & CanvasProvider<? extends Canvas, ? extends EX>>
    void render( R renderer ) throws EX
    {
        render( renderer, renderer );
    }

    public <Canvas, EX extends Exception>
    void render( Renderer<Canvas, ?, EX> renderer, CanvasProvider<? extends Canvas, ? extends EX> provider ) throws EX
    {
        convert( renderer, provider, canvas -> null );
    }

    public <Result, Canvas, EX extends Exception, R extends Renderer<? super Canvas, ?, EX> &
            CanvasProvider<? extends Canvas, ? extends EX> & Function<Canvas, Result>> Result convert( R renderer )
            throws EX
    {
        return convert( renderer, renderer, renderer );
    }

    public <Result, Canvas, EX extends Exception, P extends CanvasProvider<? extends Canvas, ? extends EX> & Function<Canvas, Result>>
    Result convert( Renderer<? super Canvas, ?, EX> renderer, P provider ) throws EX
    {
        return convert( renderer, provider, provider );
    }

    public <Result, Canvas, EX extends Exception, R extends Renderer<? super Canvas, ?, EX> &
            CanvasProvider<? extends Canvas, ? extends EX>> Result convert( R renderer,
                                                                            Function<Canvas, Result> finish ) throws EX
    {
        return convert( renderer, renderer, finish );
    }

    public <Result, Canvas, EX extends Exception> Result convert(
            Renderer<? super Canvas, ?, EX> renderer,
            CanvasProvider<? extends Canvas, ? extends EX> provider,
            Function<Canvas, Result> finish ) throws EX
    {
        Size size = renderer.diagramSize( root.size( renderer ) );
        Canvas canvas = provider.newCanvas( name, size.width, size.height );
        renderer.renderDiagram( name, canvas, root );
        return finish.apply( canvas );
    }

    public String name()
    {
        return name;
    }

    public static abstract class Figure
    {
        private FigureBuilder.Conditional<Size> size = FigureBuilder.Conditional.none();

        Figure()
        {
        }

        abstract <T> Size computeSize( Renderer<?, T, ?> renderer );

        public abstract <O, T, EX extends Exception> void render(
                O target, double x, double y, Renderer<O, T, EX> renderer, boolean forward ) throws EX;

        public boolean isNothing()
        {
            return false;
        }

        public Size size( Renderer<?, ?, ?> renderer )
        {
            return (size = size.compute( this, renderer, Figure::computeSize )).get();
        }

        @Override
        public final String toString()
        {
            StringBuilder result = new StringBuilder();
            toString( result );
            return result.toString();
        }

        abstract void toString( StringBuilder result );
    }

    private static final ProductionTransformation<BuilderOptions, Optional<Diagram>, RuntimeException> PRODUCTION =
            ( options, production ) -> options.shouldSkip( production )
                                       ? Optional.empty() : Optional.of( build( production, options ) );

    private final String name;
    private final Figure root;

    private Diagram( String name, Figure root )
    {
        this.name = name;
        this.root = root;
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
        Diagram that = (Diagram) o;
        return Objects.equals( this.name, that.name ) &&
               Objects.equals( this.root, that.root );
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder().append( "Diagram{'" ).append( name ).append( "': " );
        root.toString( result );
        return result.append( "}" ).toString();
    }

    // Factory methods for constructing diagrams directly

    public static Diagram diagram( String name, Figure figure )
    {
        return new Diagram( requireNonNull( name, "name" ), FigureBuilder.root( requireNonNull( figure, "figure" ) ) );
    }

    public static Figure nothing()
    {
        return FigureBuilder.nothing();
    }

    public static Figure text( String text )
    {
        return FigureBuilder.text( text );
    }

    public static Figure anyCase( String text )
    {
        return FigureBuilder.anyCase( text );
    }

    public static Figure reference( String name )
    {
        return FigureBuilder.reference( name, name );
    }

    public static Figure charset( String set )
    {
        return FigureBuilder.charset( set );
    }

    public static Figure line( Diagram.Figure... content )
    {
        return FigureBuilder.line( content );
    }

    public static Figure branch( Diagram.Figure... branches )
    {
        return FigureBuilder.branch( branches );
    }

    public static Figure loop( Diagram.Figure forward, Diagram.Figure backwards, int minTimes, Integer maxTimes )
    {
        return FigureBuilder.loop( forward, backwards, minTimes, maxTimes );
    }
}
