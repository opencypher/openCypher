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
package org.opencypher.tools.tck

import org.opencypher.tools.tck.api.{ExecutionFailed, Graph, SideEffectQuery}
import org.opencypher.tools.tck.constants.TCKQueries._
import org.opencypher.tools.tck.constants.TCKSideEffects._
import org.opencypher.tools.tck.values.CypherValue


object SideEffectOps {

  case class Diff(v: Map[String, Int] = Map.empty) {
    override def toString: String = {
      val nonZeroSideEffects = ALL intersect v.keySet
      nonZeroSideEffects.toSeq
        .sortBy(str => (str.charAt(1), str.charAt(0)))
        .map { key =>
          s"${fill(key)}${v(key)}"
        }
        .mkString(System.lineSeparator())
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

  case class State(
      nodes: Set[CypherValue] = Set.empty,
      rels: Set[CypherValue] = Set.empty,
      labels: Set[CypherValue] = Set.empty,
      props: Seq[(CypherValue, CypherValue, CypherValue)] = Seq.empty) {

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

      Diff(
        Map(
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

  def measureState(graph: Graph): State = {
    val nodes = execToSet(graph, NODES_QUERY)
    val rels = execToSet(graph, RELS_QUERY)
    val labels = execToSet(graph, LABELS_QUERY)
    val nodeProps = graph.execute(NODE_PROPS_QUERY, Map.empty, SideEffectQuery)._2 match {
      case Left(error) =>
        throw MeasurementFailed(error)
      case Right(records) =>
        records.rows.map { row =>
          Tuple3(row("nodeId"), row("key"), row("value"))
        }
    }
    val relProps = graph.execute(REL_PROPS_QUERY, Map.empty, SideEffectQuery)._2 match {
      case Left(error) =>
        throw MeasurementFailed(error)
      case Right(records) =>
        records.rows.map { row =>
          Tuple3(row("relId"), row("key"), row("value"))
        }
    }

    State(nodes, rels, labels, nodeProps ++ relProps)
  }

  private def execToSet(graph: Graph, q: String): Set[CypherValue] =
    graph.execute(q, Map.empty, SideEffectQuery)._2 match {
      case Left(error) =>
        throw MeasurementFailed(error)
      case Right(records) =>
        records.rows.flatMap(_.values).toSet
    }
}

case class MeasurementFailed(failed: ExecutionFailed) extends Throwable {
  failed.exception.foreach(initCause)
}
