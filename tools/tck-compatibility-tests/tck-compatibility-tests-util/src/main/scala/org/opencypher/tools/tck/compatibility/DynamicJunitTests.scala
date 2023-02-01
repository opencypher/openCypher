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
package org.opencypher.tools.tck.compatibility

import java.util

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.function.Executable
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.groups.ContainerGroup
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.api.groups.Item
import org.opencypher.tools.tck.api.groups.Tag
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total

import scala.jdk.CollectionConverters._

trait DynamicJunitTests {

  def create(scenarios: Seq[Scenario], createTest: Scenario => Executable): util.Collection[DynamicNode] = {
    implicit val tck: TckTree = TckTree(scenarios)

    def spawnTests(currentGroup: Group): Seq[DynamicNode] = {
      currentGroup match {
        case Total =>
          Total.children.toSeq.sorted.flatMap(g => spawnTests(g))
        case _: Tag => Seq.empty[DynamicNode]
        case g: ContainerGroup =>
          Seq(DynamicContainer.dynamicContainer(
            g.description,
            g.children.flatMap(g => spawnTests(g)).asJavaCollection
          ))
        case i: Item =>
          Seq(DynamicTest.dynamicTest(i.description, createTest(i.scenario)))
      }
    }

    spawnTests(Total).asJavaCollection
  }
}
