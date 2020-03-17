/*
 * Copyright (c) 2015-2020 "Neo Technology,"
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
package org.opencypher.tools.tck.inspection

import org.opencypher.tools.tck.api.Different
import org.opencypher.tools.tck.api.PotentiallyDuplicated
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.ScenarioDiff

case class GroupDiff(unchanged: Set[Scenario], changed: Set[(Scenario, Scenario, Set[ScenarioDiff])], added: Set[Scenario], removed: Set[Scenario])

case object GroupDiff {
  def apply(scenariosBefore: Option[Seq[Scenario]], scenariosAfter: Option[Seq[Scenario]]): GroupDiff = {
    GroupDiff(scenariosBefore.getOrElse(Seq[Scenario]()), scenariosAfter.getOrElse(Seq[Scenario]()))
  }

  def apply(scenariosBefore: Seq[Scenario], scenariosAfter: Seq[Scenario]): GroupDiff = {
    GroupDiff(scenariosBefore.toSet, scenariosAfter.toSet)
  }

  def apply(scenariosBefore: Set[Scenario], scenariosAfter: Set[Scenario]): GroupDiff = {
    val unchangedScenarios = scenariosAfter intersect scenariosBefore

    val removedOrChangedScenarios: Set[Scenario] = scenariosBefore -- unchangedScenarios
    val addedOrChangedScenarios: Set[Scenario] = scenariosAfter -- unchangedScenarios

    val removeScenarios = removedOrChangedScenarios.filter(b => addedOrChangedScenarios.forall(_.diff(b) == Set(Different)))
    val addedScenarios = addedOrChangedScenarios.filter(a => removedOrChangedScenarios.forall(_.diff(a) == Set(Different)))

    val changedScenariosBefore = removedOrChangedScenarios -- removeScenarios
    val changedScenariosAfter = addedOrChangedScenarios -- addedScenarios

    val changedScenarios: Set[(Scenario, Scenario, Set[ScenarioDiff])] = changedScenariosBefore.flatMap(
      b => changedScenariosAfter.map(a => (b, a, b.diff(a) - PotentiallyDuplicated)).filter {
        case (_, _, d) if d contains Different => false
        case _ => true
      }
    )

    GroupDiff(unchangedScenarios, changedScenarios, addedScenarios, removeScenarios)
  }
}

object scenarioGroupDiff extends ((Map[Group, Seq[Scenario]], Map[Group, Seq[Scenario]]) => Map[Group, GroupDiff]) {
  def apply(before: Map[Group, Seq[Scenario]],
           after: Map[Group, Seq[Scenario]]): Map[Group, GroupDiff] = {
    val allGroups = before.keySet ++ after.keySet
    allGroups.map(f = group => (group, GroupDiff(before.get(group), after.get(group)))).toMap
  }
}
