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

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file._
import java.util
import java.util.function.Predicate

import gherkin.ast.GherkinDocument
import gherkin.pickles.{Compiler, Pickle, PickleRow, PickleStep, PickleString, PickleTable}
import gherkin.{AstBuilder, Parser, TokenMatcher}
import org.opencypher.tools.tck.SideEffectOps.Diff
import org.opencypher.tools.tck._
import org.opencypher.tools.tck.api.events.TCKEvents
import org.opencypher.tools.tck.api.events.TCKEvents.FeatureRead
import org.opencypher.tools.tck.constants.TCKStepDefinitions._
import org.opencypher.tools.tck.constants.{TCKErrorDetails, TCKErrorPhases, TCKErrorTypes}
import org.opencypher.tools.tck.values.CypherValue

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object CypherTCK {

  val featuresPath = "/features"
  val featureSuffix = ".feature"

  private lazy val parser = new Parser[GherkinDocument](new AstBuilder)
  private lazy val matcher = new TokenMatcher

  /**
    * Provides all the scenarios in the openCypher TCK.
    *
    * @note While each scenario is unique, several scenarios could have the same name.
    *       This happens when the scenario is generated from a Gherkin Scenario Outline, in which case
    *       all variants of the Scenario Outline get the same name.
    */
  def allTckScenarios: Seq[Scenario] = parseClasspathFeatures(featuresPath).flatMap(_.scenarios)

  @deprecated("use allTckScenarios instead")
  def allTckScenariosFromFilesystem: Seq[Scenario] = {
    allTckScenarios
  }

  def parseClasspathFeatures(path: String): Seq[Feature] = {
    parseFeatures(getClass.getResource(path).toURI)
  }

  def parseFeatures(resource: URI): Seq[Feature] = {
    val fs =
      if ("jar".equalsIgnoreCase(resource.getScheme)) {
        Some(FileSystems.newFileSystem(resource, new util.HashMap[String, String]))
      } else None
    val directoryPath: Path = Paths.get(resource)
    try {
      val featurePaths = Files.walk(directoryPath).filter {
        (t: Path) => Files.isRegularFile(t) && t.toString.endsWith(featureSuffix)
      }
      // Note that converting to list is necessary to cut off lazy evaluation
      // otherwise evaluation of parsePathFeature will happen after the file system is already closed
      featurePaths.iterator().asScala.toList.map(parsePathFeature)
    } finally {
      try {
        fs.foreach(_.close())
      } catch {
        case _: UnsupportedOperationException => Unit
      }
    }
  }

  def parsePathFeature(featureFile: Path): Feature = {
    val featureString = new String(Files.readAllBytes(featureFile), StandardCharsets.UTF_8)
    parseFeature(featureFile.toAbsolutePath.toString, featureString)
  }

  def parseFeature(source: String, featureString: String): Feature = {
    val gherkinDocument = Try(parser.parse(featureString, matcher)) match {
      case Success(doc) => doc
      case Failure(error) =>
        throw InvalidFeatureFormatException(s"Could not parse feature from $source: ${error.getMessage}")
    }
    val compiler = new Compiler
    val pickles = compiler.compile(gherkinDocument).asScala
    // filters out scenarios with @ignore
    val included = pickles.filterNot(tagNames(_) contains "@ignore")
    val featureName = gherkinDocument.getFeature.getName
    val scenarios = included.map(toScenario(featureName, _))
    TCKEvents.setFeature(FeatureRead(featureName, source, featureString))
    Feature(scenarios)
  }

  private def toScenario(featureName: String, pickle: Pickle): Scenario = {

    val tags = tagNames(pickle)
    val shouldValidate = !tags.contains("@allowCustomErrors")

    val steps = pickle.getSteps.asScala.flatMap { step =>
      def stepArguments = step.getArgument.asScala

      def queryFromStep: String = {
        require(stepArguments.size == 1)
        stepArguments.head.asInstanceOf[PickleString].getContent
      }

      def parseTable(orderedLists: Boolean = true): CypherValueRecords = {
        require(step.getArgument.size == 1)
        val rows = stepArguments.head.asInstanceOf[PickleTable].getRows.asScala
        val header = cellValues(rows.head)
        val values = rows.tail
        val expectedRows = values.map { row =>
          header
            .zip(cellValues(row))
            .toMap
        }.toList
        CypherValueRecords.fromRows(header, expectedRows, orderedLists)
      }

      def cellValues(row: PickleRow): List[String] =
        row.getCells.asScala.map(_.getValue).toList

      def parseSideEffectsTable: Diff = {
        Diff(parseMap(_.toInt))
      }

      def parseParameters: Map[String, CypherValue] = {
        parseMap(CypherValue(_))
      }

      def parseMap[V](parseValue: String => V): Map[String, V] = {
        require(step.getArgument.size == 1)
        val rows = stepArguments.head.asInstanceOf[PickleTable].getRows.asScala
        rows.map { row =>
          val sideEffect = cellValues(row)
          require(sideEffect.length == 2)
          sideEffect.head -> parseValue(sideEffect.tail.head)
        }.toMap
      }

      val scenarioSteps: List[Step] = step.getText match {
        // Given
        case emptyGraphR()     => List(Dummy(step))
        case namedGraphR(name) => List(Execute(NamedGraphs.graphs(name), InitQuery, step))
        case anyGraphR()       => List(Execute(NamedGraphs.graphs.values.head, InitQuery, step))

        // And
        case initQueryR()                   => List(Execute(queryFromStep, InitQuery, step))
        case parametersR()                  => List(Parameters(parseParameters, step))
        case installedProcedureR(signature) => List(RegisterProcedure(signature, parseTable(), step))

        // When
        case executingQueryR()        => List(Measure(step), Execute(queryFromStep, ExecQuery, step))
        case executingControlQueryR() => List(Execute(queryFromStep, ExecQuery, step))

        // Then
        case expectEmptyResultR()          => List(ExpectResult(CypherValueRecords.empty, step))
        case expectResultR()               => List(ExpectResult(parseTable(), step))
        case expectSortedResultR()         => List(ExpectResult(parseTable(), step, sorted = true))
        case expectResultUnorderedListsR() => List(ExpectResult(parseTable(orderedLists = false), step))
        case expectErrorR(errorType, time, detail) =>
          val expectedError = ExpectError(errorType, time, detail, step)
          if (shouldValidate) {
            expectedError.validate() match {
              case None => // No problem
              case Some(errorMessage) =>
                throw InvalidFeatureFormatException(
                  s"""Invalid error format in scenario "${pickle.getName}" from feature "$featureName":
                    $errorMessage
                    If this is a custom error, then disable this validation with tag "@allowCustomErrors"""")
            }
          }
          List(expectedError, SideEffects(source = step).fillInZeros)

        // And
        case noSideEffectsR() => List(SideEffects(source = step).fillInZeros)
        case sideEffectsR()   => List(SideEffects(parseSideEffectsTable, step).fillInZeros)

        // Unsupported step
        case other =>
          throw InvalidFeatureFormatException(
            s"""Unsupported step: $other in scenario "${pickle.getName}" from feature "$featureName"""")
      }
      scenarioSteps
    }.toList
    Scenario(featureName, pickle.getName, tags, steps, pickle)
  }

  private def tagNames(pickle: Pickle): Set[String] = pickle.getTags.asScala.map(_.getName).toSet

}

case class Feature(scenarios: Seq[Scenario])

sealed trait Step {
  val source: PickleStep
}

case class SideEffects(expected: Diff = Diff(), source: PickleStep) extends Step {
  def fillInZeros: SideEffects = copy(expected = expected.fillInZeros)
}

case class Measure(source: PickleStep) extends Step

case class Dummy(source: PickleStep) extends Step

case class RegisterProcedure(signature: String, values: CypherValueRecords, source: PickleStep) extends Step

case class Parameters(values: Map[String, CypherValue], source: PickleStep) extends Step

case class Execute(query: String, qt: QueryType, source: PickleStep) extends Step

case class ExpectResult(expectedResult: CypherValueRecords, source: PickleStep, sorted: Boolean = false) extends Step

case class ExpectError(errorType: String, phase: String, detail: String, source: PickleStep) extends Step {
  // Returns None if valid and Some("error message") otherwise.
  def validate(): Option[String] = {
    if (!TCKErrorTypes.ALL.contains(errorType)) {
      Some(s"invalid TCK error type: $errorType, valid ones are ${TCKErrorTypes.ALL.mkString("{ ", ", ", " }")}")
    } else if (!TCKErrorPhases.ALL.contains(phase)) {
      Some(s"invalid TCK error phase: $phase, valid ones are ${TCKErrorPhases.ALL.mkString("{ ", ", ", " }")}")
    } else if (!TCKErrorDetails.ALL.contains(detail)) {
      Some(s"invalid TCK error detail: $detail, valid ones are ${TCKErrorDetails.ALL.mkString("{ ", ", ", " }")}")
    } else {
      None
    }
  }
}

case class InvalidFeatureFormatException(message: String) extends RuntimeException(message)

sealed trait QueryType

case object InitQuery extends QueryType

case object ExecQuery extends QueryType

case object SideEffectQuery extends QueryType
