/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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

/**
 * Implemented by {@linkplain Element XML element classes} that need to know which file the element was read from, and
 * where in that file (line / column) the element was read.
 */
public interface LocationAware
{
    /**
     * Invoked with the location of this element.
     *
     * @param path         the path to the XML file this element was read from.
     * @param lineNumber   the line number in that XML file at which this element was read.
     * @param columnNumber the column number in the line at which this element was read.
     */
    void location( String path, int lineNumber, int columnNumber );
}
