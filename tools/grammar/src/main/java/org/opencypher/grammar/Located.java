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
package org.opencypher.grammar;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opencypher.tools.xml.LocationAware;

class Located implements LocationAware
{
    private String path;
    private int lineNumber;
    private int columnNumber;

    @Override
    public final void location( String path, int lineNumber, int columnNumber )
    {
        this.path = path;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    static Comparator<Located> comparator( Map<String, ? extends Located> locations )
    {
        return ( lhs, rhs ) -> compare( locations, lhs, rhs );
    }

    private static int compare( Map<String, ? extends Located> locations, Located lhs, Located rhs )
    {
        if ( Objects.equals( lhs.path, rhs.path ) )
        {
            return compare( lhs, rhs );
        }
        return compare( trace( locations, lhs ).iterator(),
                        trace( locations, rhs ).iterator() );
    }

    private static int compare( Iterator<Located> left, Iterator<Located> right )
    {
        while ( left.hasNext() && right.hasNext() )
        {
            Located lhs = left.next(), rhs = right.next();
            if ( !Objects.equals( lhs.path, rhs.path ) )
            {
                break;
            }
            int pos = compare( lhs, rhs );
            if ( pos != 0 )
            {
                return pos;
            }
        }
//        throw new IllegalStateException( "Located objects should share a root, " +
//                                         "and be included from different positions where their trees diverge." );
        return 0;
    }

    private static List<Located> trace( Map<String, ? extends Located> locations, Located located )
    {
        LinkedList<Located> result = new LinkedList<>();
        do
        {
            result.addFirst( located );
        } while ( null != (located = locations.get( located.path )) );
        return result;
    }

    private static int compare( Located lhs, Located rhs )
    {
        int pos = lhs.lineNumber - rhs.lineNumber;
        if ( pos == 0 )
        {
            pos = lhs.columnNumber - rhs.columnNumber;
        }
        return pos;
    }
}
