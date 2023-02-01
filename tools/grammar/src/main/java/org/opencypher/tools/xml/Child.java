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
package org.opencypher.tools.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Handle a nested XML element. A method annotated with this annotation will be used to handle XML elements nested
 * within the XML element that corresponds to the instance of the class that defines the method.
 *
 * @see org.opencypher.tools.xml The package documentation for usage and context.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Child
{
    /**
     * The types of nested elements handled by the annotated method.
     *
     * The default value corresponds to the parameter type of the annotated method.
     *
     * Valid values must be subtypes of the parameter type of the annotated method, and annotated with {@link Element},
     * or one of {@link String String.class}, {@link Comment Comment.class}, or
     * {@link Comment.Header Comment.Header.class}.
     *
     * @return The specific types that are accepted as child elements of the element that corresponds to the class with
     * the annotated method.
     */
    Class<?>[] value() default {};
}
