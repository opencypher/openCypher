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
package org.opencypher.tools.tck.inspection.browser.web

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total
import org.opencypher.tools.tck.inspection.diff.TckTreeDiff

import scala.collection.compat._
import scala.util.matching.Regex

sealed trait TckCollection

object BeforeCollection extends TckCollection {
  override def toString: String = "Before"
}
object AfterCollection extends TckCollection {
  override def toString: String = "After"
}
object BothCollections extends TckCollection {
  override def toString: String = "Both"
}

case class DiffModel(beforePath: String, afterPath: String) {

  private val scenariosBefore = CypherTCK.allTckScenariosFromFilesystem(beforePath)

  private val scenariosAfter = CypherTCK.allTckScenariosFromFilesystem(afterPath)

  private val (before, after) = (TckTree(scenariosBefore), TckTree(scenariosAfter))

  val tckTreeDiff: TckTreeDiff = TckTreeDiff(before, after)

  val scenario2Collection: Map[Scenario, TckCollection] =
    tckTreeDiff.diffs(Total).unchangedScenarios.map(_ -> BothCollections).toMap ++
      tckTreeDiff.diffs(Total).movedScenarios.flatMap { case d => Map(d.before -> BeforeCollection, d.after -> AfterCollection) } ++
      tckTreeDiff.diffs(Total).changedScenarios.flatMap { case d => Map(d.before -> BeforeCollection, d.after -> AfterCollection) } ++
      tckTreeDiff.diffs(Total).addedScenarios.map(_ -> AfterCollection).toMap ++
      tckTreeDiff.diffs(Total).removedScenarios.map(_ -> BeforeCollection).toMap

  val (groupId2Group, group2GroupId) = {
    val groupList = tckTreeDiff.groups.toIndexedSeq
    (groupList, groupList.zipWithIndex.map(p => (p._1, p._2)).toMap)
  }

  val (scenarioId2Scenario, scenario2ScenarioId) = {
    val scenarioList = (scenariosBefore concat scenariosAfter).toList.toIndexedSeq
    (scenarioList, scenarioList.zipWithIndex.map(p => (p._1, p._2)).toMap)
  }
}