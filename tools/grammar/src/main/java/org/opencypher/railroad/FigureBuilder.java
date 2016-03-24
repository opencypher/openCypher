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
package org.opencypher.railroad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.grammar.TermTransformation;
import org.opencypher.grammar.Terms;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;

class FigureBuilder implements TermTransformation<FigureBuilder.Group, Void, RuntimeException>
{
    static Diagram.Figure nothing()
    {
        return NOTHING;
    }

    static Diagram.Figure text( String text )
    {
        return new Text( text );
    }

    static Diagram.Figure anyCase( String text )
    {
        return new AnyCase( text );
    }

    static Diagram.Figure reference( String text )
    {
        return new Reference( text );
    }

    static Diagram.Figure charset( String set )
    {
        return new Charset( set );
    }

    static Diagram.Figure line( Diagram.Figure... content )
    {
        Line line = new Line();
        Collections.addAll( line.seq, content );
        return line;
    }

    static Diagram.Figure branch( Diagram.Figure... branches )
    {
        Branch branch = new Branch();
        Collections.addAll( branch.alt, branches );
        return branch;
    }

    static Diagram.Figure loop( Diagram.Figure forward, Diagram.Figure backwards, int minTimes, Integer maxTimes )
    {
        if ( minTimes < 0 || (maxTimes != null && maxTimes < minTimes) )
        {
            throw new IllegalArgumentException( "Invalid bounds, min=" + minTimes + ", max=" + maxTimes );
        }
        return new Loop( forward, backwards, minTimes, maxTimes, false );
    }

    static Diagram.Figure root( Diagram.Figure figure )
    {
        Line line = new Line();
        line.add( BULLET );
        if ( figure instanceof Line )
        {
            line.seq.addAll( ((Line) figure).seq );
        }
        else
        {
            line.add( figure );
        }
        line.add( BULLET );
        return line;
    }

    static Diagram.Figure build( Grammar.Term term, Diagram.BuilderOptions options )
    {
        Line line = new Line();
        line.add( BULLET );
        term.transform( new FigureBuilder( options ), line );
        line.add( BULLET );
        return line;
    }

    private final boolean expandAnyCase;

    private FigureBuilder( Diagram.BuilderOptions options )
    {
        this.expandAnyCase = options.expandAnyCase();
    }

    @Override
    public Void transformAlternatives( Group group, Alternatives alternatives )
    {
        group.branch( this, alternatives );
        return null;
    }

    @Override
    public Void transformSequence( Group group, Sequence sequence )
    {
        group.line( this, sequence );
        return null;
    }

    @Override
    public Void transformLiteral( Group group, Literal literal )
    {
        if ( literal.caseSensitive() )
        {
            literal( group, literal.toString(), FigureBuilder::text );
        }
        else if ( expandAnyCase )
        {
            Line line = group instanceof Line ? (Line) group : new Line();
            literal.accept( new Literal.Visitor<RuntimeException>()
            {
                @Override
                public void visitLiteral( String literal )
                {
                    line.add( text( literal ) );
                }

                @Override
                public void visitAnyCase( int cp )
                {
                    Branch branch = new Branch();
                    StringBuilder chr = new StringBuilder( 2 );
                    chr.appendCodePoint( Character.toUpperCase( cp ) );
                    branch.add( text( chr.toString() ) );
                    chr.setLength( 0 );
                    chr.appendCodePoint( Character.toLowerCase( cp ) );
                    branch.add( text( chr.toString() ) );
                    line.add( branch );
                }
            } );
            if ( group != line && !line.seq.isEmpty() )
            {
                if ( line.seq.size() == 1 )
                {
                    group.add( line.seq.get( 0 ) );
                }
                else
                {
                    group.add( line );
                }
            }
        }
        else
        {
            literal( group, literal.toString(), FigureBuilder::anyCase );
        }
        return null;
    }

    private static void literal( Group group, String text, Function<String, Diagram.Figure> figure )
    {
        int start = 0;
        Line line = null;
        for ( int i = 0, cp; i < text.length(); i += Character.charCount( cp ) )
        {
            cp = text.codePointAt( i );
            if ( Character.isLetterOrDigit( cp ) || (cp >= 0x20 && cp < 0xFF) )
            {
                continue;
            }
            if ( line == null )
            {
                line = group instanceof Line ? (Line) group : new Line();
            }
            if ( i > start )
            {
                line.add( figure.apply( text.substring( start, i ) ) );
            }
            line.add( charset( '[' + CharacterSet.escapeCodePoint( cp ) + ']' ) );
            start = i + Character.charCount( cp );
        }
        if ( line == null ) // no special characters found
        {
            group.add( figure.apply( text ) );
        }
        else if ( group != line )
        {
            if ( line.seq.size() == 1 ) // only a single special character
            {
                group.add( line.seq.get( 0 ) );
            }
            else
            {
                group.add( line );
            }
        }
    }

