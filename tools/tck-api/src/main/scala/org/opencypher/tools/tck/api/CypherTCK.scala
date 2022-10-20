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
package org.opencypher.tools.tck.api

import io.cucumber.core.gherkin
import io.cucumber.core.gherkin.DataTableArgument
import io.cucumber.core.gherkin.DocStringArgument
import io.cucumber.core.gherkin.vintage.GherkinVintageFeatureParser
import org.opencypher.tools.tck.SideEffectOps.Diff
import org.opencypher.tools.tck._
import org.opencypher.tools.tck.api.events.TCKEvents
import org.opencypher.tools.tck.api.events.TCKEvents.FeatureRead
import org.opencypher.tools.tck.constants.TCKErrorDetails
import org.opencypher.tools.tck.constants.TCKErrorPhases
import org.opencypher.tools.tck.constants.TCKErrorTypes
import org.opencypher.tools.tck.constants.TCKStepDefinitions._
import org.opencypher.tools.tck.constants.TCKTags
import org.opencypher.tools.tck.values.CypherValue

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file._
import java.util
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object CypherTCK {

  val featuresPath = "/features"
  val featureSuffix = ".feature"

  private lazy val parser = new GherkinVintageFeatureParser()

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

  def allTckScenariosFromClasspath(path: String): Seq[Scenario] = {
    parseClasspathFeatures(path).flatMap(_.scenarios)
  }

  def allTckScenariosFromFilesystem(path: String): Seq[Scenario] = {
    parseFilesystemFeatures(path).flatMap(_.scenarios)
  }

  def parseClasspathFeatures(path: String): Seq[Feature] = {
    parseFeatures(getClass.getResource(path).toURI)
  }

  def parseFilesystemFeatures(path: String): Seq[Feature] = {
    parseFeatures(new java.io.File(path).toURI)
  }

  def parseFeatures(resource: URI): Seq[Feature] = {
    val fs =
      if ("jar".equalsIgnoreCase(resource.getScheme)) {
        Some(FileSystems.newFileSystem(resource, new util.HashMap[String, String]))
      } else None
    val directoryPath: Path = Paths.get(resource)
    try {
      val featurePaths = Files.walk(directoryPath).filter {
        t: Path => Files.isRegularFile(t) && t.toString.endsWith(featureSuffix)
      }
      // Note that converting to list is necessary to cut off lazy evaluation
      // otherwise evaluation of parsePathFeature will happen after the file system is already closed
      featurePaths.iterator().asScala.toList.map(parsePathFeature(_, directoryPath))
    } finally {
      try {
        fs.foreach(_.close())
      } catch {
        case _: UnsupportedOperationException => ()
      }
    }
  }

  def parsePathFeature(featureFile: Path, directoryPath: Path): Feature = {
    val featureString = new String(Files.readAllBytes(featureFile), StandardCharsets.UTF_8)
    val relativeFeaturePath = directoryPath.relativize(featureFile.getParent)
    val categories = (0 until relativeFeaturePath.getNameCount)
      .map(index => relativeFeaturePath.getName(index).toString)
      .filter(_.nonEmpty)
      .toList
    parseFeature(featureFile, featureString, categories)
  }

  def parseFeature(featureFile: Path, featureString: String, categories: Seq[String]): Feature = {

    case class NameExtractedPickle(pickle: gherkin.Pickle, nameAndNumber: String, exampleName: Option[String])

    object parsePickleName extends (gherkin.Pickle => NameExtractedPickle) {
      private val exampleNamePattern = "^(((?! #Example: ).)+)(?: #Example: (.*)?)?$".r

      def apply(pickle: gherkin.Pickle): NameExtractedPickle = pickle.getName match {
        case exampleNamePattern(nameAndNumber, _, null) => NameExtractedPickle(pickle, nameAndNumber, None)
        case exampleNamePattern(nameAndNumber, _, exampleName) => NameExtractedPickle(pickle, nameAndNumber, Some(exampleName))
        case _ => NameExtractedPickle(pickle, pickle.getName, None)
      }
    }

    case class PickleGroupingKey(keyword: String, pickleName: String)

    object formPickleGroupingKey extends (NameExtractedPickle => PickleGroupingKey) {
      def apply(pickle: NameExtractedPickle): PickleGroupingKey = PickleGroupingKey(pickle.pickle.getKeyword, pickle.nameAndNumber)
    }

    Try(parser.parse(featureFile.toUri, featureString, null)) match {
      case Success(featureOption) =>
        if(featureOption.isPresent) {
          val feature = featureOption.get()
          val pickles = feature.getPickles.asScala.toList
          // filters out scenarios with TCKTags.IGNORE
          val included = pickles.filterNot(tagNames(_) contains TCKTags.IGNORE)

          val includedWithExtractedName = included.map(parsePickleName)
          val includedGroupedByKeywordAndName = includedWithExtractedName.groupBy(formPickleGroupingKey)
          val includedGroupedAndSorted = includedGroupedByKeywordAndName.transform {
            case (_, included) => included.sortBy(_.pickle.getLocation.getLine)
          }

          val featureName = feature.getName
          val scenarios = includedGroupedAndSorted.flatMap {
            case (PickleGroupingKey("Scenario Outline", _), pickles) =>
                pickles.zipWithIndex.map {
                  case (pickle, exampleIndex) => toScenario(categories, featureName, pickle.nameAndNumber, Some(exampleIndex), pickle.exampleName, pickle.pickle, featureFile)
                }
            case (PickleGroupingKey("Scenario", _), pickles) =>
                pickles.map(pickle => toScenario(categories, featureName, pickle.nameAndNumber, None, pickle.exampleName, pickle.pickle, featureFile))
            case _ => Seq[Scenario]()
          }.toSeq
          TCKEvents.setFeature(FeatureRead(featureName, featureFile.toUri, featureString))
          Feature(scenarios)
        }
        else Feature(Seq[Scenario]())
      case Failure(error) =>
        throw InvalidFeatureFormatException(s"Could not parse feature from ${featureFile.toAbsolutePath.toString}: ${error.getMessage}")
    }
  }

  private val scenarioNumberPattern = "^\\Q[\\E([0-9]+)\\Q]\\E (.+)$".r

  private def parseNameAndNumber(nameAndNumber: String): (String, Option[Int]) = {
    nameAndNumber match {
      case scenarioNumberPattern(number, name) => (name, Some(number.toInt))
      case _ => (nameAndNumber, None)
    }
  }

  private def toScenario(categories: Seq[String], featureName: String, nameAndNumber: String, exampleIndex: Option[Int], exampleName: Option[String], pickle: io.cucumber.core.gherkin.Pickle, sourceFile: Path): Scenario = {

    val tags = tagNames(pickle)
    val shouldValidate = !tags.contains(TCKTags.ALLOW_CUSTOM_ERRORS)

    val steps = pickle.getSteps.asScala.flatMap { step =>
      def stepArgument = step.getArgument

      def queryFromStep: String = {
        stepArgument.asInstanceOf[DocStringArgument].getContent
      }

      def getDataTableRowsFromArgument: List[List[String]] = stepArgument.asInstanceOf[DataTableArgument].cells().asScala.map(_.asScala.toList).toList

      def parseTable(orderedLists: Boolean = true): CypherValueRecords = {
        val rows = getDataTableRowsFromArgument
        val header = rows.head
        val values = rows.tail
        val expectedRows = values.map { row =>
          header
            .zip(row)
            .toMap
        }
        CypherValueRecords.fromRows(header, expectedRows, orderedLists)
      }

      def parseSideEffectsTable: Diff = {
        Diff(parseMap(_.toInt))
      }

      def parseParameters: Map[String, CypherValue] = {
        parseMap(CypherValue(_))
      }

      def parseMap[V](parseValue: String => V): Map[String, V] = {
        getDataTableRowsFromArgument.map { sideEffect =>
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
        case csvFileR(urlParameter)         => List(CsvFile(urlParameter, parseTable(), step))

        // When
        case executingQueryR()        => List(Measure(step), Execute(queryFromStep, ExecQuery, step))
        case executingControlQueryR() => List(Execute(queryFromStep, ControlQuery, step))

        // Then
        case expectEmptyResultR()          => List(ExpectResult(CypherValueRecords.empty, step))
        case expectResultR()               => List(ExpectResult(parseTable(), step))
        case expectSortedResultR()         => List(ExpectResult(parseTable(), step, sorted = true))
        case expectResultUnorderedListsR() => List(ExpectResult(parseTable(orderedLists = false), step))
        case expectSortedResultUnorderedListsR() => List(ExpectResult(parseTable(orderedLists = false), step, sorted = true))
        case expectErrorR(errorType, time, detail) =>
          val expectedError = ExpectError(errorType, time, detail, step)
          if (shouldValidate) {
            expectedError.validate() match {
              case None => // No problem
              case Some(errorMessage) =>
                throw InvalidFeatureFormatException(
                  s"""Invalid error format in scenario "${pickle.getName}" from feature "$featureName":
                    $errorMessage
                    If this is a custom error, then disable this validation with tag "${TCKTags.ALLOW_CUSTOM_ERRORS}"""")
            }
          }
          List(expectedError)

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

    val transformedSteps = insertSideEffectsOnExpectError(steps)

    val (name, number) = parseNameAndNumber(nameAndNumber)
    val tagsInferred = tags ++ Set(TCKTags.NEGATIVE_TEST, TCKTags.WILDCARD_ERROR_DETAILS).filter {
      case TCKTags.NEGATIVE_TEST => transformedSteps.exists {
        case _: ExpectError => true
        case _ => false
      }
      case TCKTags.WILDCARD_ERROR_DETAILS => transformedSteps.exists {
        case ExpectError(TCKErrorTypes.ERROR, _, _, _) => true
        case ExpectError(_, TCKErrorPhases.ANY_TIME, _, _) => true
        case ExpectError(_, _, TCKErrorDetails.ANY, _) => true
        case _ => false
      }
      case _ => false
    }
    Scenario(categories.toList, featureName, number, name, exampleIndex, exampleName, tagsInferred, transformedSteps, pickle, sourceFile)
  }

  private def tagNames(pickle: io.cucumber.core.gherkin.Pickle): Set[String] = pickle.getTags.asScala.toSet

  private def insertSideEffectsOnExpectError(originalSteps: List[Step]): List[Step] = {
    @tailrec
    def recurse(steps: List[Step], done: ListBuffer[Step]): List[Step] = steps match {
      case (_: ExpectError) :: (_: SideEffects) :: _ => originalSteps // We already have side effects
      case (expectError: ExpectError) :: tail =>
        // Insert empty side effects after expect error
        val sideEffects = SideEffects(source = expectError.source).fillInZeros
        (done ++= (expectError :: sideEffects :: tail)).toList
      case head :: tail => recurse(tail, done += head)
      case _ => originalSteps
    }
    recurse(originalSteps, ListBuffer.empty)
  }
}

case class Feature(scenarios: Seq[Scenario])

sealed trait Step {
  val source: io.cucumber.core.gherkin.Step
}

case class SideEffects(expected: Diff = Diff(), source: io.cucumber.core.gherkin.Step) extends Step {
  def fillInZeros: SideEffects = copy(expected = expected.fillInZeros)

  override def equals(obj: Any): Boolean = {
    obj match {
      case SideEffects(thatExpected, thatSource) =>
        thatExpected == expected &&
        PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(expected, PickleStep(source))
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class Measure(source: io.cucumber.core.gherkin.Step) extends Step {
  override def equals(obj: Any): Boolean = {
    obj match {
      case Measure(thatSource) => PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = PickleStep(source).hashCode()
}

case class Dummy(source: io.cucumber.core.gherkin.Step) extends Step {
  override def equals(obj: Any): Boolean = {
    obj match {
      case Dummy(thatSource) => PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = PickleStep(source).hashCode()
}

case class RegisterProcedure(signature: String, values: CypherValueRecords, source: io.cucumber.core.gherkin.Step) extends Step {
  override def equals(obj: Any): Boolean = {
    obj match {
      case RegisterProcedure(thatSignature, thatValues, thatSource) =>
        thatSignature == signature &&
        thatValues == values &&
        PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(signature, values, PickleStep(source))
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class CsvFile(urlParameter: String, table: CypherValueRecords, source: io.cucumber.core.gherkin.Step) extends Step {
  override def equals(obj: Any): Boolean = {
    obj match {
      case CsvFile(thatParameter, thatTable, thatSource) =>
        thatParameter == urlParameter &&
          thatTable == table &&
          PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(urlParameter, table, PickleStep(source))
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class Parameters(values: Map[String, CypherValue], source: io.cucumber.core.gherkin.Step) extends Step {
  override def equals(obj: Any): Boolean = {
    obj match {
      case Parameters(thatValues, thatSource) =>
          thatValues == values &&
          PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(values, PickleStep(source))
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class Execute(query: String, qt: QueryType, source: io.cucumber.core.gherkin.Step) extends Step {
  override def equals(obj: Any): Boolean = {
    obj match {
      case Execute(thatQuery, thatQt, thatSource) =>
        thatQuery == query &&
        thatQt == qt &&
        PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(query, qt, PickleStep(source))
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class ExpectResult(expectedResult: CypherValueRecords, source: io.cucumber.core.gherkin.Step, sorted: Boolean = false) extends Step {
  override def equals(obj: Any): Boolean = {
    obj match {
      case ExpectResult(thatExpectedResult, thatSource, thatSorted) =>
        thatExpectedResult == expectedResult &&
        PickleStep(thatSource) == PickleStep(source) &&
        thatSorted == sorted
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(expectedResult, PickleStep(source), sorted)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class ExpectError(errorType: String, phase: String, detail: String, source: io.cucumber.core.gherkin.Step) extends Step {
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

  override def equals(obj: Any): Boolean = {
    obj match {
      case ExpectError(thatErrorType, thatPhase, thatDetail, thatSource) =>
        thatErrorType == errorType &&
        thatPhase == phase &&
        thatDetail == detail &&
        PickleStep(thatSource) == PickleStep(source)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(errorType, phase, detail, PickleStep(source))
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class InvalidFeatureFormatException(message: String) extends RuntimeException(message)

sealed trait QueryType

case object InitQuery extends QueryType

case object ExecQuery extends QueryType

case object ControlQuery extends QueryType

case object SideEffectQuery extends QueryType
