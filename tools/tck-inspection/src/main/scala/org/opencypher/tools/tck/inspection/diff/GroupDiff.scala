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
package org.opencypher.tools.tck.inspection.diff

import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.inspection.diff.ScenarioDiffTag._

case class GroupDiff(unchanged: Set[Scenario], moved: Set[(Scenario, Scenario, ScenarioDiff)], changed: Set[(Scenario, Scenario, ScenarioDiff)], added: Set[Scenario], removed: Set[Scenario])

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

    val removeScenarios = removedOrChangedScenarios.filter(b => addedOrChangedScenarios.forall(a => ScenarioDiff(b, a).diffTags == Set(Different)))
    val addedScenarios = addedOrChangedScenarios.filter(a => removedOrChangedScenarios.forall(b => ScenarioDiff(b, a).diffTags == Set(Different)))

    val changedScenariosBefore = removedOrChangedScenarios -- removeScenarios
    val changedScenariosAfter = addedOrChangedScenarios -- addedScenarios

    val allChangedScenarios: Set[(Scenario, Scenario, ScenarioDiff)] = changedScenariosBefore.flatMap(
      b => changedScenariosAfter.map(a => (b, a, ScenarioDiff(b, a))).filter {
        case (_, _, d) if d.diffTags contains Different => false
        case _ => true
      }
    )

    val (movedScenarios: Set[(Scenario, Scenario, ScenarioDiff)], changedScenarios: Set[(Scenario, Scenario, ScenarioDiff)]) = allChangedScenarios.partition(_._3.diffTags == Set(Moved))

    GroupDiff(unchangedScenarios, movedScenarios, changedScenarios, addedScenarios, removeScenarios)
  }
}
