/*
 * Copyright (c) 2015-2020 "Neo Technology,"
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
package org.opencypher.tools.io;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.opencypher.tools.Reflection.lambdaParameterName;

/**
 * Tiny DSL for generating HTML.
 *
 * Usage:
 * <pre><code>
 * try ( {@link Html} html = HtmlTag.{@link #html(Path) html}( {@link Path outputPath} ) ) {
 *     html.{@link HtmlTag.Html#head head}( title -&gt; "My Page" );
 *     try ( {@link HtmlTag} body = html.{@link Html#body() body}() ) {
 *         body.{@link #tag(String, Attribute[]) tag}("h1")                  // opens a new &lt;h1&gt; tag
 *             .{@link #text(String) text}("Welcome to My Page") // adds text content to the tag
 *             .{@link #close() close}();                   // closes the &lt;/h1&gt; tag
 *         body.{@link #text(String) text}("This is a very neat page.");
 *         body.{@link #p() p}();
 *         body.{@link #text(String) text}("You should come back when there is more content.");
 *         body.{@link #br() br}();
 *         body.{@link #text(String) text}("Until then, here is a picture of a cat for you to look at:");
 *         body.{@link #tag(String, Attribute[]) tag}("img", src -&gt; "http://thecatapi.com/api/images/get?format=src&amp;type=gif");
 *         body.{@link #p() p}();
 *         body.{@link #tag(String, Attribute[]) tag}("b").{@link #text(String) text}("To do:").{@link #close()
 * close}();
 *         try ( {@link HtmlTag} list = body.{@link #tag(String, Attribute[]) tag}("ul") ) {
 *             list.{@link #tag(String, Attribute[]) tag}("li").{@link #text(String) text}("Find cuter cat").{@link
 * #close() close}();
 *             list.{@link #tag(String, Attribute[]) tag}("li").{@link #text(String) text}("???").{@link #close()
 * close}();
 *             list.{@link #tag(String, Attribute[]) tag}("li").{@link #text(String) text}("Profit!").{@link #close()
 * close}();
 *         }
 *     }
 * }
 * </code></pre>
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

    /**
     * Generate html tag attributes for use in {@link}.
     *
     * This is an alternative to {@code body.tag( "img", src -> imgUri )}, allowing the use of the API on earlier
     * builds of the JDK as {@code body.tag( "img", attr( "src", imgUri ) )}.
     *
     * @param attribute the name of the attribute.
     * @param value     the value of the attribute.
     * @return an object that generates the attribute.
     */
    public static Attribute<Void> attr( String attribute, String value )
    {
        return new Attribute<Void>()
        {
            @Override
            public String name()
            {
                return attribute;
            }

            @Override
            public String value( Void target )
            {
                return value;
            }
        };
    }

    /**
     * Generate meta tags for use in {@link HtmlTag.Html#head &lt;head&gt;}.
     *
     * @param name  the name of the meta attribute.
     * @param value the value of the meta attribute.
     * @return an object that generates the tag in the head.
     */
    public static Attribute<HtmlTag> meta( String name, String value )
    {
        return head( "meta", ignored ->
        {
        }, new Attribute<Void>()
        {
            @Override
            public String name()
            {
                return name;
            }

            @Override
            public String value( Void target )
            {
                return value;
            }
        } );
    }

    /**
     * Generate html tags for use in {@link HtmlTag.Html#head &lt;head&gt;}.
     *
     * Allows adding attributes to a head tag.
     *
     * @param tag        the name and contents of the head tag.
     * @param attributes the attributes of the head tag.
     * @return an object that generates the tag in the head.
     */
    public static Attribute<HtmlTag> head( Attribute<HtmlTag> tag, Attribute<Void>... attributes )
    {
        return head( tag.name(), tag::value, attributes );
    }

    /**
     * Generate html tags for use in {@link HtmlTag.Html#head &lt;head&gt;}.
     *
     * This is an alternative to {@code html.head( title -> "my title" )}, allowing the use of the API on earlier
     * builds of the JDK as {@code html.head( head( "title", title -> title.text( "my title" ) ) )}.
     *
     * @param tag        the name of the head tag.
     * @param content    generator for the content of the head tag.
     * @param attributes the attributes of the head tag.
     * @return an object that generates the tag in the head.
     */
    @SafeVarargs
    public static Attribute<HtmlTag> head( String tag, Consumer<HtmlTag> content, Attribute<Void>... attributes )
    {
        return new HeadTag( tag, content, attributes );
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
                    try ( HtmlTag headTag = head.tag( tag.name(), HeadTag.attributes( tag ) ) )
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

    /**
     * Unsafe access to the underlying {@link Output}.
     *
     * This provides direct access to the underlying {@link Output} and should be used carefully, since its use might
     * result in invalid HTML.
     *
     * @return the underlying {@link Output}
     */
    public Output output()
    {
        return output;
    }

    private static class HeadTag implements Attribute<HtmlTag>
    {
        @SuppressWarnings( "unchecked" )
        private static final Attribute<Void>[] NO_ATTRIBUTES = new Attribute[0];

        static Attribute<Void>[] attributes( Attribute<HtmlTag> tag )
        {
            return tag instanceof HeadTag ? ((HeadTag) tag).attributes : NO_ATTRIBUTES;
        }

        private final String tag;
        private final Consumer<HtmlTag> content;
        private final Attribute<Void>[] attributes;

        HeadTag( String tag, Consumer<HtmlTag> content, Attribute<Void>[] attributes )
        {
            this.tag = tag;
            this.content = content;
            this.attributes = attributes;
        }

        @Override
        public String name()
        {
            return tag;
        }

        @Override
        public String value( HtmlTag target )
        {
            content.accept( target );
            return null;
        }
    }
}
