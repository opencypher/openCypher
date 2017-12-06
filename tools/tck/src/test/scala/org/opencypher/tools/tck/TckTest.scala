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

import org.junit.jupiter.api.{DynamicTest, TestFactory}
import org.opencypher.tools.tck.api._
import org.opencypher.tools.tck.values.CypherValue

import scala.collection.JavaConverters._

object FakeGraph extends Graph with ProcedureSupport {
  override def cypher(query: String, params: Map[String, CypherValue] = Map.empty): Records = {
    CypherValueRecords.empty
  }

  override def registerProcedure(signature: String, values: CypherValueRecords): Unit =
    ()
}

class TckTest {

  val tckScenarios = CypherTCK.allTckScenariosFromFilesystem.flatMap(_.scenarios)

  val fooUri = getClass.getResource("Foo.feature").toURI
  //val ourScenarios = CypherTCK.parseFeature(new File(fooUri)).scenarios

  val allScenarios = tckScenarios //++ ourScenarios

  @TestFactory
  def testGraphTests() = {
    def createTestGraph(): Graph = FakeGraph

    val dynamicTests = allScenarios.map { scenario =>
      val name = scenario.toString
      val executable = scenario(createTestGraph)
      DynamicTest.dynamicTest(name, executable)
    }
    dynamicTests.asJavaCollection
  }

}
