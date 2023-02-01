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

import java.util.Objects;
import java.util.function.Consumer;

import org.opencypher.tools.io.Output;

import static java.util.Objects.requireNonNull;

import static org.opencypher.tools.io.Output.stringBuilder;

public abstract class Node
{
    static Tree root( String language )
    {
        return new Tree( null, requireNonNull( language, "language" ) );
    }

    private final Node parent;
    private final String name;
    private Node next;

    private Node( Node parent, String name )
    {
        this.parent = parent;
        this.name = name;
    }

    public final Node parent()
    {
        return parent;
    }

    public void write( Output output )
    {
        for ( Node child = children(); child != null; child = child.next )
        {
            child.write( output );
        }
    }

    @Override
    public final int hashCode()
    {
        int hash = Objects.hashCode( name );
        int detail = hash();
        if ( detail != 0 )
        {
            hash = hash * 31 + detail;
        }
        for ( Node child = children(); child != null; child = child.next )
        {
            hash = hash * 31 + child.hashCode();
        }
        return hash;
    }

    int hash()
    {
        return 0;
    }

    @Override
    public final boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() || !eq( (Node) obj ) )
        {
            return false;
        }
        Node these = children(), those = ((Node) obj).children();
        for (; these != null && those != null; these = these.next, those = those.next )
        {
            if ( !these.equals( those ) )
            {
                return false;
            }
        }
        return these == those;
    }

    boolean eq( Node obj )
    {
        return true;
    }

    @Override
    public final String toString()
    {
        Output result = stringBuilder();
        toString( result );
        return result.toString();
    }

    public final void sExpression( Output output )
    {
        sExpression( output, 0 );
    }

    void toString( Output result )
    {
        result.append( getClass().getSimpleName() ).append( '{' ).append( name() );
        String sep = ": ";
        for ( Node child = children(); child != null; child = child.next )
        {
            result.append( sep );
            child.toString( result );
            sep = ", ";
        }
        result.append( '}' );
    }

    void sExpression( Output output, int indent )
    {
        indent += 2;
        output.append( '(' ).append( name() );
        Node child = children();
        if ( child != null && child.next == null )
        {
            output.append( ' ' );
            child.sExpression( output, indent );
        }
        else
        {
            for (; child != null; child = child.next )
            {
                output.println();
                for ( int space = 0; space < indent; space++ )
                {
                    output.append( ' ' );
                }
                child.sExpression( output, indent );
            }
        }
        output.append( ')' );
    }

    Node append( String literal )
    {
        return append( new Literal( parent() ) ).append( literal );
    }

    Node appendCodePoint( int cp )
    {
        return append( new Literal( parent() ) ).appendCodePoint( cp );
    }

    Node children()
    {
        return null;
    }

    public final String name()
    {
        return name;
    }

    final Node append( Node node )
    {
        assert next == null : "appending to the middle of the chain";
        next = node;
        return node;
    }

    static class Tree extends Node
    {
        private Node first, last;

        private Tree( Node parent, String name )
        {
            super( parent, name );
        }

        @Override
        Node children()
        {
            return first;
        }

        public Tree child( String name )
        {
            return add( new Tree( this, requireNonNull( name, "name" ) ) );
        }

        public void literal( CharSequence literal )
        {
            if ( last == null )
            {
                first = last = new Literal( this );
            }
            last = last.append( literal.toString() );
        }

        public void codePoint( int cp )
        {
            if ( last == null )
            {
                first = last = new Literal( this );
            }
            last = last.appendCodePoint( cp );
        }

        public <T> void production(
                String name, ProductionReplacement<T> replacement, T context, Consumer<Tree> defaultValue )
        {
            add( new Replacement<>( this, name, replacement, context, defaultValue ) );
        }

        private <T extends Node> T add( T child )
        {
            if ( last == null )
            {
                first = last = child;
            }
            else
            {
                last = last.append( child );
            }
            return child;
        }
    }

    private static class Literal extends Node
    {
        private final StringBuilder buffer = new StringBuilder();

        private Literal( Node parent )
        {
            super( parent, null );
        }

        @Override
        int hash()
        {
            int hash = 0;
            for ( int i = buffer.length(); i-- > 0; )
            {
                hash = hash * 31 + buffer.charAt( i );
            }
            return hash;
        }

        @Override
        boolean eq( Node obj )
        {
            Literal that = (Literal) obj;
            int len = buffer.length();
            if ( that.buffer.length() != len )
            {
                return false;
            }
            for ( int i = 0; i < len; i++ )
            {
                if ( this.buffer.charAt( i ) != that.buffer.charAt( i ) )
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        Node append( String literal )
        {
            buffer.append( literal );
            return this;
        }

        @Override
        Node appendCodePoint( int cp )
        {
            buffer.appendCodePoint( cp );
            return this;
        }

        @Override
        public void write( Output output )
        {
            output.append( buffer );
        }

        @Override
        void toString( Output result )
        {
            result.append( '\'' )
                  .append( buffer.toString().replace( "\r", "\\r" ).replace( "\n", "\\n" ).replace( "\t", "\\t" ) )
                  .append( '\'' );
        }

        @Override
        void sExpression( Output output, int indent )
        {
            toString( output );
        }
    }

    private static class Replacement<T> extends Node
    {
        private final ProductionReplacement<T> replacement;
        private final T context;
        private final Consumer<Tree> defaults;

        private Replacement(
                Node parent, String name, ProductionReplacement<T> replacement, T context, Consumer<Tree> defaults )
        {
            super( parent, name );
            this.replacement = replacement;
            this.context = context;
            this.defaults = defaults;
        }

        @Override
        public void write( Output output )
        {
            replacement.replace( new ReplacementContext<T>( this, output ) );
        }

        @Override
        int hash()
        {
            return replacement.hashCode() * 31 + Objects.hashCode( context );
        }

        @Override
        boolean eq( Node obj )
        {
            Replacement that = (Replacement) obj;
            return Objects.equals( this.replacement, that.replacement ) &&
                   Objects.equals( this.context, that.context );
        }
    }

    private static class ReplacementContext<T> implements ProductionReplacement.Context<T>
    {
        private final Replacement<T> replacement;
        private final Output output;

        private ReplacementContext( Replacement<T> replacement, Output output )
        {
            this.replacement = replacement;
            this.output = output;
        }

        @Override
        public Node node()
        {
            return replacement;
        }

        @Override
        public void generateDefault()
        {
            Tree tree = new Tree( replacement.parent(), replacement.name() );
            replacement.defaults.accept( tree );
            tree.write( output );
        }

        @Override
        public T context()
        {
            return replacement.context;
        }

        @Override
        public void write( CharSequence str )
        {
            output.append( str );
        }

        @Override
        public void write( int codePoint )
        {
            output.appendCodePoint( codePoint );
        }

        @Override
        public Output output()
        {
            return output;
        }
    }
}
