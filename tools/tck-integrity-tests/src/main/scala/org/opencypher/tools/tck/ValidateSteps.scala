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

import io.cucumber.core.gherkin.StepType
import org.opencypher.tools.tck.api.ControlQuery
import org.opencypher.tools.tck.api.ExecQuery
import org.opencypher.tools.tck.api.Execute
import org.opencypher.tools.tck.api.ExpectError
import org.opencypher.tools.tck.api.ExpectResult
import org.opencypher.tools.tck.api.InitQuery
import org.opencypher.tools.tck.api.SideEffects
import org.opencypher.tools.tck.api.Step
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

/**
  * This function will check that the input query string adheres to the specified code styling rules for Cypher queries.
  * If the code had bad styling, a message will be returned showing the bad query and a prettified version of it,
  * otherwise None will be returned.
  */
trait ValidateSteps extends AnyFunSpecLike with Matchers with OptionValues with ValidateQuery with ValidateSideEffects with ValidateError {

  def validateSteps(steps: List[Step], tags: Set[String]): Unit = {

    class DescribedStep(s: Step) {
      def description: String = s"step `${s.source.getKeyWord.trim} ${s.source.getText}` in line ${s.source.getLine}"
    }
    implicit def toDescribedStep(s: Step): DescribedStep = new DescribedStep(s)

    val numberOfExecQuerySteps = steps.count {
      case Execute(_, ExecQuery, _) => true
      case _ => false
    }
    they("have exactly one `When executing query` steps") {
      numberOfExecQuerySteps shouldBe 1
    }

    val numberOfControlQuerySteps = steps.count {
      case Execute(_, ControlQuery, _) => true
      case _ => false
    }
    they("have at most one `When executing control query` steps") {
      numberOfControlQuerySteps should be <= 1
    }

    val numberOfExpectErrorSteps = steps.count {
      case _:ExpectError => true
      case _ => false
    }
    they("have at most one `Then expect error` steps") {
      numberOfExpectErrorSteps should be <= 1
    }

    they("have as many `Then expect results` and `Then expect error` steps as `When executing query` and `When executing control query` steps") {
      val numberOfExpectResultSteps = steps.count {
        case _:ExpectResult => true
        case _ => false
      }
      (numberOfExpectResultSteps + numberOfExpectErrorSteps) should equal(numberOfExecQuerySteps + numberOfControlQuerySteps)
    }

    they("have as many explicit `And side effects` step or implicit SideEffects step as `When executing query` steps") {
      val numberOfExplicitSideEffectSteps = steps.count {
        case se:SideEffects if se.source.getType == StepType.AND => true
        case _ => false
      }
      val numberOfImplicitSideEffectSteps = steps.count {
        case se:SideEffects if se.source.getType == StepType.THEN => true
        case _ => false
      }
      (numberOfExplicitSideEffectSteps + numberOfImplicitSideEffectSteps) shouldBe numberOfExecQuerySteps
    }

    they("start with a `Given` step") {
      steps.head.source.getType == StepType.GIVEN
    }

    describe("have the right order, so that") {
      val stepPairs = steps zip steps.tail

      stepPairs foreach {
        case (predecessor, er:ExpectResult) =>
          it(s"${er.description} is preceded by a `When executing query` or `When executing control query` step") {
            predecessor should matchPattern { case Execute(_, ExecQuery | ControlQuery, _) => }
          }
        case (predecessor, ee:ExpectError) =>
          it(s"${ee.description} is preceded by a `When executing query` step") {
            predecessor should matchPattern { case Execute(_, ExecQuery, _) => }
          }
        case (predecessor, se:SideEffects) if se.source.getType == StepType.AND =>
          it(s"${se.description} is preceded by a `Then expect results` step") {
            predecessor should matchPattern { case _:ExpectResult => }
          }
        case (predecessor, se:SideEffects) if se.source.getType == StepType.THEN =>
          it(s"${se.description} is preceded by a `Then expect error` step") {
            predecessor should matchPattern { case _:ExpectError => }
          }
        case _ => Unit
      }

      they("every `Then expect error` step is not succeeded by anything, i.e. is the last step") {
        stepPairs should not contain matchPattern { case (_:ExpectError, _) => }
      }

      they("every `Given` and `And having executed` step before any `When executing query` or `When executing control query` step") {
        steps.lastIndexWhere {
          case Execute(_, InitQuery, _) => true
          case _ => false
        } should be < steps.indexWhere {
          case Execute(_, ExecQuery, _) => true
          case _ => false
        }
      }
    }

    describe("have valid step parameters, so that") {
      steps foreach {
        case e:Execute =>
          describe(s"the query of ${e.description}") {
            validateQuery(e, tags)
          }
        case se:SideEffects =>
          describe(s"the expectation of ${se.description}") {
            validateSideEffects(se)
          }
        case ee:ExpectError =>
          describe(s"the error detail of ${ee.description}") {
            validateError(ee.errorType, ee.phase, ee.detail)
          }
        case _ => Unit
      }
    }
  }
}
