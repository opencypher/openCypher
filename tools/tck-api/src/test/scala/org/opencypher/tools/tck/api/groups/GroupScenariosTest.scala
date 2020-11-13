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

  describe("Total group") {
    it("has indent of zero") {
      Total.indent shouldBe 0
    }
    it("has no parent") {
      Total.parent shouldBe None
    }
  }

  def invariants(groupedScenarios: Map[Group, Seq[Scenario]], scenarios: Seq[Scenario]) {
    val groups = groupedScenarios.keys

    describe("should yield a map that contains the every of original scenarios at least once") {
      groups.flatMap(g => groupedScenarios(g)).toSet should equal(scenarios.toSet)
    }
    describe("should yield a map where the key set") {
      it("has one Feature group per distinct feature per category") {
        groups.count {
          case _: Feature => true
          case _ => false
        } should equal(scenarios.map(s => (s.categories, s.featureName)).distinct.size)
      }
      it("has one ScenarioCategory group per distinct category") {
        def prepCategoryList(categories: List[String], parent: List[String] = List[String]()): List[List[String]] = categories match {
          case Nil => List[List[String]]()
          case c :: cs => (parent :+ c) +: prepCategoryList(cs, parent :+ c)
        }
        groups.count {
          case _: ScenarioCategory => true
          case _ => false
        } should equal(scenarios.flatMap(s => prepCategoryList(s.categories)).distinct.size)
      }
      it("has one Tag group per distinct tag") {
        groups.count {
          case _: Tag => true
          case _ => false
        } should equal(scenarios.flatMap(_.tags).distinct.size)
      }
      describe("has every group, except Total, with a parent group so that") {
        groups filter {
          case Total => false
          case _ => true
        } foreach { g =>
          it(s"group $g has a parent") {
            g.parent.isDefined shouldBe true
          }
        }
      }
      describe("has every ScenarioCategory group with a parent group which is a ScenarioCategory or Total so that") {
        groups filter {
          case _:ScenarioCategory => true
          case _ => false
        } foreach { g =>
          it(s"group $g has a parent which is a ScenarioCategory or Total") {
            g.parent.value should matchPattern {
              case _:ScenarioCategory =>
              case Total =>
            }
          }
        }
      }
      describe("has every Feature group with a parent group which is a ScenarioCategory or Total so that") {
        groups filter {
          case _:Feature => true
          case _ => false
        } foreach { g =>
          it(s"group $g has a parent which is a ScenarioCategory or Total") {
            g.parent.value should matchPattern {
              case _:ScenarioCategory =>
              case Total =>
            }
          }
        }
      }
      describe("has every Tag group with a parent group which is Total so that") {
        groups filter {
          case _:Tag => true
          case _ => false
        } foreach { g =>
          it(s"group $g has a parent which is Total") {
            g.parent.value shouldBe Total
          }
        }
      }
      describe("has every group, except Total, indented one more than their parent group so that") {
        groups filter {
          case Total => false
          case _ => true
        } foreach { g =>
          it(s"group $g is indented one more than ${g.parent}") {
            g.indent should equal(g.parent.get.indent + 1)
          }
        }
      }
    }
  }

  describe("The given list of four scenarios, GroupScenarios") {
    val scrA = createScenario(List[String](), "ftr5", "scrA", Set[String]())
    val scrB = createScenario(List[String](), "ftr1", "scrB", Set[String]("A"))
    val scrC = createScenario(List[String]("b"), "ftr11", "scrC", Set[String]("A"))
    val scrD = createScenario(List[String]("b"), "ftr3", "scrD", Set[String]("B"))

    val scenarios = List(scrA, scrB, scrC, scrD)
    val groupedScenarios = GroupScenarios(scenarios)

    invariants(groupedScenarios, scenarios)

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

    invariants(groupedScenarios, scenarios)

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

  private def createScenario(categories: List[String], featureName: String, name: String, tags: Set[String]) = {
    val dummyPickle = new io.cucumber.core.gherkin.Pickle() {
      override val getKeyword: String = ""

      override val getLanguage: String = "EN"

      override val getName: String = "name"

      override val getLocation: io.cucumber.core.gherkin.Location = new io.cucumber.core.gherkin.Location() {
        override val getLine: Int = 1

        override val getColumn: Int = 1
      }

      override val getScenarioLocation: io.cucumber.core.gherkin.Location = new io.cucumber.core.gherkin.Location() {
        override val getLine: Int = 1

        override val getColumn: Int = 1
      }

      override val getSteps: util.List[io.cucumber.core.gherkin.Step] = new util.ArrayList[io.cucumber.core.gherkin.Step]()

      override val getTags: util.List[String] = new util.ArrayList[String]()

      override val getUri: URI = new URI("http://www.opencypher.org/")

      override val getId: String = "id"
    }

    val dummyPickleStep: io.cucumber.core.gherkin.Step = new io.cucumber.core.gherkin.Step() {
      override val getLine: Int = 1

      override val getArgument: io.cucumber.core.gherkin.Argument = new io.cucumber.core.gherkin.DocStringArgument() {
        override val getContent: String = "text"

        override val getContentType: String = ""

        override val getLine: Int = 1
      }

      override val getKeyWord: String = "keyWord"

      override val getType: io.cucumber.core.gherkin.StepType = io.cucumber.core.gherkin.StepType.GIVEN

      override val getPreviousGivenWhenThenKeyWord: String = ""

      override val getText: String = "xyz"

      override val getId: String = "id"
    }

    val dummySteps: List[Step] = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val dummyPath: java.nio.file.Path = new java.io.File("ftr1.feature").toPath

    Scenario(categories, featureName, name, None, tags, dummySteps, dummyPickle, dummyPath)
  }
}
