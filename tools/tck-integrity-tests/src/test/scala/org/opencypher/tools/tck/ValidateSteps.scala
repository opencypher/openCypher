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

import io.cucumber.core.gherkin.StepType
import org.opencypher.tools.tck.api.ControlQuery
import org.opencypher.tools.tck.api.CsvFile
import org.opencypher.tools.tck.api.ExecQuery
import org.opencypher.tools.tck.api.Execute
import org.opencypher.tools.tck.api.ExpectError
import org.opencypher.tools.tck.api.ExpectResult
import org.opencypher.tools.tck.api.InitQuery
import org.opencypher.tools.tck.api.Parameters
import org.opencypher.tools.tck.api.SideEffects
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.constants.TCKErrorDetails
import org.opencypher.tools.tck.constants.TCKErrorPhases
import org.opencypher.tools.tck.constants.TCKErrorTypes
import org.scalatest.AppendedClues
import org.scalatest.Assertion
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers

trait ValidateSteps extends AppendedClues with Matchers with OptionValues with ValidateQuery with ValidateSideEffects  with DescribeStepHelper {

  def validateSteps(steps: List[Step], tags: Set[String]): Assertion = {
    var numberOfExecQuerySteps,
        numberOfControlQuerySteps,
        numberOfExpectErrorSteps,
        numberOfExpectResultSteps,
        numberOfExplicitSideEffectSteps,
        numberOfImplicitSideEffectSteps = 0
    var positionLastInitQuery = Int.MinValue
    var positionFirstExecQuery = Int.MaxValue

    steps.zipWithIndex foreach {
      case (Execute(_, InitQuery, _), ix) => positionLastInitQuery = Math.max(positionLastInitQuery, ix)
      case (Execute(_, ExecQuery, _), ix) => numberOfExecQuerySteps += 1
        positionFirstExecQuery = Math.min(positionFirstExecQuery, ix)
      case (Execute(_, ControlQuery, _), _) => numberOfControlQuerySteps += 1
      case (_: ExpectError, _) => numberOfExpectErrorSteps += 1
      case (_: ExpectResult, _) => numberOfExpectResultSteps += 1
      case (se: SideEffects, _) if se.source.getType == StepType.AND => numberOfExplicitSideEffectSteps += 1
      case (se: SideEffects, _) if se.source.getType == StepType.THEN => numberOfImplicitSideEffectSteps += 1
      case _ => ()
    }

    withClue("scenario has exactly one `When executing query` steps") {
      numberOfExecQuerySteps shouldBe 1
    }

    withClue("scenario has at most one `When executing control query` steps") {
      numberOfControlQuerySteps should be <= 1
    }

    withClue("scenario has at most one `Then expect error` steps") {
      numberOfExpectErrorSteps should be <= 1
    }

    withClue("scenario has as many `Then expect results` and `Then expect error` steps as `When executing query` and `When executing control query` steps") {
      (numberOfExpectResultSteps + numberOfExpectErrorSteps) should equal(numberOfExecQuerySteps + numberOfControlQuerySteps)
    }

    withClue("scenario has as many explicit `And side effects` step or implicit SideEffects step as `When executing query` steps") {
      (numberOfExplicitSideEffectSteps + numberOfImplicitSideEffectSteps) shouldBe numberOfExecQuerySteps
    }

    withClue("scenario starts with a `Given` step") {
      steps.head.source.getType shouldBe StepType.GIVEN
    }

    (steps zip steps.tail) foreach {
      case (predecessor, er: ExpectResult) =>
        withClue(s"${er.description} is preceded by a `When executing query` or `When executing control query` step") {
          predecessor should matchPattern { case Execute(_, ExecQuery | ControlQuery, _) => }
        }
      case (predecessor, ee: ExpectError) =>
        withClue(s"${ee.description} is preceded by a `When executing query` step") {
          predecessor should matchPattern { case Execute(_, ExecQuery, _) => }
        }
      case (predecessor, se: SideEffects) if se.source.getType == StepType.AND =>
        withClue(s"${se.description} is preceded by a `Then expect results` step") {
          predecessor should matchPattern { case _: ExpectResult => }
        }
      case (predecessor, se: SideEffects) if se.source.getType == StepType.THEN =>
        withClue(s"${se.description} is preceded by a `Then expect error` step") {
          predecessor should matchPattern { case _: ExpectError => }
        }
      case (ee: ExpectError, successor) =>
        withClue(s"${ee.description} is not succeeded by anything, i.e. is the last step") {
          fail(s"${ee.description} is succeeded by ${successor.description}")
        }
      case _ => succeed
    }

    withClue("every `Given` and `And having executed` step before any `When executing query` or `When executing control query` step") {
      positionLastInitQuery should be < positionFirstExecQuery
    }

    steps foreach {
      case e: Execute => validateQuery(e, tags)
      case se: SideEffects => validateSideEffects(se)
      case ee: ExpectError =>
        withClue(s"${ee.description} has valid type") {
          TCKErrorTypes.ALL should contain(ee.errorType)
        }
        withClue(s"${ee.description} has valid phase") {
          TCKErrorPhases.ALL should contain(ee.phase)
        }
        withClue(s"${ee.description} has valid detail") {
          TCKErrorDetails.ALL should contain(ee.detail)
        }
      case _ => succeed
    }

    withClue("Scenario declares conflicting parameter ") {
      val parameters: Seq[String] = steps.collect {
        case Parameters(parameters, _) => parameters.keys.toList
        case CsvFile(parameter, _, _) => List(parameter)
      }.flatten
      parameters.groupBy(identity).foreach {
        case (parameter, group) =>
          withClue(s"$$$parameter: ") {
            group.size shouldBe 1
          }
        case _ =>
      }
    }

    succeed
  }
}
