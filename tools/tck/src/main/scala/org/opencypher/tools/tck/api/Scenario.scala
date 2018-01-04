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
package org.opencypher.tools.tck.api

import org.junit.jupiter.api.function.Executable
import org.opencypher.tools.tck.SideEffectOps
import org.opencypher.tools.tck.SideEffectOps._
import org.opencypher.tools.tck.api.Graph.Result
import org.opencypher.tools.tck.values.CypherValue

import scala.compat.Platform.EOL
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

case class Scenario(featureName: String, name: String, tags: Set[String], steps: List[Step]) {

  self =>

  override def toString = s"""Feature "$featureName": Scenario "$name""""

  def apply(graph: => Graph): Executable = new Executable {
    override def execute(): Unit = {
      val g = graph // ensure that lazy parameter is only evaluated once
      try {
        executeOnGraph(g)
      } finally g.close()
    }
  }

  def executeOnGraph(empty: Graph): Unit = {
    steps.foldLeft(ScenarioExecutionContext(empty)) {

      case (ctx, Execute(query, qt)) =>
        ctx.execute(query, qt)

      case (ctx, Measure) =>
        ctx.measure

      case (ctx, RegisterProcedure(signature, table)) =>
        ctx.graph match {
          case support: ProcedureSupport =>
            support.registerProcedure(signature, table)
          case _ =>
        }
        ctx

      case (ctx, ExpectResult(expected, sorted)) =>
        ctx.lastResult match {
          case Right(records) =>
            val correctResult =
              if (sorted)
                expected == records
              else
                expected.equalsUnordered(records)

            if (!correctResult) {
              val detail = if (sorted) "ordered rows" else "in any order of rows"
              throw ScenarioFailedException(
                s"${EOL}Expected ($detail):$EOL$expected${EOL}Actual:$EOL$records")
            }
          case Left(error) =>
            throw ScenarioFailedException(s"Expected: $expected, got error $error")
        }
        ctx

      case (ctx, e @ ExpectError(errorType, phase, detail)) =>
        ctx.lastResult match {
          case Left(error) =>
            if (error.errorType != errorType)
              throw ScenarioFailedException(s"Wrong error type: expected $errorType, got ${error.errorType}")
            if (error.phase != phase)
              throw ScenarioFailedException(s"Wrong error phase: expected $phase, got ${error.phase}")
            if (error.detail != detail)
              throw ScenarioFailedException(s"Wrong error detail: expected $detail, got ${error.detail}")

          case Right(records) =>
            throw ScenarioFailedException(s"Expected: $e, got records $records")
        }

        ctx

      case (ctx, SideEffects(expected)) =>
        val before = ctx.state
        val after = ctx.measure.state
        val diff = before diff after
        if (diff != expected)
          throw ScenarioFailedException(
            s"${EOL}Expected side effects:$EOL$expected${EOL}Actual side effects:$EOL$diff")
        ctx

      case (ctx, Parameters(ps)) =>
        ctx.copy(parameters = ps)

      case (_, step) =>
        throw new UnsupportedOperationException(s"Unsupported step: $step")
    }
  }

  def validate() = {
    // TODO:
    // validate similar to FeatureFormatValidator
    // there should be at least one Execute(_, ExecQuery)
    // there should be either a ExpectResult with ExpectSideEffects or ExpectError
    // etc..
  }

  case class ScenarioExecutionContext(
      graph: Graph,
      lastResult: Result = Right(CypherValueRecords.empty),
      state: State = State(),
      parameters: Map[String, CypherValue] = Map.empty) {

    def execute(query: String, queryType: QueryType): ScenarioExecutionContext = {
      val (g, r) = graph.execute(query, parameters, queryType)
      copy(graph = g, lastResult = r)
    }

    def measure: ScenarioExecutionContext = {
      Try(SideEffectOps.measureState(graph)) match {
        case Success(measuredState) => copy(state = measuredState)
        case Failure(error) =>
          val msg = s"Side effect measurement failed with $error"
          throw ScenarioFailedException(msg)
      }
    }
  }

  case class ScenarioFailedException(msg: String) extends Throwable(s"$self failed with message: $msg")
}
