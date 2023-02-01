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
/**
 * This package contains utilities that read XML files and build up a custom object graph by mapping the XML structure
 * to annotated classes.
 *
 * <p>
 * In order to parse XML into you object graph, you need to create an {@link org.opencypher.tools.xml.XmlParser} for
 * your root object type:
 * <pre><code>XmlParser&lt;MyRoot&gt; PARSER = XmlParser.xmlParser( MyRoot.class );</code></pre>
 * Your class {@code MyRoot} and the child node types should be annotated with
 * {@link org.opencypher.tools.xml.Element @Element} and have fields annotated with
 * {@link org.opencypher.tools.xml.Attribute @Attribute} to map the attributes of the xml element, and methods
 * annotated with {@link org.opencypher.tools.xml.Child @Child} to map the child elements of the xml element.
 *
 * <p>
 * Accepted types for fields annotated with {@link org.opencypher.tools.xml.Attribute @Attribute} are:
 * <ul>
 * <li>{@link java.lang.String}</li>
 * <li>{@code int} or {@link java.lang.Integer}</li>
 * <li>{@code boolean} or {@link java.lang.Boolean}</li>
 * <li>{@code long} or {@link java.lang.Long}</li>
 * <li>{@code double} or {@link java.lang.Double}</li>
 * <li>any {@code enum} type</li>
 * <li>{@link org.opencypher.tools.xml.XmlFile}m - in order to reference another XML file (typically for
 * inclusion).</li>
 * </ul>
 * (See the source code of {@link org.opencypher.tools.xml.AttributeHandler} for details)
 * <p>
 * It is also possible to use the {@link org.opencypher.tools.xml.Attribute @Attribute} annotation on a method that
 * accepts a single argument (and returns void). The accepted argument types are the same as for fields annotated with
 * {@link org.opencypher.tools.xml.Attribute @Attribute}.
 *
 * <p>
 * Methods annotated with {@link org.opencypher.tools.xml.Child @Child} may have any name, should return {@code void}
 * and have one of the following parameters lists:
 * <ul>
 * <li>A single parameter, where the type of that parameter is annotated with
 * {@link org.opencypher.tools.xml.Element @Element}<br><b>Example:</b>
 * <pre><code>
 * {@literal @}Child
 * void add( SomeChildType child ) {
 *     // do what you want with the child object here
 * }
 *
 * {@literal @}Element(uri=YOUR_NAMESPACE_URI, name="some-child")
 * class SomeChildType {
 * }
 * </code></pre></li>
 * <li>A single parameter, that is a supertype of all of the types supplied as argument to the
 * {@link org.opencypher.tools.xml.Child @Child} annotation. Each of the types in the arguments list of the
 * {@link org.opencypher.tools.xml.Child @Child} annotation should be annotated with
 * {@link org.opencypher.tools.xml.Element @Element}, but the actual parameter type of the method does not need that
 * annotation.<br><b>Example:</b>
 * <pre><code>
 * {@literal @}Child(ActualChildOne.class, ActualChildTwo.class)
 * void add( SomeChildInterface child ) {
 *     // do what you want with the child object here
 * }
 *
 * interface SomeChildInterface {
 * }
 * {@literal @}Element(uri=YOUR_NAMESPACE_URI, name="one")
 * class ActualChildOne implements SomeChildInterface {
 * }
 * {@literal @}Element(uri=YOUR_NAMESPACE_URI, name="two")
 * class ActualChildTwo implements SomeChildInterface {
 * }
 * </code></pre></li>
 * <li>A single {@link java.lang.String} parameter. This handles text within the XML element.
 * Alternatively, if the {@link org.opencypher.tools.xml.Child @Child} annotation is given an argument of
 * {@link org.opencypher.tools.xml.Comment Comment.class} the method handles XML comments within the XML element
 * instead. For the root element type, it is also possible to give the {@link org.opencypher.tools.xml.Child @Child}
 * annotation an argument of {@link org.opencypher.tools.xml.Comment.Header Comment.Header.class}, which makes the
 * method handle XML comments from before the start of the first XML element.<br><b>Examples:</b>
 * <pre><code>
 * {@literal @}Child
 * void text( String text ) {
 *     // handle embedded text
 * }
 * {@literal @}Child( Comment.class )
 * void comment( String comment ) {
 *     // handle embedded comments
 * }
 * {@literal @}Child( Comment.Header.class )
 * void header( String headerComment ) {
 *     // handle embedded the header comment(s)
 * }
 * </code></pre></li>
 * <li>Three parameters: {@code char[] text, int start, int length}. This works in the same way as if the method had a
 * single {@link java.lang.String} parameter, but uses the raw {@code char[]} from the parser, with a start offset and
 * a length of the string found at that offset and does not have to instantiate a new string. This might be preferable
 * if further parsing is to be made on the characters in the embedded text. As with {@link java.lang.String}-methods,
 * {@link org.opencypher.tools.xml.Comment Comment.class} and
 * {@link org.opencypher.tools.xml.Comment.Header Comment.Header.class} can be given as arguments to the
 * {@link org.opencypher.tools.xml.Child @Child} annotation in order to handle embedded comments or the header
 * comment(s) of the file instead of embedded text.<br><b>Examples:</b>
 * <pre><code>
 * {@literal @}Child
 * void text( char[] text, int start, int length ) {
 *     // handle embedded text
 * }
 * {@literal @}Child( Comment.class )
 * void comment( char[] comment, int start, int length ) {
 *     // handle embedded comments
 * }
 * {@literal @}Child( Comment.Header.class )
 * void header( char[] headerComment, int start, int length ) {
 *     // handle embedded the header comment(s)
 * }
 * </code></pre></li>
 * </ul>
 *
 * Once a {@linkplain org.opencypher.tools.xml.XmlParser parser} has been created, parsing an XML file to generate an
 * object graph is as simple as invoking one of the {@code parse}-methods:
 * <ul>
 * <li>{@link org.opencypher.tools.xml.XmlParser#parse(java.nio.file.Path, org.opencypher.tools.xml.XmlParser.Option...)}</li>
 * <li>{@link org.opencypher.tools.xml.XmlParser#parse(java.io.Reader, org.opencypher.tools.xml.XmlParser.Option...)}</li>
 * <li>{@link org.opencypher.tools.xml.XmlParser#parse(java.io.InputStream, org.opencypher.tools.xml.XmlParser.Option...)}</li>
 * </ul>
 *
 * There are also some more advanced features of the XML-to-object mapping available, such as:
 * <ul>
 * <li>Getting information about which XML file, and where in that file, an element was parsed from. This is done by
 * having the class (that is annotated with {@link org.opencypher.tools.xml.Element @Element}) implement the
 * {@link org.opencypher.tools.xml.LocationAware} interface.</li>
 * <li>Accepting (optional) attributes from other XML namespaces. This is done by specifying the
 * {@linkplain org.opencypher.tools.xml.Attribute#uri() <code>uri</code>-parameter of the <code>@Attribute</code>
 * annotation}.</li>
 * </ul>
 *
 * In terms of implementation, mapping the object class structure into a parser that can be used to parse an XML file
 * is handled by {@link org.opencypher.tools.xml.Structure}. This class is responsible for creating
 * {@link org.opencypher.tools.xml.NodeBuilder} objects that knows how to instantiate and build objects for the various
 * XML entities that the parser encounters.
 *
 * The actual parsing, and keeping track of which {@link org.opencypher.tools.xml.NodeBuilder} maps to what part of the
 * XML file is handled by {@link org.opencypher.tools.xml.ParserStateMachine}.
 *
 * Converting XML attributes into values that can be assigned to fields, or passed into methods annotated with
 * {@link org.opencypher.tools.xml.Attribute @Attribute} is handled by
 * {@link org.opencypher.tools.xml.AttributeHandler}, which also handles the actual field assignment or invocation.
 *
 * For creating {@link org.opencypher.tools.xml.XmlFile} attribute values, a {@link org.opencypher.tools.xml.Resolver}
 * instance is used for finding the xml file in question.
 */
package org.opencypher.tools.xml;
