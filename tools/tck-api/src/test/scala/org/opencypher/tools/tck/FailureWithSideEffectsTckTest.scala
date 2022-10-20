/*
 * Copyright (c) 2015-2022 "Neo Technology,"
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

import org.opencypher.tools.tck.api._
import org.opencypher.tools.tck.constants.TCKErrorDetails.ANY
import org.opencypher.tools.tck.constants.TCKErrorPhases.COMPILE_TIME
import org.opencypher.tools.tck.constants.TCKErrorTypes.ERROR
import org.opencypher.tools.tck.constants.TCKQueries.LABELS_QUERY
import org.opencypher.tools.tck.constants.TCKQueries.NODES_QUERY
import org.opencypher.tools.tck.constants.TCKQueries.NODE_PROPS_QUERY
import org.opencypher.tools.tck.values.CypherInteger
import org.opencypher.tools.tck.values.CypherString
import org.opencypher.tools.tck.values.CypherValue
import org.scalatest.Assertions
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class FailureWithSideEffectsTckTest extends AnyFunSuite with Assertions with Matchers {

  private val scenarios =  {
    CypherTCK.parseFeatures(getClass.getResource("FailureWithSideEffects.feature").toURI) match {
      case feature :: Nil => feature.scenarios
      case _              => List[Scenario]()
    }
  }

  test("Fail with side effects") {
    val scenario = scenarios.find(_.name == "Fail with side effects").get
    scenario(new FakeGraph).run()
  }

  test("Fail with side effects, incorrect side effect assertion 1") {
    val scenario = scenarios.find(_.name == "Fail scenario because of incorrect side effects 1").get
    val exception = intercept[Throwable](scenario(new FakeGraph).run())
    exception.getMessage should include ("Fail scenario because of incorrect side effects")
  }

  test("Fail without side effects, incorrect side effect assertion 2") {
    val scenario = scenarios.find(_.name == "Fail scenario because of incorrect side effects 2").get
    val exception = intercept[Throwable](scenario(new FakeGraph).run())
    exception.getMessage should include ("Fail scenario because of incorrect side effects")
  }

  private class FakeGraph extends Graph with ProcedureSupport with CsvFileCreationSupport {
    private var hasExecutedQuery = false

    override def cypher(query: String, params: Map[String, CypherValue], queryType: QueryType): Result = {
      queryType match {
        case InitQuery =>
          CypherValueRecords.empty
        case SideEffectQuery =>
          if (!hasExecutedQuery) {
            CypherValueRecords.empty
          } else if (query == NODES_QUERY) {
            CypherValueRecords(List("id(n)"), List(Map("id(n)" -> CypherInteger(1))))
          } else if (query == LABELS_QUERY) {
            CypherValueRecords(List("label"), List(Map("label" -> CypherString("N"))))
          } else if (query == NODE_PROPS_QUERY) {
            val result = Map("nodeId" -> CypherInteger(1), "key" -> CypherString("p"), "value" -> CypherInteger(-1))
            CypherValueRecords(result.keySet.toList, List(result))
          } else {
            CypherValueRecords.empty
          }
        case ControlQuery =>
          CypherValueRecords.empty
        case ExecQuery =>
          hasExecutedQuery = true
          ExecutionFailed(ERROR, COMPILE_TIME, ANY)
      }
    }

    override def registerProcedure(signature: String, values: CypherValueRecords): Unit = ()

    override def createCSVFile(contents: CypherValueRecords): String = ???
  }
}
