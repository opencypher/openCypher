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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    static Diagram.Figure reference( String target, String text )
    {
        return new Reference( target, text );
    }

    static Diagram.Figure charset( String set )
    {
        return new Charset( set, set );
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

    private final boolean expandAnyCase, skipNone, inlineNone, optimizeDiagram;

    private FigureBuilder( Diagram.BuilderOptions options )
    {
        this.expandAnyCase = options.expandAnyCase();
        this.skipNone = options.skipNone();
        this.inlineNone = options.inlineNone();
        this.optimizeDiagram = options.optimizeDiagram();
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
                group.add( lineFigures( line ) );
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
            if ( Character.isLetterOrDigit( cp ) || (cp >= 0x20 && cp < 0x7F) )
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
            group.add( lineFigures( line ) );
        }
    }

    @Override
    public Void transformNonTerminal( Group group, NonTerminal nonTerminal )
    {
        if ( skipNone || !nonTerminal.skip() )
        {
            if ( !inlineNone && nonTerminal.inline() )
            {
                nonTerminal.productionDefinition().transform( this, group );
            }
            else
            {
                group.add( reference( nonTerminal.productionName(), nonTerminal.title() ) );
            }
        }
        return null;
    }

    @Override
    public Void transformOptional( Group group, Optional optional )
    {
        Branch branch = new Branch();
        branch.add( NOTHING );
        optional.term().transform( this, branch );
        if ( !branch.containsNothing() )
        {
            group.add( branch );
        }
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
        String name = characters.name();
        String set = CharacterSet.Unicode.toSetString( characters );
        group.add( new Charset( name == null ? set : name, set ) );
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
                    add( builder.combineAlternatives( branch ) );
                }
            }
        }

        void line( FigureBuilder builder, Sequence sequence )
        {
            Line line = new Line();
            builder.addAll( sequence, line );
            if ( !line.seq.isEmpty() )
            {
                add( lineFigures( line ) );
            }
        }

        void repetition( FigureBuilder builder, Line repeated, int minTimes, Integer maxTimes )
        {
            Diagram.Figure forwards, backwards;
            if ( minTimes == 0 )
            {
                forwards = NOTHING;
                backwards = lineFigures( repeated );
            }
            else
            {
                forwards = lineFigures( repeated );
                backwards = NOTHING;
                minTimes -= 1;
                if ( maxTimes != null )
                {
                    maxTimes -= 1;
                }
            }
            add( builder.loop( forwards, backwards, minTimes, maxTimes, false ) );
        }

        abstract void add( Diagram.Figure child );
    }

    private Diagram.Figure combineAlternatives( Branch branch )
    {
        if ( !optimizeDiagram )
        {
            return branch;
        }
        List<Line> lines = lines( branch.alt );
        Line one = lines.remove( lines.size() - 1 ); // pick one as reference (last one is easiest to put back in order)
        int prefix = 0, suffix = 0;
        int oneSize = one.seq.size();
        for ( boolean pre = true, post = true; pre || post; )
        {
            for ( Line line : lines )
            {
                int lineSize = line.seq.size();
                if ( pre && !(lineSize <= prefix || oneSize <= prefix ||
                              line.seq.get( prefix ).equals( one.seq.get( prefix ) )) )
                {
                    pre = false;
                }
                if ( post && (lineSize <= suffix || oneSize <= suffix ||
                              !line.seq.get( lineSize - suffix - 1 ).equals(
                                      one.seq.get( oneSize - suffix - 1 ) )) )
                {
                    post = false;
                }
                if ( prefix + suffix >= Math.min( oneSize, lineSize ) )
                {
                    if ( prefix + suffix > Math.min( oneSize, lineSize ) )
                    {
                        suffix--;
                    }
                    pre = post = false;
                }
            }
            if ( pre )
            {
                prefix++;
            }
            if ( post )
            {
                suffix++;
            }
        }
        if ( prefix > 0 || suffix > 0 )
        {
            Line common = new Line();
            List<Diagram.Figure> after = new ArrayList<>();
            for ( int i = 0; i < prefix; i++ )
            {
                common.add( one.seq.get( i ) );
            }
            for ( int i = suffix; i > 0; i-- )
            {
                after.add( one.seq.get( one.seq.size() - i ) );
            }
            branch = new Branch();
            lines.add( one ); // put back the last line and remove the shared prefix & suffix from all lines
            for ( Line line : lines )
            {
                for ( int i = 0; i < suffix; i++ )
                {
                    line.seq.remove( line.seq.size() - 1 );
                }
                for ( int i = 0; i < prefix; i++ )
                {
                    line.seq.remove( 0 );
                }
                branch.add( lineFigures( line ) );
            }
            common.add( branch );
            for ( Diagram.Figure figure : after )
            {
                common.add( figure );
            }
            return common;
        }
        return branch;
    }

    private static List<Line> lines( Collection<Diagram.Figure> figures )
    {
        List<Line> lines = new ArrayList<>( figures.size() );
        for ( Diagram.Figure figure : figures )
        {
            if ( figure instanceof Line )
            {
                lines.add( (Line) figure );
            }
            else
            {
                Line line = new Line();
                line.add( figure );
                lines.add( line );
            }
        }
        return lines;
    }

    private static Diagram.Figure lineFigures( Line line )
    {
        switch ( line.seq.size() )
        {
        case 0:
            return NOTHING;
        case 1:
            return line.seq.get( 0 );
        default:
            return line;
        }
    }

    private Diagram.Figure loop( Diagram.Figure forwards, Diagram.Figure backwards, int min, Integer max, boolean top )
    {
        if ( optimizeDiagram && forwards instanceof Loop )
        {
            Loop loop = (Loop) forwards;
            forwards = loop.forward;
            Branch branch = new Branch();
            branch.add( loop.backward );
            branch.add( backwards );
            backwards = branch;
        }
        if ( forwards == NOTHING && backwards instanceof Branch )
        {
            // prefer forward directions
            Branch branch = new Branch();
            branch.add( NOTHING );
            branch.add( new Loop( backwards, forwards, min, max, top ) );
            return branch;
        }
        if ( min == 1 && max == null )
        {
            Line line = new Line();
            line.add( forwards );
            line.add( new Loop( backwards, forwards, 0, null, top ) );
            return line;
        }
        return new Loop( forwards, backwards, min, max, top );
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
        private final String target;

        Reference( String target, String title )
        {
            super( title );
            this.target = target;
        }

        @Override
        <T> Size computeSize( Diagram.Renderer<?, T, ?> renderer, T name )
        {
            return renderer.sizeOfReference( name );
        }

        @Override
        <O, T, EX extends Exception> void render(
                O target, double x, double y, Diagram.Renderer<O, T, EX> renderer, T text ) throws EX
        {
            renderer.renderReference( target, x, y, this.target, text );
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
        private final String set;

        Charset( String name, String set )
        {
            super( name );
            this.set = set;
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
            renderer.renderCharset( target, x, y, text, set );
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
            if ( child instanceof Line )
            {
                Line line = (Line) child;
                seq.addAll( line.seq );
            }
            else if ( child != NOTHING )
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
            backward = lineFigures( repeated );
            add( builder.loop( forward, backward, minTimes, maxTimes, false ) );
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
            if ( child instanceof Branch )
            {
                Branch branch = (Branch) child;
                alt.addAll( branch.alt );
            }
            else
            {
                alt.add( child );
            }
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

        boolean containsNothing()
        {
            if ( alt.isEmpty() )
            {
                return true;
            }
            for ( Diagram.Figure f : alt )
            {
                if ( !f.isNothing() )
                {
                    return false;
                }
            }
            return true;
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
                text.append( min );
                if ( max == null )
                {
                    text.append( "..N" );
                }
                else if ( min != max )
                {
                    text.append( ".." ).append( max.intValue() );
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
