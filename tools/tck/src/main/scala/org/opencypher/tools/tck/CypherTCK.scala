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
package org.opencypher.tools.tck

import java.io.File
import java.net.URL
import java.nio.file.{FileSystems, Files, Paths}
import java.util.HashMap

import gherkin.ast.GherkinDocument
import gherkin.pickles.{Compiler, Pickle, PickleString, PickleTable}
import gherkin.{AstBuilder, Parser, TokenMatcher}
import org.opencypher.tools.tck.constants.TCKStepDefinitions._

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
    FileSystems.newFileSystem(resource, new HashMap[String, String]) // Needed to support `Paths.get` below
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

      def parseTable: Records = {
        require(step.getArgument.size == 1)
        val rows = stepArguments.head.asInstanceOf[PickleTable].getRows.asScala
        val headerRow = rows.head
        val values = rows.tail
        val expectedRows = values.map { v =>
          v.getCells.asScala
            .zip(headerRow.getCells.asScala)
            .map {
              case (value, name) =>
                name.getValue -> value.getValue
            }
            .toMap
        }.toList
        val expectedHeader = headerRow.getCells.asScala.toList.map(_.getValue)
                Records(expectedHeader, expectedRows)
        Records.fromRows(expectedHeader, expectedRows)
      }

      val scenarioStep = step.getText match {
        // Given
        case emptyGraphR() => None
        case namedGraphR(name) => Some(Execute(NamedGraphs.graphs(name)))
        case anyGraphR() => Some(Execute(NamedGraphs.graphs.values.head))

        // And
        case initQueryR() => Some(Execute(queryFromStep))
        case parametersR() => None // TODO: Parse params
        case installedProcedureR(signature) => None // TODO: Hairy

        // When
        case executingQueryR() => Some(Execute(queryFromStep))
        case executingControlQueryR() => Some(Execute(queryFromStep))

        // Then
        case expectEmptyResultR() => Some(ExpectUnorderedResult(Records.empty))
        case expectResultR() => Some(ExpectUnorderedResult(parseTable))
        case expectSortedResultR() => Some(ExpectOrderedResult(parseTable))
        case expectResultUnorderedListsR() => None
        case expectErrorR(errorType, time, detail) => Some(ExpectError(errorType, time, detail))

        // And
        case noSideEffectsR() => Some(NoSideEffects)
        case sideEffectsR() => None

        // Unsupported step
        case other => throw new UnsupportedOperationException(s"Unsupported step: $other")
      }
      scenarioStep
    }.toList
    Scenario(featureName, pickle.getName, steps)
  }

}

case class Feature(scenarios: Seq[Scenario])

trait Step

case object NoSideEffects extends Step

case class Execute(query: String) extends Step

trait Expect extends Step

trait ExpectResult extends Expect {
  def eval(actualResult: Records): Unit
}

case class ExpectOrderedResult(expectedResult: Records) extends ExpectResult {
  override def eval(actualResult: Records): Unit = {
    assert(actualResult == expectedResult)
  }
}

case class ExpectUnorderedResult(expectedResult: Records) extends ExpectResult {
  override def eval(actualResult: Records): Unit = {
    assert(expectedResult.equalsUnordered(actualResult))
  }
}

case class ExpectError(errorType: String, time: String, detail: String) extends Expect
