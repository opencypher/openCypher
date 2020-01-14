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
package org.opencypher.tools.tck.api.events

import java.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{AfterAll, BeforeAll, DynamicTest, TestFactory}
import org.opencypher.tools.tck.api._
import org.opencypher.tools.tck.values.CypherValue

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object TCKEventsTest {

  private val events = ListBuffer[String]()

  @BeforeAll
  def subscribe(): Unit = {
    TCKEvents.feature.subscribe(
      f => { if (f.name == "ListOperations") { events += s"Feature '${f.name}' read" } })
    TCKEvents.scenario.subscribe(s => events += s"Scenario '${s.name}' started")
    TCKEvents.stepStarted.subscribe(s =>
      events += s"Step '${s.step.getClass.getSimpleName} -> ${s.step.source.getText}' started")
    TCKEvents.stepFinished.subscribe(s =>
      events += s"Step '${s.step.getClass.getSimpleName}' finished. Result: ${s.result.right.get.right.get}")
  }

  @AfterAll
  def assertEvents(): Unit = {
    TCKEvents.reset()
    val expected = List[String](
      "Feature 'ListOperations' read",
      "Scenario 'Return list size' started",
      "Step 'Execute -> any graph' started",
      "Step 'Execute' finished. Result: <empty result>",
      "Step 'Measure -> executing query:' started",
      "Step 'Measure' finished. Result: <empty result>",
      "Step 'Execute -> executing query:' started",
      "Step 'Execute' finished. Result: | n |" + System.lineSeparator + "| 3 |",
      "Step 'ExpectResult -> the result should be, in any order:' started",
      "Step 'ExpectResult' finished. Result: | n |" + System.lineSeparator + "| 3 |",
      "Step 'SideEffects -> no side effects' started",
      "Step 'SideEffects' finished. Result: | n |" + System.lineSeparator + "| 3 |"
    )
    assertEquals(expected.asJava, events.toList.asJava, s"${expected.asJava} did not equal ${events.toList.asJava}")
  }
}

class TCKEventsTest {

  @TestFactory
  def testSingleScenario(): util.Collection[DynamicTest] = {
    val scenarios = CypherTCK.allTckScenarios.filter(s => s.name == "Return list size")

    def createTestGraph(): Graph = FakeGraph

    val dynamicTests = scenarios.map { scenario =>
      val name = scenario.toString()
      val executable = scenario(createTestGraph())
      DynamicTest.dynamicTest(name, executable)
    }
    dynamicTests.asJavaCollection
  }

  private object FakeGraph extends Graph with ProcedureSupport {
    override def cypher(query: String, params: Map[String, CypherValue], queryType: QueryType): Result = {
      queryType match {
        case InitQuery =>
          CypherValueRecords.empty
        case SideEffectQuery =>
          CypherValueRecords.empty
        case ExecQuery =>
          StringRecords(List("n"), List(Map("n" -> "3")))
      }
    }
    override def registerProcedure(signature: String, values: CypherValueRecords): Unit =
      ()
  }
}
