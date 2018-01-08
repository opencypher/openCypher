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
package org.opencypher.grammar;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;

@Element(uri = Grammar.XML_NAMESPACE, name = "except")
class Except
{
    @Attribute(optional = true)
    String literal;
    @Attribute(optional = true)
    Integer codePoint;
    @Attribute(optional = true)
    CharacterSet.Unicode set;

    CodePointSet set()
    {
        if ( (literal == null && codePoint == null && set == null) ||
             (literal != null && (codePoint != null || set != null))
             || (codePoint != null && set != null) )
        {
            throw new IllegalArgumentException(
                    "<except .../> must have a 'literal', 'codePoint' or 'set' attribute, but not more than one." );
        }
        if ( literal != null )
        {
            int cp;
            if ( literal.isEmpty() || Character.charCount( cp = literal.codePointAt( 0 ) ) != literal.length() )
            {
                throw new IllegalArgumentException( "'literal' exception must be a single character" );
            }
            return CodePointSet.single( cp );
        }
        else if ( codePoint != null )
        {
            return CodePointSet.single( codePoint );
        }
        else
        {
            return set.set;
        }
    }
}
