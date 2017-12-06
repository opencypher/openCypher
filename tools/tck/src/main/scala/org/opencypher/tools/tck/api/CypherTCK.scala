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
import org.opencypher.tools.tck.values.CypherValue

import scala.collection.JavaConverters._
import scala.io.Source

object CypherTCK {

  val featuresPath = "/features"
  val featureSuffix = ".feature"

  private lazy val parser = new Parser[GherkinDocument](new AstBuilder)
  private lazy val matcher = new TokenMatcher

  def allTckScenarios: Seq[Feature] = parseClasspathFeatures(featuresPath)

  private[tck] def allTckScenariosFromFilesystem = {
    parseFilesystemFeatures(new File(getClass.getResource(featuresPath).toURI))
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
    parseFeature(Source.fromFile(file).mkString)
  }

  def parseClasspathFeature(pathUrl: URL): Feature = {
    parseFeature(Source.fromURL(pathUrl).mkString)
  }

  def parseFeature(featureString: String): Feature = {
    val gherkinDocument = parser.parse(featureString, matcher)
    val compiler = new Compiler
    val pickles = compiler.compile(gherkinDocument).asScala
    val featureName = gherkinDocument.getFeature.getName
    val scenarios = pickles.map(toScenario(featureName, _))
    Feature(scenarios)
  }

  def toScenario(featureName: String, pickle: Pickle): Scenario = {
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
        case namedGraphR(name) => List(Execute(NamedGraphs.graphs(name)))
        case anyGraphR() => List(Execute(NamedGraphs.graphs.values.head))

        // And
        case initQueryR() => List(Execute(queryFromStep))
        case parametersR() => List(Parameters(parseParameters))
        case installedProcedureR(signature) => List(RegisterProcedure(signature, parseTable()))

        // When
        case executingQueryR() => List(Measure, Execute(queryFromStep))
        case executingControlQueryR() => List(Execute(queryFromStep))

        // Then
        case expectEmptyResultR() => List(ExpectResult(CypherValueRecords.empty))
        case expectResultR() => List(ExpectResult(parseTable()))
        case expectSortedResultR() => List(ExpectResult(parseTable(), sorted = true))
        case expectResultUnorderedListsR() => List(ExpectResult(parseTable(orderedLists = false)))
        case expectErrorR(errorType, time, detail) => List(ExpectError(errorType, time, detail))

        // And
        case noSideEffectsR() => List(SideEffects())
        case sideEffectsR() => List(SideEffects(parseSideEffectsTable))

        // Unsupported step
        case other => throw new UnsupportedOperationException(s"Unsupported step: $other")
      }
      scenarioSteps
    }.toList
    Scenario(featureName, pickle.getName, steps)
  }

}

case class Feature(scenarios: Seq[Scenario])

sealed trait Step

case class SideEffects(expected: Diff = Diff()) extends Step

case object Measure extends Step

case class RegisterProcedure(signature: String, values: CypherValueRecords) extends Step

case class Parameters(values: Map[String, CypherValue]) extends Step

case class Execute(query: String) extends Step

case class ExpectResult(expectedResult: CypherValueRecords, sorted: Boolean = false) extends Step

case class ExpectError(errorType: String, time: String, detail: String) extends Step
