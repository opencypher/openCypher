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
import org.opencypher.tools.tck.values.CypherValue

import scala.compat.Platform.EOL
import scala.language.implicitConversions

case class Scenario(featureName: String, name: String, steps: List[Step]) extends (Graph => Executable) {
  override def toString() = s"""Feature "$featureName": Scenario "$name""""

  override def apply(graph: Graph): Executable = new Executable {
    override def execute(): Unit = executeOnGraph(graph)
  }

  def executeOnGraph(empty: Graph): Unit = {
    steps.foldLeft(ScenarioContext(empty)) {

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
        val success = if (sorted) {
          expected == ctx.lastResult
        } else {
          expected.equalsUnordered(ctx.lastResult.asRecords)
        }
        if (!success) {
          val detail = if (sorted) "ordered rows" else "in any order of rows"
          throw new ScenarioFailedException(s"${EOL}Expected ($detail):$EOL$expected${EOL}Actual:$EOL${ctx.lastResult}")
        }
        ctx

      case (ctx, ExpectError(errorType, phase, detail)) =>
        val error = ctx.lastResult.asError

        if (error.errorType != errorType)
          throw new ScenarioFailedException(s"Wrong error type: expected $errorType, got ${error.errorType}")
        if (error.phase != phase)
          throw new ScenarioFailedException(s"Wrong error phase: expected $phase, got ${error.phase}")
        if (error.detail != detail)
          throw new ScenarioFailedException(s"Wrong error detail: expected $detail, got ${error.detail}")

        ctx

      case (ctx, SideEffects(expected)) =>
        val before = ctx.state
        val after = ctx.measure.state
        val diff = before diff after
        if (diff != expected)
          throw new ScenarioFailedException(
            s"${EOL}Expected side effects:$EOL$expected${EOL}Actual side effects:$EOL$diff")
        ctx

      case (_, step) =>
        throw new UnsupportedOperationException(s"Unsupported step: $step")
    }
  }

  implicit def toCypherValueRecords(t: (Graph, Result)): (Graph, CypherValueRecords) =
    t._1 -> t._2.asRecords

  def validate() = {
    // TODO:
    // validate similar to FeatureFormatValidator
    // there should be at least one Execute(_, ExecQuery)
    // there should be either a ExpectResult with ExpectSideEffects or ExpectError
    // etc..
  }
}

case class ScenarioContext(
    graph: Graph,
    lastResult: Result = CypherValueRecords.empty,
    state: State = State(),
    parameters: Map[String, CypherValue] = Map.empty) {

  def execute(query: String, queryType: QueryType): ScenarioContext = {
    val (g, r) = graph.execute(query, parameters, queryType)
    copy(graph = g, lastResult = r)
  }

  def measure: ScenarioContext = {
    copy(state = SideEffectOps.measureState(graph))
  }
}

class ScenarioFailedException(msg: String) extends Throwable(msg)
