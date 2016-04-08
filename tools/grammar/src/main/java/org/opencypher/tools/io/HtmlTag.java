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
package org.opencypher.tools.io;

import java.io.Serializable;
import java.nio.file.Path;

import static org.opencypher.tools.Reflection.lambdaParameterName;

/**
 * Tiny DSL for generating HTML.
 */
public final class HtmlTag implements AutoCloseable
{
    public static Html html( Path file )
    {
        return html( Output.output( file ) );
    }

    public static Html html( Output output )
    {
        return new Html( output );
    }

    @FunctionalInterface
    public interface Attribute<T> extends Serializable
    {
        String value( T target );

        default String name()
        {
            return lambdaParameterName( this );
        }
    }

    @SafeVarargs
    public final HtmlTag tag( String tag, Attribute<Void>... attributes )
    {
        output.append( '<' ).append( tag );
        for ( Attribute<Void> attribute : attributes )
        {
            String value = attribute.value( null );
            if ( value != null )
            {
                output.append( ' ' ).append( attribute.name() )
                      .append( "=\"" ).append( value ).append( '"' );
            }
        }
        output.append( '>' );
        return new HtmlTag( output, tag );
    }

    public HtmlTag text( String text )
    {
        output.escape( text, c -> c == '<' ? "&lt;" : null );
        return this;
    }

    public void p()
    {
        output.println( "<p>" );
    }

    public void br()
    {
        output.append( "<br>" );
    }

    private final Output output;
    private final String tag;

    private HtmlTag( Output output, String tag )
    {
        this.output = output;
        this.tag = tag;
    }

    public static final class Html implements AutoCloseable
    {
        @SafeVarargs
        public final void head( Attribute<HtmlTag>... tags )
        {
            state = state.head();
            try ( HtmlTag head = html.tag( "head" ) )
            {
                for ( Attribute<HtmlTag> tag : tags )
                {
                    try ( HtmlTag headTag = head.tag( tag.name() ) )
                    {
                        String text = tag.value( headTag );
                        if ( text != null )
                        {
                            headTag.text( text );
                        }
                    }
                }
            }
        }

        public HtmlTag body()
        {
            state = state.body();
            return html.tag( "body" );
        }

        private State state = State.EMIT_HEAD;
        private final HtmlTag html;

        private Html( Output output )
        {
            output.append( "<html>" );
            this.html = new HtmlTag( output, "html" );
        }

        @Override
        public void close()
        {
            state = state.close();
            html.close();
            html.output.close();
        }

        private enum State
        { // <pre>
            EMIT_HEAD
            {
                @Override State head()  { return EMIT_BODY; }
                @Override State body()  { return SHOULD_CLOSE; }
                @Override State close() { return CLOSED; }
            },
            EMIT_BODY
            {
                @Override State head()  { return illegal( "<head> has already been emitted." ); }
                @Override State body()  { return SHOULD_CLOSE; }
                @Override State close() { return CLOSED; }
            },
            SHOULD_CLOSE
            {
                @Override State head()  { return illegal( "<head> and <body> have already been emitted." ); }
                @Override State body()  { return illegal( "<body> has already been emitted." ); }
                @Override State close() { return CLOSED; }
            },
            CLOSED
            {
                @Override State head()  { return illegal( "Already closed" ); }
                @Override State body()  { return illegal( "Already closed" ); }
                @Override State close() { return illegal( "Already closed" ); }
            };
            abstract State head();
            abstract State body();
            abstract State close();
            State illegal( String message ) { throw new IllegalStateException( message ); }
        } //</pre>
    }

    @Override
    public void close()
    {
        output.format( "</%s>", tag );
    }
}
