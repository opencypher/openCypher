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
package org.opencypher.tools.tck.api.groups

import java.net.URI
import java.util

import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Inside
import org.scalatest.Inspectors
import org.scalatest.OptionValues

class GroupScenariosTest extends AnyFunSpec with Matchers with Inspectors with Inside with OptionValues {

  describe("The given list of four scenarios, GroupScenarios") {
    val scrA = createScenario(List[String](), "ftr5", "scrA", Set[String]())
    val scrB = createScenario(List[String](), "ftr1", "scrB", Set[String]("A"))
    val scrC = createScenario(List[String]("b"), "ftr11", "scrC", Set[String]("A"))
    val scrD = createScenario(List[String]("b"), "ftr3", "scrD", Set[String]("B"))

    val scenarios = List(scrA, scrB, scrC, scrD)
    val groupedScenarios = GroupScenarios(scenarios)

    it("should yield the given map") {
      val scB = ScenarioCategory("b", Total)
      val expected = Map(
        Total -> Seq(scrA, scrB, scrC, scrD),
        scB -> Seq(scrC, scrD),
        Feature("ftr3", scB) -> Seq(scrD),
        Feature("ftr11", scB) -> Seq(scrC),
        Feature("ftr1", Total) -> Seq(scrB),
        Feature("ftr5", Total) -> Seq(scrA),
        Tag("A") -> Seq(scrB, scrC),
        Tag("B") -> Seq(scrD)
      )

      groupedScenarios should equal(expected)
    }
  }

  describe("The given list of ten scenarios, GroupScenarios") {
    val scrA = createScenario(List[String](), "ftr5 - a", "1", Set[String]())
    val scrB = createScenario(List[String](), "ftr1 - b", "1", Set[String]("A"))
    val scrC = createScenario(List[String](), "ftr1 - b", "2", Set[String]("A"))
    val scrD = createScenario(List[String]("b"), "ftr11 - c", "1", Set[String]("A", "C"))
    val scrE = createScenario(List[String]("b"), "ftr11", "1", Set[String]("A", "C"))
    val scrF = createScenario(List[String]("a", "b"), "ftr2", "1", Set[String]("C"))
    val scrG = createScenario(List[String]("a", "b"), "ftr", "1", Set[String]("D"))
    val scrH = createScenario(List[String]("b"), "ftr11 - b", "1", Set[String]("D", "2"))
    val scrI = createScenario(List[String]("b"), "ftr3", "1", Set[String]("B"))
    val scrJ = createScenario(List[String]("a", "b"), "ftrX", "1", Set[String]("11"))

    val scenarios = List(scrA, scrB, scrC, scrD, scrE, scrF, scrG, scrH, scrI, scrJ)
    val groupedScenarios = GroupScenarios(scenarios)

    it("should yield the given map") {
      val scB = ScenarioCategory("b", Total)
      val scA = ScenarioCategory("a", Total)
      val scAB = ScenarioCategory("b", scA)
      val expected = Map(
        Total -> Seq(scrA, scrB, scrC, scrD, scrE, scrF, scrG, scrH, scrI, scrJ),
        scA -> Seq(scrF, scrG, scrJ),
        scAB -> Seq(scrF, scrG, scrJ),
        Feature("ftr", scAB) -> Seq(scrG),
        Feature("ftr2", scAB) -> Seq(scrF),
        Feature("ftrX", scAB) -> Seq(scrJ),
        scB -> Seq(scrD, scrE, scrH, scrI),
        Feature("ftr3", scB) -> Seq(scrI),
        Feature("ftr11", scB) -> Seq(scrE),
        Feature("ftr11 - b", scB) -> Seq(scrH),
        Feature("ftr11 - c", scB) -> Seq(scrD),
        Feature("ftr1 - b", Total) -> Seq(scrB, scrC),
        Feature("ftr5 - a", Total) -> Seq(scrA),
        Tag("11") -> Seq(scrJ),
        Tag("2") -> Seq(scrH),
        Tag("A") -> Seq(scrB, scrC, scrD, scrE),
        Tag("B") -> Seq(scrI),
        Tag("C") -> Seq(scrD, scrE, scrF),
        Tag("D") -> Seq(scrG, scrH)
      )

      groupedScenarios should equal(expected)
    }
  }

  private def createScenario(categories: List[String], featureName: String, name: String, tags: Set[String]) =
    Scenario(categories, featureName, name, None, tags, dummySteps, dummyPickle, dummyPath("xyz.feature"))

  private val dummyPickle = new io.cucumber.core.gherkin.Pickle() {
    override def getKeyword: String = ""

    override def getLanguage: String = "EN"

    override def getName: String = "name"

    override def getLocation: io.cucumber.core.gherkin.Location = new io.cucumber.core.gherkin.Location() {
      override def getLine: Int = 1

      override def getColumn: Int = 1
    }

    override def getScenarioLocation: io.cucumber.core.gherkin.Location = new io.cucumber.core.gherkin.Location() {
      override def getLine: Int = 1

      override def getColumn: Int = 1
    }

    override def getSteps: util.List[io.cucumber.core.gherkin.Step] = new util.ArrayList[io.cucumber.core.gherkin.Step]()

    override def getTags: util.List[String] = new util.ArrayList[String]()

    override def getUri: URI = new URI("http://www.opencypher.org/")

    override def getId: String = "id"
  }

  private def namedDummyPickleStep(name: String): io.cucumber.core.gherkin.Step = new io.cucumber.core.gherkin.Step() {
    override def getLine: Int = 1

    override def getArgument: io.cucumber.core.gherkin.Argument = new io.cucumber.core.gherkin.DocStringArgument() {
      override def getContent: String = "text"

      override def getContentType: String = ""

      override def getLine: Int = 1
    }

    override def getKeyWord: String = "keyWord"

    override def getType: io.cucumber.core.gherkin.StepType = io.cucumber.core.gherkin.StepType.GIVEN

    override def getPreviousGivenWhenThenKeyWord: String = ""

    override def getText: String = name

    override def getId: String = "id"
  }

  private val dummyPickleStep = namedDummyPickleStep("")

  private val dummySteps: List[Step] = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
  private def dummyPath(path: String): java.nio.file.Path = new java.io.File("ftr1.feature").toPath

}
