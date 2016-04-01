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
package org.opencypher.tools.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Handle an XML attribute. A field or method annotated with this annotation will be used to handle attributes with the
 * given name from an XML element.
 *
 * @see org.opencypher.tools.xml The package documentation for usage and context.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute
{
    /**
     * The namespace of the attribute.
     *
     * If no namespace is given, the namespace of the {@link Element} annotation of the enclosing class is used.
     *
     * @return The namespace of this attribute.
     */
    String uri() default "";

    /**
     * The name of the attribute.
     *
     * If no name is given, the name of the annotated field or method is used.
     *
     * @return the name of this attribute.
     */
    String name() default "";

    /**
     * Whether this attribute is optional or mandatory. The default is for an atribute to be mandatory.
     *
     * If the {@linkplain #uri() namespace} of this attribute is not the same as the
     * {@linkplain Element#uri() namespace} of the enclosing class, the attribute <b>must</b> be specified as optional.
     *
     * @return {@code true} if this attribute is optional, {@code false} if this attribute is mandatory.
     */
    boolean optional() default false;
}
