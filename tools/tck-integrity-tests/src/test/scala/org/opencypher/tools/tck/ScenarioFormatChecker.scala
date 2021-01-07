/*
 * Copyright (c) 2015-2021 "Neo Technology,"
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

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.ExecQuery
import org.opencypher.tools.tck.api.InitQuery
import org.opencypher.tools.tck.api.ControlQuery
import org.opencypher.tools.tck.api.Execute
import org.opencypher.tools.tck.api.ExpectError
import org.opencypher.tools.tck.api.ExpectResult
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.SideEffects
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.api.groups.ContainerGroup
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.api.groups.Item
import org.opencypher.tools.tck.api.groups.Tag
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.language.implicitConversions

class ScenarioFormatChecker extends AnyFunSpecLike with Matchers with OptionValues {

  implicit val tck: TckTree = TckTree(CypherTCK.allTckScenarios)

  def spawnTests(currentGroup: Group): Unit = {
    currentGroup match {
      case Total =>
        Total.children.foreach(spawnTests)
      case _: Tag => Unit // do not execute scenarios via tags, would be redundant
      case g: ContainerGroup =>
        describe(g.description) {
          g.children.foreach(spawnTests)
        }
      case i: Item =>
        /*
         * use ignore(...) for switching OFF the test
         * use it(...) for switching ON the test
         */
        describe(i.description) {
          checkScenario(i.scenario)
        }
      case _ => Unit
    }
  }

  spawnTests(Total)

  def checkScenario(scenario: Scenario) {
    it("has a number, greater zero") {
      scenario.number.value should be > 0
    }

    val steps = scenario.steps

    class DescribedStep(s: Step) {
      def description: String = s"step ${s.source.getKeyWord.trim} ${s.source.getText} in line ${s.source.getLine}"
    }
    implicit def toDescribedStep(s: Step): DescribedStep = new DescribedStep(s)

    val numberOfExecQuerySteps = steps.count {
      case Execute(_, ExecQuery, _) => true
      case _ => false
    }
    it("has exactly one `When executing query` steps") {
      numberOfExecQuerySteps shouldBe 1
    }

    val numberOfControlQuerySteps = steps.count {
      case Execute(_, ControlQuery, _) => true
      case _ => false
    }
    it("has at most one `When executing control query` steps") {
      numberOfControlQuerySteps should be <= 1
    }

    val numberOfExpectErrorSteps = steps.count {
      case _:ExpectError => true
      case _ => false
    }
    it("has at most one `Then expect error` steps") {
      numberOfExpectErrorSteps should be <= 1
    }

    it("has as many `Then expect results` and `Then expect error` steps as `When executing query` and `When executing control query` steps") {
      val numberOfExpectResultSteps = steps.count {
        case _:ExpectResult => true
        case _ => false
      }
      (numberOfExpectResultSteps + numberOfExpectErrorSteps) should equal(numberOfExecQuerySteps + numberOfControlQuerySteps)
    }

    val numberOfExplicitSideEffectSteps = steps.count {
      case se:SideEffects if se.source.getKeyWord.trim.toLowerCase == "and" => true
      case _ => false
    }
    val numberOfImplicitSideEffectSteps = steps.count {
      case se:SideEffects if se.source.getKeyWord.trim.toLowerCase == "then" => true
      case _ => false
    }
    it("has as many explicit `And side effects` step or implicit SideEffects step as `When executing query` steps") {
      (numberOfExplicitSideEffectSteps + numberOfImplicitSideEffectSteps) shouldBe numberOfExecQuerySteps
    }

    it("starts with a `Given` step") {
      steps.head.isGivenStep shouldBe true
    }

    val stepPairs = steps zip steps.tail

    describe("has every explicit `And side effects` step and implicit SideEffects step preceded by a `Then expect results` or `Then expect error` step, respectively, so that") {
      stepPairs foreach {
        case (predecessor, se:SideEffects) if se.source.getKeyWord.trim.toLowerCase == "and" =>
          it(s"${se.description} preceded by a `Then expect results` step") {
            predecessor should matchPattern { case _:ExpectResult => }
          }
        case (predecessor, se:SideEffects) if se.source.getKeyWord.trim.toLowerCase == "then" =>
          it(s"${se.description} preceded by a `Then expect error` step") {
            predecessor should matchPattern { case _:ExpectError => }
          }
        case _ => Unit
      }
    }

    describe("has every `Then expect results` preceded by a `When executing query` or `When executing control query` step, so that") {
      stepPairs foreach {
        case (predecessor, er:ExpectResult) =>
          it(s"${er.description} preceded by a `When executing query` or `When executing control query` step") {
            predecessor should matchPattern { case Execute(_, ExecQuery | ControlQuery, _) => }
          }
        case _ => Unit
      }
    }

    describe("has every `Then expect error` preceded by a `When executing query` step, so that") {
      stepPairs foreach {
        case (predecessor, ee:ExpectError) =>
          it(s"${ee.description} preceded by a `When executing query` step") {
            predecessor should matchPattern { case Execute(_, ExecQuery, _) => }
          }
        case _ => Unit
      }
    }

    it("has every `Then expect error` succeeded by nothing (i.e. the last step)") {
      stepPairs should not contain matchPattern { case (_:ExpectError, _) => }
    }

    it("has a `@NegativeTest` tag and a `Then expect error` step or neither") {
      (steps exists {
        case _:ExpectError => true
        case _ => false
      }) should equal (scenario.tags contains "@NegativeTest")
    }

    it("has every `Given` and `And having executed` step before any `When executing query` or `When executing control query` step") {
      steps.lastIndexWhere {
        case Execute(_, InitQuery, _) => true
        case _ => false
      } should be < steps.indexWhere {
        case Execute(_, ExecQuery, _) => true
        case _ => false
      }
    }
  }
}