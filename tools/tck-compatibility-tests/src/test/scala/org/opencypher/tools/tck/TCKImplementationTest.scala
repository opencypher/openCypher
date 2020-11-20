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
package org.opencypher.tools.tck

import java.util

import cypher.features.InterpretedTCKTests
import cypher.features.InterpretedTestConfig
import cypher.features.Neo4jAdapter.defaultTestConfig
import cypher.features.ScenarioTestHelper
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.neo4j.test.TestDatabaseManagementServiceBuilder
import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.groups.ContainerGroup
import org.opencypher.tools.tck.api.groups.Feature
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.api.groups.ScenarioCategory
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total
import org.scalatest.ParallelTestExecution
import org.scalatest.Tag
import org.scalatest.funspec.AsyncFunSpec

import scala.collection.JavaConverters._
import scala.concurrent.Future

class TCKImplementationTest extends AsyncFunSpec with ParallelTestExecution {

  /*
   * To run these test in parallel, provide the program argument -P<n> to the runner,
   * where <n> is the number of threads, e.g. -P16
   * (with -P the number of threads will be decided based on the number of processors available)
   */
  describe("On Neo4j") {
    val tck = TckTree(CypherTCK.allTckScenarios)

    def spawnTests(currentGroup: Group): Unit = {
      currentGroup match {
        case Total =>
          tck.groupChildren(Total).foreach(g => spawnTests(g))
        case c:ScenarioCategory =>
          describe(s"${c.name}") {
            tck.groupChildren(c).foreach(g => spawnTests(g))
          }
        case f:Feature =>
          describe(s"${f.name}") {
            val scenarios = tck.groupedScenarios(f).sortBy(s => (s.name, s.exampleIndex))
            val tests = scenarios.map(s => (s, ScenarioTestHelper.createTests(List(s), InterpretedTestConfig, () => new TestDatabaseManagementServiceBuilder(), defaultTestConfig).asScala.head))
            tests foreach {
              case (s, t) =>
                /*
                 * use ignore(...) for switching OFF the test
                 * use it(...) for switching ON the test
                 */
                it(s"${s.name}${s.exampleIndex.map(ix => " #"+ix).getOrElse("")}", s.tags.map(t => Tag(t)).toSeq: _*) {
                  val future = Future {
                    //print(":")
                    t.getExecutable.execute()
                  }
                  future map { _ => succeed }
                }
            }
          }
        case _ => Unit
      }
    }

    spawnTests(Total)
  }
}

object TCKImplementationTest {
  def tckOnNeo4jAsJUnitTests(): util.Collection[DynamicTest] = new InterpretedTCKTests().runInterpreted()

  def tckOnNeo4jAsJUnitHierarchicalTests(): util.Collection[DynamicNode] = {
    val tck = TckTree(CypherTCK.allTckScenarios)

    def createTests(currentGroup: Group = Total): DynamicNode = {
      currentGroup match {
        case c:ContainerGroup =>
          DynamicContainer.dynamicContainer(
            c.name,
            tck.groupChildren(c).map(g => createTests(g)).asJavaCollection
          )
        case f:Feature =>
          DynamicContainer.dynamicContainer(
            f.name,
            ScenarioTestHelper.createTests(tck.groupedScenarios(f), InterpretedTestConfig, () => new TestDatabaseManagementServiceBuilder(), defaultTestConfig)
          )
      }
    }

    Seq(createTests()).asJavaCollection
  }
}
