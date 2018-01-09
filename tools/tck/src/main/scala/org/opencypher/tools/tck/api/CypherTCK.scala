/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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

import java.io.File
import java.net.URL
import java.nio.file.{FileSystems, Files, Paths}
import java.util

import gherkin.ast.GherkinDocument
import gherkin.pickles.{Compiler, Pickle, PickleRow, PickleString, PickleTable}
import gherkin.{AstBuilder, Parser, TokenMatcher}
import org.opencypher.tools.tck.SideEffectOps.Diff
import org.opencypher.tools.tck._
import org.opencypher.tools.tck.constants.TCKStepDefinitions._
import org.opencypher.tools.tck.constants.{TCKErrorDetails, TCKErrorPhases, TCKErrorTypes}
import org.opencypher.tools.tck.values.CypherValue

import scala.collection.JavaConverters._
import scala.io.Source
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

  def allTckScenariosFromFilesystem: Seq[Scenario] = {
    parseFilesystemFeatures(new File(getClass.getResource(featuresPath).toURI)).flatMap(_.scenarios)
  }

  def parseClasspathFeatures(path: String): Seq[Feature] = {
    val resource = getClass.getResource(path).toURI
    FileSystems.newFileSystem(resource, new util.HashMap[String, String]) // Needed to support `Paths.get` below
    val directoryPath = Paths.get(resource)
    val paths = Files.newDirectoryStream(directoryPath).asScala.toSeq
    val featurePathStrings = paths.map(path => path.toString).filter(_.endsWith(featureSuffix))
    val featureUrls = featurePathStrings.map(getClass.getResource(_))
    featureUrls.map(parseClasspathFeature)
  }

  def parseFilesystemFeatures(directory: File): Seq[Feature] = {
    require(directory.isDirectory)
    val featureFileNames = directory.listFiles.filter(_.getName.endsWith(featureSuffix))
    featureFileNames.map(parseFilesystemFeature)
  }

  def parseFilesystemFeature(file: File): Feature = {
    parseFeature(file.getAbsolutePath, Source.fromFile(file).mkString)
  }

  def parseClasspathFeature(pathUrl: URL): Feature = {
    parseFeature(pathUrl.toString, Source.fromURL(pathUrl).mkString)
  }

  def parseFeature(source: String, featureString: String): Feature = {
    val gherkinDocument = Try(parser.parse(featureString, matcher)) match {
      case Success(doc) => doc
      case Failure(error) =>
        throw InvalidFeatureFormatException(
          s"Could not parse feature from $source: ${error.getMessage}")
    }
    val compiler = new Compiler
    val pickles = compiler.compile(gherkinDocument).asScala
    // filters out scenarios with @ignore
    val included = pickles.filterNot(tagNames(_) contains "@ignore")
    val featureName = gherkinDocument.getFeature.getName
    val scenarios = included.map(toScenario(featureName, _))
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

      def parseMap[V](parseValue: (String => V)): Map[String, V] = {
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
        case emptyGraphR() => List.empty
        case namedGraphR(name) => List(Execute(NamedGraphs.graphs(name), InitQuery))
        case anyGraphR() => List(Execute(NamedGraphs.graphs.values.head, InitQuery))

        // And
        case initQueryR() => List(Execute(queryFromStep, InitQuery))
        case parametersR() => List(Parameters(parseParameters))
        case installedProcedureR(signature) => List(RegisterProcedure(signature, parseTable()))

        // When
        case executingQueryR() => List(Measure, Execute(queryFromStep, ExecQuery))
        case executingControlQueryR() => List(Execute(queryFromStep, ExecQuery))

        // Then
        case expectEmptyResultR() => List(ExpectResult(CypherValueRecords.empty))
        case expectResultR() => List(ExpectResult(parseTable()))
        case expectSortedResultR() => List(ExpectResult(parseTable(), sorted = true))
        case expectResultUnorderedListsR() => List(ExpectResult(parseTable(orderedLists = false)))
        case expectErrorR(errorType, time, detail) =>
          val expectedError = ExpectError(errorType, time, detail)
          if (shouldValidate) {
            expectedError.validate match {
              case None => // No problem
              case Some(errorMessage) =>
                throw new InvalidFeatureFormatException(
                  s"""Invalid error format in scenario "${pickle.getName}" from feature "$featureName":
                    $errorMessage
                    If this is a custom error, then disable this validation with tag "@allowCustomErrors"""")
            }
          }
          List(expectedError, SideEffects().fillInZeros)

        // And
        case noSideEffectsR() => List(SideEffects().fillInZeros)
        case sideEffectsR() => List(SideEffects(parseSideEffectsTable).fillInZeros)

        // Unsupported step
        case other =>
          throw InvalidFeatureFormatException(
            s"""Unsupported step: $other in scenario "${pickle.getName}" from feature "$featureName"""")
      }
      scenarioSteps
    }.toList
    Scenario(featureName, pickle.getName, tags, steps)
  }

  private def tagNames(pickle: Pickle): Set[String] = pickle.getTags.asScala.map(_.getName).toSet

}

case class Feature(scenarios: Seq[Scenario])

sealed trait Step

case class SideEffects(expected: Diff = Diff()) extends Step {
  def fillInZeros: SideEffects = copy(expected = expected.fillInZeros)
}

case object Measure extends Step

case class RegisterProcedure(signature: String, values: CypherValueRecords) extends Step

case class Parameters(values: Map[String, CypherValue]) extends Step

case class Execute(query: String, qt: QueryType) extends Step

case class ExpectResult(expectedResult: CypherValueRecords, sorted: Boolean = false) extends Step

case class ExpectError(errorType: String, phase: String, detail: String) extends Step {
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

sealed trait QueryType

case object InitQuery extends QueryType

case object ExecQuery extends QueryType

case object SideEffectQuery extends QueryType