    @Override
    public Void transformNonTerminal( Group group, NonTerminal nonTerminal )
    {
        if ( nonTerminal.inline() )
        {
            nonTerminal.productionDefinition().transform( this, group );
        }
        else
        {
            group.add( reference( nonTerminal.productionName() ) );
        }
        return null;
    }

    @Override
    public Void transformOptional( Group group, Optional optional )
    {
        Branch branch = new Branch();
        branch.add( NOTHING );
        optional.term().transform( this, branch );
        group.add( branch );
        return null;
    }

    @Override
    public Void transformRepetition( Group group, Repetition repetition )
    {
        Line repeated = new Line();
        repetition.term().transform( this, repeated );
        group.repetition( this, repeated, repetition.minTimes(),
                          repetition.limited() ? repetition.maxTimes() : null );
        return null;
    }

    @Override
    public Void transformEpsilon( Group group )
    {
        group.add( NOTHING );
        return null;
    }

    @Override
    public Void transformCharacters( Group group, CharacterSet characters )
    {
        group.add( new Charset( CharacterSet.Unicode.toSetString( characters ) ) );
        return null;
    }

    private void addAll( Terms terms, Group group )
    {
        terms.forEach( term -> term.transform( this, group ) );
    }

    static abstract class Node extends Diagram.Figure
    {
        final String def;
        private Conditional<Object> text = Conditional.none();

        Node( String def )
        {
            this.def = def;
        }

        final <T> T text( Diagram.Renderer<?, T, ?> renderer )
        {
            return (text = text.compute( this, renderer, Node::renderText )).get();
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer )
        {
            return computeSize( renderer, text( renderer ) );
        }

        @Override
        public <O, T, EX extends Exception> void render(
                O target, double x, double y, Diagram.Renderer<O, T, EX> renderer, boolean forward ) throws EX
        {
            render( target, x, y, renderer, text( renderer ) );
        }

        abstract <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer, T text );

        abstract <O, T, EX extends Exception> void render( O target, double x, double y,
                                                           Diagram.Renderer<O, T, EX> renderer, T text ) throws EX;

        <T> T renderText( Diagram.Renderer<?, T, ?> renderer )
        {
            return renderer.renderText( getClass().getSimpleName().toLowerCase(), def );
        }

