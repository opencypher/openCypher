/*
 * Copyright (c) 2015-2017 "Neo Technology,"
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
package org.opencypher.tools.tck

import org.opencypher.tools.tck.api.{Graph, SideEffectQuery}
import org.opencypher.tools.tck.constants.TCKSideEffects._
import org.opencypher.tools.tck.values.CypherValue

import scala.compat.Platform

object SideEffectOps {

  case class Diff(v: Map[String, Int] = Map.empty) {
    override def toString: String = {
      val nonZeroSideEffects = ALL intersect v.keySet
      nonZeroSideEffects.toSeq.sortBy(_.charAt(1)).map { key =>
        s"${fill(key)}${v(key)}"
      }.mkString(Platform.EOL)
    }

    private def fill(s: String) = (s + ":                 ").take(16)

    def fillInZeros: Diff = {
      val setToZero = ALL -- v.keySet
      val withZeros = setToZero.foldLeft(v) {
        case (m, s) => m.updated(s, 0)
      }
      copy(withZeros)
    }
  }

  case class State(nodes: Set[CypherValue] = Set.empty,
                   rels: Set[CypherValue] = Set.empty,
                   labels: Set[CypherValue] = Set.empty,
                   props: Set[(CypherValue, CypherValue, CypherValue)] = Set.empty) {

    /**
      * Computes the difference in between this state and a later state (the argument).
      * The difference is a set of side effects in the form of a Values instance.
      *
      * @param later the later state to compare against.
      * @return the side effect difference, as a Values instance.
      */
    def diff(later: State): Diff = {
      val nodesCreated = (later.nodes diff nodes).size
      val nodesDeleted = (nodes diff later.nodes).size
      val relsCreated = (later.rels diff rels).size
      val relsDeleted = (rels diff later.rels).size
      val labelsCreated = (later.labels diff labels).size
      val labelsDeleted = (labels diff later.labels).size
      val propsCreated = (later.props diff props).size
      val propsDeleted = (props diff later.props).size

      Diff(Map(
        ADDED_NODES -> nodesCreated,
        DELETED_NODES -> nodesDeleted,
        ADDED_RELATIONSHIPS -> relsCreated,
        DELETED_RELATIONSHIPS -> relsDeleted,
        ADDED_LABELS -> labelsCreated,
        DELETED_LABELS -> labelsDeleted,
        ADDED_PROPERTIES -> propsCreated,
        DELETED_PROPERTIES -> propsDeleted
      ))
    }
  }

  private val nodesQuery =
    s"""MATCH (n) RETURN id(n)"""

  private val relsQuery =
    s"""MATCH ()-[r]->() RETURN id(r)"""

  private val labelsQuery =
    s"""MATCH (n)
       |UNWIND labels(n) AS label
       |RETURN DISTINCT label""".stripMargin

  private val propsQuery =
    s"""MATCH (n)
       |UNWIND keys(n) AS key
       |WITH properties(n) AS properties, key, n
       |RETURN id(n) AS entityId, key, properties[key] AS value
       |UNION ALL
       |MATCH ()-[r]->()
       |UNWIND keys(r) AS key
       |WITH properties(r) AS properties, key, r
       |RETURN id(r) AS entityId, key, properties[key] AS value""".stripMargin

  def measureState(graph: Graph): State = {
    val nodes = execToSet(graph, nodesQuery)
    val rels = execToSet(graph, relsQuery)
    val labels = execToSet(graph, labelsQuery)
    val props = graph.execute(propsQuery, Map.empty, SideEffectQuery)._2.asRecords.rows.map { row =>
      Tuple3(row("entityId"), row("key"), row("value"))
    }.toSet

    State(nodes, rels, labels, props)
  }

  private def execToSet(graph: Graph, q: String): Set[CypherValue] =
    graph.execute(q, Map.empty, SideEffectQuery)._2.asRecords.rows.flatMap(_.values).toSet
}
