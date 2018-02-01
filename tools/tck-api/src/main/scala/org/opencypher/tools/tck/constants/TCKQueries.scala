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
package org.opencypher.tools.tck.constants

object TCKQueries {

  val NODES_QUERY =
    s"""MATCH (n) RETURN id(n)"""

  val RELS_QUERY =
    s"""MATCH ()-[r]->() RETURN id(r)"""

  val LABELS_QUERY =
    s"""MATCH (n)
       |UNWIND labels(n) AS label
       |RETURN DISTINCT label""".stripMargin

  val NODE_PROPS_QUERY =
    s"""MATCH (n)
       |UNWIND keys(n) AS key
       |WITH properties(n) AS properties, key, n
       |RETURN id(n) AS nodeId, key, properties[key] AS value""".stripMargin

  val REL_PROPS_QUERY =
    """MATCH ()-[r]->()
      |UNWIND keys(r) AS key
      |WITH properties(r) AS properties, key, r
      |RETURN id(r) AS relId, key, properties[key] AS value""".stripMargin

}
