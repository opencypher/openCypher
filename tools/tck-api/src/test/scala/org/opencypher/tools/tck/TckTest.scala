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

import org.opencypher.tools.tck.api._
import org.opencypher.tools.tck.constants.TCKErrorDetails.UNKNOWN_FUNCTION
import org.opencypher.tools.tck.constants.TCKErrorPhases.COMPILE_TIME
import org.opencypher.tools.tck.constants.TCKErrorTypes.SYNTAX_ERROR
import org.opencypher.tools.tck.values.CypherValue
import org.scalatest.Assertions
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.compat.immutable._

class TckTest extends AnyFunSpec with Assertions with Matchers {

  private val scenarios = CypherTCK.parseFeatures(getClass.getResource("Foo.feature").toURI) match {
    case feature :: Nil => feature.scenarios
    case _              => List[Scenario]()
  }

  private val (invalidScenarios, validScenarios) = scenarios.partition(_.name.startsWith("Do not fail init queries silently"))

  describe("Out of the scenarios in Foo.feature") {
    validScenarios.foreach { scenario =>
      it(s"${scenario.toString()} should run successfully") {
        scenario(FakeGraph).run()
        succeed
      }
    }

    invalidScenarios.foreach { scenario =>
      it(s"${scenario.toString()} should fail") {
        val e = the [scenario.ScenarioFailedException] thrownBy scenario(FakeGraph).run()
        causes(e) should contain(FakeGraph.FAIL_EXCEPTION)
      }
    }
  }

  describe("Error handling") {
    it("should retain original exception from side effect queries") {
      val myException = MyException("original")

      val graph = FailingGraph(FakeGraph) {
        case SideEffectQuery => myException
      }

      val scenario = validScenarios.head

      val e = the [scenario.ScenarioFailedException] thrownBy scenario(graph).run()
      causes(e) should contain(myException)
    }
  }

  def causes(throwable: Throwable): LazyList[Throwable] = {
    val self = LazyList(throwable)
    Option(throwable.getCause) match {
      case None              => self
      case Some(`throwable`) => self
      case Some(cause)       => self.lazyAppendedAll(causes(cause))
    }
  }

  private object FakeGraph extends Graph with ProcedureSupport with CsvFileCreationSupport {

    var cvsData: CypherValueRecords = CypherValueRecords.empty

    val FAIL_EXCEPTION: MyException = MyException("fail")

    override def cypher(query: String, params: Map[String, CypherValue], queryType: QueryType): Result = {
      queryType match {
        case InitQuery if query.contains("FAIL") =>
          ExecutionFailed(SYNTAX_ERROR, COMPILE_TIME, "fail", Some(FAIL_EXCEPTION))
        case InitQuery if !query.contains("FAIL") =>
          CypherValueRecords.empty
        case SideEffectQuery =>
          CypherValueRecords.empty
        case ControlQuery =>
          CypherValueRecords.empty
        case ExecQuery if query.contains("foo()") =>
          ExecutionFailed(SYNTAX_ERROR, COMPILE_TIME, UNKNOWN_FUNCTION)
        // assert that csv path parameter is not overwritten by additional parameters
        case ExecQuery if query.contains("LOAD CSV") && params.keySet.equals(Set("param", "list")) =>
          StringRecords(List("res"), cvsData.rows.map(r => Map("res" -> r("txt").toString)))
        case ExecQuery =>
          StringRecords(List("1"), List(Map("1" -> "1")))
      }
    }

    override def registerProcedure(signature: String, values: CypherValueRecords): Unit =
      ()

    override def createCSVFile(contents: CypherValueRecords): String = {
      cvsData = contents
      "dummy/path.csv"
    }
  }

  private case class FailingGraph(base: Graph)(failureFor: PartialFunction[QueryType, Throwable]) extends Graph with ProcedureSupport {
    override def cypher(query: String, params: Map[String, CypherValue], queryType: QueryType): Result = {
      failureFor.lift.apply(queryType) match {
        case Some(e) => ExecutionFailed("dummyType", "dummyPhase", "dummyDetail", Some(e))
        case None    => base.cypher(query, params, queryType)
      }
    }

    override def registerProcedure(signature: String, values: CypherValueRecords): Unit =
      ()
  }

  private case class MyException(msg: String) extends Exception(msg)
}