        @Override
        public int hashCode()
        {
            return def.hashCode();
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null || getClass() != obj.getClass() )
            {
                return false;
            }
            Node that = (Node) obj;
            return def.equals( that.def );
        }

        @Override
        void toString( StringBuilder result )
        {
            result.append( getClass().getSimpleName().toLowerCase() ).append( "('" ).append( def ).append( "')" );
        }
    }

    static abstract class Group extends Diagram.Figure
    {
        void branch( FigureBuilder builder, Alternatives alternatives )
        {
            Branch branch = new Branch();
            builder.addAll( alternatives, branch );
            if ( !branch.alt.isEmpty() )
            {
                if ( branch.alt.size() == 1 )
                {
                    Diagram.Figure alt = branch.alt.iterator().next();
                    add( alt );
                }
                else
                {
                    add( branch );
                }
            }
        }

        void line( FigureBuilder builder, Sequence sequence )
        {
            Line line = new Line();
            builder.addAll( sequence, line );
            if ( !line.seq.isEmpty() )
            {
                if ( line.seq.size() == 1 )
                {
                    add( line.seq.get( 0 ) );
                }
                else
                {
                    add( line );
                }
            }
        }

        void repetition( FigureBuilder builder, Line repeated, int minTimes, Integer maxTimes )
        {
            Diagram.Figure forwards, backwards;
            if ( minTimes == 0 )
            {
                forwards = NOTHING;
                backwards = repeated;
            }
            else
            {
                forwards = repeated;
                backwards = NOTHING;
                minTimes -= 1;
                if ( maxTimes != null )
                {
                    maxTimes -= 1;
                }
            }
            add( new Loop( forwards, backwards, minTimes, maxTimes, false ) );
        }

        abstract void add( Diagram.Figure child );
    }

    private static final Diagram.Figure BULLET = new Diagram.Figure()
    {
        @Override
        public int hashCode()
        {
            return 1;
        }

        @Override
        public boolean equals( Object obj )
        {
            return this == obj;
        }

        @Override
        public Size size( Diagram.Renderer<?, ?, ?> renderer )
        {
            return computeSize( renderer );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer )
        {
            return renderer.sizeOfBullet();
        }

        @Override
        public <O, T, EX extends Exception> void render(
                O target, double x, double y, Diagram.Renderer<O, T, EX> renderer, boolean forward ) throws EX
        {
            renderer.renderBullet( target, x, y );
        }

        @Override
        void toString( StringBuilder result )
        {
            result.append( "◦" );
        }
    };
    private static final Diagram.Figure NOTHING = new Diagram.Figure()
    {
        @Override
        public int hashCode()
        {
            return 0;
        }

        @Override
        public boolean equals( Object obj )
        {
            return this == obj;
        }

        @Override
        public Size size( Diagram.Renderer<?, ?, ?> renderer )
        {
            return computeSize( renderer );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer )
        {
            return renderer.sizeOfNothing();
        }

        @Override
        public <O, T, EX extends Exception> void render(
                O target, double x, double y, Diagram.Renderer<O, T, EX> renderer, boolean forward ) throws EX
        {
            renderer.renderNothing( target, x, y, forward );
        }

        @Override
        public boolean isNothing()
        {
            return true;
        }

        @Override
        void toString( StringBuilder result )
        {
            result.append( "nothing()" );
        }
    };

    /**
     * Renders as a rounded rectangle.
     *
     * Example: {@code (FOO)}
     */
    private static class Text extends Node
    {
        Text( String text )
        {
            super( text );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer, T text )
        {
            return renderer.sizeOfText( text );
        }

        @Override
        <O, T, EX extends Exception> void render( O target, double x, double y, Diagram.Renderer<O, T, EX> renderer,
                                                  T text ) throws EX
        {
            renderer.renderText( target, x, y, text );
        }
    }

    /**
     * Renders as a rounded rectangle.
     *
     * Example: {@code /FOO/}
     */
    private static class AnyCase extends Node
    {
        AnyCase( String text )
        {
            super( text );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer, T text )
        {
            return renderer.sizeOfAnyCase( text );
        }

        @Override
        <O, T, EX extends Exception> void render( O target, double x, double y, Diagram.Renderer<O, T, EX> renderer,
                                                  T text ) throws EX
        {
            renderer.renderAnyCase( target, x, y, text );
        }

        @Override
        public int hashCode()
        {
            return def.toUpperCase().hashCode();
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null || getClass() != obj.getClass() )
            {
                return false;
            }
            AnyCase that = (AnyCase) obj;
            return def.equalsIgnoreCase( that.def );
        }
    }

    /**
     * Renders as a rectangle.
     *
     * Example: {@code |foo|}
     */
    private static class Reference extends Node
    {
        Reference( String name )
        {
            super( name );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer, T name )
        {
            return renderer.sizeOfReference( name );
        }

        @Override
        <O, T, EX extends Exception> void render( O target, double x, double y, Diagram.Renderer<O, T, EX> renderer,
                                                  T name ) throws EX
        {
            renderer.renderReference( target, x, y, name );
        }
    }

    /**
     * Renders as a hexagon
     * <p>
     * Example: {@code [a-z]}
     * <p>
     * Example: {@code [[:ANY:]-[`]]} (same as {@code [^`]})
     *
     * <pre><code>
     *     ╱
     *
     *     ╲
     * </code></pre>
     */
    private static class Charset extends Node
    {
        Charset( String set )
        {
            super( set );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer, T text )
        {
            return renderer.sizeOfCharset( text );
        }

        @Override
        <O, T, EX extends Exception> void render( O target, double x, double y, Diagram.Renderer<O, T, EX> renderer,
                                                  T text ) throws EX
        {
            renderer.renderCharset( target, x, y, text );
        }
    }

    /**
     * Line: {@code ◦->|A|->|B|->|C|->◦}
     * <p>
     * Stack:
     * <pre><code>
     * ◦->|A|--+
     *         |
     * +--|B|<-+
     * |
     * +->|C|->◦
     * </code></pre>
     */
    // TODO: we might want to add a 'Stack' type as well, which is like 'Line' but renders differently.
    private static class Line extends Group
    {
        final List<Diagram.Figure> seq = new ArrayList<>();

        @Override
        void add( Diagram.Figure child )
        {
            if ( child != NOTHING )
            {
                seq.add( child );
            }
        }

        @Override
        void line( FigureBuilder builder, Sequence sequence )
        {
            builder.addAll( sequence, this );
        }

        @Override
        void repetition( FigureBuilder builder, Line repeated, int minTimes, Integer maxTimes )
        {
            LinkedList<Diagram.Figure> common = new LinkedList<>();
            for ( int these = seq.size(), those = repeated.seq.size(); these-- > 0 && those-- > 0; )
            {
                Diagram.Figure mine = seq.get( these ), your = repeated.seq.get( those );
                if ( mine.equals( your ) )
                {
                    common.addFirst( mine );
                    seq.remove( these );
                    repeated.seq.remove( those );
                }
            }
            if ( common.isEmpty() )
            {
                super.repetition( builder, repeated, minTimes, maxTimes );
                return;
            }
            Diagram.Figure forward, backward;
            if ( common.size() == 1 )
            {
                forward = common.getFirst();
            }
            else
            {
                forward = new Line();
                ((Line) forward).seq.addAll( common );
            }
            if ( repeated.seq.isEmpty() )
            {
                backward = NOTHING;
            }
            else if ( repeated.seq.size() == 1 )
            {
                backward = repeated.seq.get( 0 );
            }
            else
            {
                backward = repeated;
            }
            add( new Loop( forward, backward, minTimes, maxTimes, false ) );
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
            Line line = (Line) o;
            return seq.equals( line.seq );
        }

        @Override
        public int hashCode()
        {
            return seq.hashCode();
        }

        @Override
        public void toString( StringBuilder result )
        {
            string( result, "line", seq );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer )
        {
            return renderer.sizeOfLine( unmodifiableList( seq ) );
        }

        @Override
        public <O, T, EX extends Exception> void render(
                O target, double x, double y, Diagram.Renderer<O, T, EX> renderer, boolean forward ) throws EX
        {
            renderer.renderLine( target, x, y, size( renderer ), unmodifiableList( seq ), forward );
        }
    }

    /**
     * <b>ASCII:</b>
     * <pre><code>
     *   +->|A|-+
     *   |      |
     * o-+->|B|-+->o
     *   |      |
     *   +->|C|-+
     * </code></pre>
     * <b>Unicode:</b>
     * <pre><code>
     *     ┏━┓
     *   ╭→┃A┃─╮
     *   │ ┗━┛ │
     *   │ ┏━┓ │
     * ○─┼→┃B┃─┼→○
     *   │ ┗━┛ │
     *   │ ┏━┓ │
     *   ╰→┃C┃─╯
     *     ┗━┛
     * </code></pre>
     */
    private static class Branch extends Group
    {
        final Set<Diagram.Figure> alt = newSetFromMap( new LinkedHashMap<>() );

        @Override
        void add( Diagram.Figure child )
        {
            alt.add( child );
        }

        @Override
        void branch( FigureBuilder builder, Alternatives alternatives )
        {
            builder.addAll( alternatives, this );
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
            Branch branch = (Branch) o;
            return alt.equals( branch.alt );
        }

        @Override
        public int hashCode()
        {
            return alt.hashCode();
        }

        @Override
        public void toString( StringBuilder result )
        {
            string( result, "branch", alt );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer )
        {
            return renderer.sizeOfBranch( unmodifiableCollection( alt ) );
        }

        @Override
        public <O, T, EX extends Exception> void render(
                O target, double x, double y, Diagram.Renderer<O, T, EX> renderer, boolean forward ) throws EX
        {
            renderer.renderBranch( target, x, y, size( renderer ), unmodifiableCollection( alt ), forward );
        }
    }

    /**
     * <b>Examples:</b>
     * <p>
     * {@code A{0-N}}
     * <pre><code>
     *   +-|A|<-+
     *   |      |
     * ◦-+------+->◦
     * </code></pre>
     * <p>
     * {@code A{1-N}}
     * <pre><code>
     *   +------+
     *   |      |
     * ◦-+->|A|-+->◦
     * </code></pre>
     * <p>
     * {@code A{5-N}}
     * <pre><code>
     *   +------+
     *   |  4..N|
     * ◦-+->|A|-+->◦
     * </code></pre>
     * <p>
     * {@code A{0-5}}
     * <pre><code>
     *   +-|A|<-+
     *   |  0..5|
     * ◦-+------+->◦
     * </code></pre>
     * <p>
     * {@code A{1-5}}
     * <pre><code>
     *   +------+
     *   |  0..4|
     * ◦-+->|A|-+->◦
     * </code></pre>
     * <p>
     * {@code A{5-10}}
     * <pre><code>
     *   +------+
     *   |  4..9|
     * ◦-+->|A|-+->◦
     * </code></pre>
     * <p>
     * {@code A (, A)*}
     * <pre><code>
     *   +-(,)<-+
     *   |      |
     * ◦-+->|A|-+->◦
     * </code></pre>
     */
    private static class Loop extends Diagram.Figure
    {
        private final Diagram.Figure forward, backward;
        /** {@code true} to loop up above the line, {@code false} to loop down below. */
        private final boolean top;
        /** at least 1 (0 represented by forward being NOTHING). */
        private final int min;
        /** greater than {@link #min}, or null for unbounded */
        private final Integer max;
        private Conditional<Object> text = Conditional.none();

        Loop( Diagram.Figure forward, Diagram.Figure backward, int min, Integer max, boolean top )
        {
            this.forward = forward;
            this.backward = backward;
            this.top = top;
            this.min = min;
            this.max = max;
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
            Loop loop = (Loop) o;
            return top == loop.top &&
                   min == loop.min &&
                   Objects.equals( forward, loop.forward ) &&
                   Objects.equals( backward, loop.backward ) &&
                   Objects.equals( max, loop.max );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( forward, backward, top, min, max );
        }

        @Override
        void toString( StringBuilder result )
        {
            result.append( "loop( " );
            forward.toString( result );
            result.append( ", " );
            backward.toString( result );
            result.append( ", min=" ).append( min );
            result.append( ", max=" ).append( max );
            result.append( " )" );
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer )
        {
            return renderer.sizeOfLoop( forward, backward, text( renderer ) );
        }

        @SuppressWarnings("unchecked")
        final <T> T text( Diagram.Renderer<?, T, ?> renderer )
        {
            return (text = text.compute( this, renderer, Loop::renderText )).get();
        }

        <T> T renderText( Diagram.Renderer<?, T, ?> renderer )
        {
            if ( min > 0 || max != null )
            {
                StringBuilder text = new StringBuilder();
                text.append( min ).append( ".." );
                if ( max == null )
                {
                    text.append( "N" );
                }
                else
                {
                    text.append( max.intValue() );
                }
                return renderer.renderText( getClass().getSimpleName().toLowerCase(), text.toString() );
            }
            else
            {
                return null;
            }
        }

        @Override
        public <O, T, EX extends Exception> void render(
                O target, double x, double y, Diagram.Renderer<O, T, EX> renderer, boolean forward ) throws EX
        {
            renderer.renderLoop( target, x, y, size( renderer ), this.forward, this.backward, text( renderer ),
                                 forward );
        }
    }

    private static void string( StringBuilder result, String type, Collection<Diagram.Figure> children )
    {
        result.append( type ).append( '(' );
        String sep = " ";
        for ( Diagram.Figure item : children )
        {
            result.append( sep );
            item.toString( result );
            sep = ", ";
        }
        if ( !children.isEmpty() )
        {
            result.append( ' ' );
        }
        result.append( ')' );
    }

    /**
     * The rendering algorithm needs the size of each child figure for rendering a parent figure, and computing the
     * size of a figure depends on the size of its child figures. This makes the rendering algorithm quadratic due to
     * size computation. This class makes the algorithm linear by memoizing the computed size of the figure for the
     * current renderer.
     * <p>
     * Furthermore, rendering text can be expensive, and we need the text to be rendered twice - once for the size
     * computation, and once for rendering the figure. This class caches rendered text in order to only render each
     * text element once per renderer.
     */
    static final class Conditional<STATE>
    {
        interface Computation<OWNER, STATE>
        {
            <EX extends Exception> STATE compute( OWNER owner, Diagram.Renderer<?, ?, EX> renderer );
        }

        @SuppressWarnings("unchecked")
        private static final Conditional NONE = new Conditional( null, null );

        @SuppressWarnings("unchecked")
        static <STATE> Conditional<STATE> none()
        {
            return NONE;
        }

        private final STATE state;
        private final Diagram.Renderer renderer;

        private Conditional( STATE state, Diagram.Renderer renderer )
        {
            this.state = state;
            this.renderer = renderer;
        }

        <OWNER> Conditional<STATE> compute(
                OWNER owner, Diagram.Renderer<?, ?, ?> renderer,
                BiFunction<OWNER, Diagram.Renderer<?, ?, ?>, STATE> compute )
        {
            if ( this.renderer == renderer )
            {
                return this;
            }
            else
            {
                return new Conditional<>( compute.apply( owner, renderer ), renderer );
            }
        }

        @SuppressWarnings("unchecked")
        <X extends STATE> X get()
        {
            return (X) state;
        }
    }
}
