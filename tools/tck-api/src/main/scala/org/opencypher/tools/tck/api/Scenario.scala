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
package org.opencypher.tools.tck.api

import java.nio.file.Path

import org.junit.jupiter.api.function.Executable
import org.opencypher.tools.tck.SideEffectOps
import org.opencypher.tools.tck.SideEffectOps._
import org.opencypher.tools.tck.api.Graph.Result
import org.opencypher.tools.tck.api.events.TCKEvents
import org.opencypher.tools.tck.api.events.TCKEvents.StepFinished
import org.opencypher.tools.tck.api.events.TCKEvents.StepStarted
import org.opencypher.tools.tck.api.events.TCKEvents.setStepFinished
import org.opencypher.tools.tck.api.events.TCKEvents.setStepStarted
import org.opencypher.tools.tck.values.CypherValue

import scala.compat.Platform.EOL
import scala.language.implicitConversions
import scala.util.Failure
import scala.util.Success
import scala.util.Try

sealed trait ScenarioDiff
case object SourceUnchanged extends ScenarioDiff
case object Unchanged extends ScenarioDiff
case object Moved extends ScenarioDiff
case object Retagged extends ScenarioDiff
case object StepsChanged extends ScenarioDiff
case object SourceChanged extends ScenarioDiff
case object ExampleIndexChanged extends ScenarioDiff
case object PotentiallyDuplicated extends ScenarioDiff
case object PotentiallyRenamed extends ScenarioDiff
case object Different extends ScenarioDiff

