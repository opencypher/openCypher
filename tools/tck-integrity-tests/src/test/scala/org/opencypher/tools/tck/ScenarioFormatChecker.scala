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

import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.groups.ContainerGroup
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.api.groups.Item
import org.opencypher.tools.tck.api.groups.Tag
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

trait ScenarioFormatChecker extends AnyFunSpec with Matchers with OptionValues with ValidateScenario {

  def create(scenarios: Seq[Scenario]): Unit = {
    implicit val tck: TckTree = TckTree(scenarios)

    def spawnTests(currentGroup: Group): Unit = {
      currentGroup match {
        case Total =>
          Total.children.toSeq.sorted.foreach(spawnTests)
        case _: Tag => ()
        case g: ContainerGroup =>
          describe(g.description) {
            g.children.toSeq.sorted.foreach(spawnTests)
          }
        case i: Item =>
          it(i.description) {
            validateScenario(i.scenario)
          }
        case _ => ()
      }
    }

    spawnTests(Total)
  }
}