case class Scenario(categories: List[String], featureName: String, name: String, exampleIndex: Option[Int], tags: Set[String], steps: List[Step], source: gherkin.pickles.Pickle, sourceFile: Path) {

  self =>

  override def toString = s"""${ categories.mkString("/") } :: "$featureName" :: "$name" ${exampleIndex.map(ix => "#"+ix).getOrElse("")} ${ if (tags.nonEmpty) tags.mkString(" (", " ", ")") else "" })"""

  override def equals(obj: Any): Boolean = {
    obj match {
      case Scenario(thatCategories, thatFeatureName, thatName, thatExampleIndex, thatTags, thatSteps, thatSource, _) =>
        thatCategories == categories &&
        thatFeatureName == featureName &&
        thatName == name &&
        thatExampleIndex == exampleIndex &&
        thatTags == tags &&
        thatSteps == steps &&
        Pickle(thatSource) == Pickle(source)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(categories, featureName, name, exampleIndex, tags, steps, Pickle(source))
    val hash = state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    hash
  }

  def diff(that: Scenario): Set[ScenarioDiff] = that match {
    case Scenario(thatCategories, thatFeatureName, thatName, thatExampleIndex, thatTags, thatSteps, thatSource, _) =>
      val diff = Set[ScenarioDiff](Unchanged, SourceUnchanged, SourceChanged, Moved, Retagged, StepsChanged, ExampleIndexChanged, PotentiallyRenamed).filter {
        case Unchanged => equals(that)
        case SourceUnchanged =>
          equals(that) &&
            Pickle(thatSource, withLocation = true) == Pickle(source, withLocation = true)
        case SourceChanged =>
          equals(that) &&
            Pickle(thatSource, withLocation = true) != Pickle(source, withLocation = true)
        case Moved =>
          (thatCategories != categories || thatFeatureName != featureName) &&
            thatName == name &&
            thatExampleIndex == exampleIndex
        case Retagged =>
          thatName == name &&
            thatExampleIndex == exampleIndex &&
            thatTags != tags
        case StepsChanged =>
          thatName == name &&
            thatExampleIndex == exampleIndex &&
            thatSteps != steps
        case ExampleIndexChanged =>
          thatCategories == categories &&
            thatFeatureName == featureName &&
            thatName == name &&
            thatExampleIndex != exampleIndex &&
            thatTags == tags &&
            thatSteps == steps &&
            Pickle(thatSource) == Pickle(source)
        case PotentiallyRenamed =>
          thatName != name &&
            thatSteps == steps
        case _ => false
      }
      if(diff.isEmpty)
        Set[ScenarioDiff](Different)
      else if(diff subsetOf Set[ScenarioDiff](Moved, Retagged, ExampleIndexChanged, StepsChanged, PotentiallyRenamed))
        diff + PotentiallyDuplicated
      else
        diff
  }

  def apply(graph: => Graph): Executable = new Executable {
    override def execute(): Unit = {
      val g = graph // ensure that lazy parameter is only evaluated once
      try {
        TCKEvents.setScenario(self)
        executeOnGraph(g)
      } finally g.close()
    }
  }

  def executeOnGraph(empty: Graph): Unit = {
    steps.foldLeft(ScenarioExecutionContext(empty)) { (context, step) =>
      {
        val eventId = setStepStarted(StepStarted(step))
        val stepResult: Either[ScenarioFailedException, ScenarioExecutionContext] = (context, step) match {

          case (ctx, Execute(query, qt, _)) =>
            Right(ctx.execute(query, qt))

          case (ctx, Measure(_)) =>
            Right(ctx.measure)

          case (ctx, RegisterProcedure(signature, table, _)) =>
            ctx.graph match {
              case support: ProcedureSupport =>
                support.registerProcedure(signature, table)
              case _ =>
            }
            Right(ctx)

          case (ctx, ExpectResult(expected, _, sorted)) =>
            ctx.lastResult match {
              case Right(records) =>
                val correctResult =
                  if (sorted)
                    expected == records
                  else
                    expected.equalsUnordered(records)

                if (!correctResult) {
                  val detail = if (sorted) "ordered rows" else "in any order of rows"
                  Left(ScenarioFailedException(s"${EOL}Expected ($detail):$EOL$expected${EOL}Actual:$EOL$records"))
                } else {
                  Right(ctx)
                }
              case Left(error) =>
                Left(ScenarioFailedException(s"Expected: $expected, got error $error", error.exception.orNull))
            }

          case (ctx, e @ ExpectError(errorType, phase, detail, _)) =>
            ctx.lastResult match {
              case Left(error) =>
                if (error.errorType != errorType)
                  Left(
                    ScenarioFailedException(
                      s"Wrong error type: expected $errorType, got ${error.errorType}",
                      error.exception.orNull))
                if (error.phase != phase)
                  Left(
                    ScenarioFailedException(
                      s"Wrong error phase: expected $phase, got ${error.phase}",
                      error.exception.orNull))
                if (error.detail != detail)
                  Left(
                    ScenarioFailedException(
                      s"Wrong error detail: expected $detail, got ${error.detail}",
                      error.exception.orNull))
                else {
                  Right(ctx)
                }

              case Right(records) =>
                Left(ScenarioFailedException(s"Expected: $e, got records $records"))
            }

          case (ctx, SideEffects(expected, _)) =>
            val before = ctx.state
            val after = ctx.measure.state
            val diff = before diff after
            if (diff != expected)
              Left(
                ScenarioFailedException(
                  s"${EOL}Expected side effects:$EOL$expected${EOL}Actual side effects:$EOL$diff"))
            else Right(ctx)

          case (ctx, Parameters(ps, _)) =>
            Right(ctx.copy(parameters = ps))

          case (ctx, _: Dummy) => Right(ctx)
          case (_, s) =>
            throw new UnsupportedOperationException(s"Unsupported step: $s")
        }
        stepResult match {
          case Right(ctx) =>
            setStepFinished(StepFinished(step, Right(ctx.lastResult), eventId))
            ctx
          case Left(throwable) =>
            setStepFinished(StepFinished(step, Left(throwable), eventId))
            throw throwable
        }
      }
    }
  }

  def validate(): Unit = {
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
          throw ScenarioFailedException(msg, error)
      }
    }
  }

  case class ScenarioFailedException(msg: String, cause: Throwable = null)
      extends Throwable(s"$self failed with message: $msg", cause)
}
