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
import org.scalatest.{Inside, Inspectors, OptionValues}
import org.scalatest.matchers.should.Matchers

class OrderGroupsDepthFirstTest extends AnyFunSpec with Matchers with Inspectors with Inside with OptionValues {

  describe("Total group") {
    it("has indent of zero") {
      Total.indent shouldBe 0
    }
    it("has no parent") {
      Total.parent shouldBe None
    }
  }

  describe("A group sequence") {
    val scenarios = List(
      createScenario(List[String](), "ftr5 - a", "1", Set[String]()),
      createScenario(List[String](), "ftr1 - b", "1", Set[String]("A")),
      createScenario(List[String](), "ftr1 - b", "2", Set[String]("A")),
      createScenario(List[String]("b"), "ftr11 - c", "1", Set[String]("A", "C")),
      createScenario(List[String]("b"), "ftr11", "1", Set[String]("A", "C")),
      createScenario(List[String]("a", "b"), "ftr2", "1", Set[String]("C")),
      createScenario(List[String]("a", "b"), "ftr", "1", Set[String]("D")),
      createScenario(List[String]("b"), "ftr11 - b", "1", Set[String]("D", "2")),
      createScenario(List[String]("b"), "ftr3", "1", Set[String]("B")),
      createScenario(List[String]("a", "b"), "ftrX", "1", Set[String]("11")),
    )
    val groupedScenarios = GroupScenarios(scenarios)
    val groupSequence = OrderGroupsDepthFirst(groupedScenarios.keySet)

    it("has Total group as the first group") {
      groupSequence.head shouldBe Total
    }
    it("has Total group no where else") {
      forAll(groupSequence.tail) { _ should not be Total }
    }
    it("has one Feature group per distinct feature per category") {
      groupSequence.count {
        case _: Feature => true
        case _ => false
      } should equal(scenarios.map(s => (s.categories, s.featureName)).distinct.size)
    }
    it("has one ScenarioCategory group per distinct category") {
      def prepCategoryList(categories: List[String], parent: List[String] = List[String]()): List[List[String]] = categories match {
        case Nil => List[List[String]]()
        case c :: cs => (parent :+ c) +: prepCategoryList(cs, parent :+ c)
      }
      groupSequence.count {
        case _: ScenarioCategory => true
        case _ => false
      } should equal(scenarios.flatMap(s => prepCategoryList(s.categories)).distinct.size)
    }
    it("has one Tag group per distinct tag") {
      groupSequence.count {
        case _: Tag => true
        case _ => false
      } should equal(scenarios.flatMap(_.tags).distinct.size)
    }
    describe("has every group with a parent group so that") {
      groupSequence.tail foreach { g =>
        it(s"group $g has a parent") {
          g.parent.isDefined shouldBe true
        }
      }
    }
    describe("has every ScenarioCategory group with a parent group which is a ScenarioCategory or Total so that") {
      groupSequence.filter(_.isInstanceOf[ScenarioCategory]) foreach { g =>
        it(s"group $g has a parent which is a ScenarioCategory or Total") {
          g.parent.value should matchPattern {
            case _:ScenarioCategory =>
            case Total =>
          }
        }
      }
    }
    describe("has every Feature group with a parent group which is a ScenarioCategory or Total so that") {
      groupSequence.filter(_.isInstanceOf[Feature]) foreach { g =>
        it(s"group $g has a parent which is a ScenarioCategory or Total") {
          g.parent.value should matchPattern {
            case _:ScenarioCategory =>
            case Total =>
          }
        }
      }
    }
    describe("has every Tag group with a parent group which is Total so that") {
      groupSequence.filter(_.isInstanceOf[Tag]) foreach { g =>
        it(s"group $g has a parent which is Total") {
          g.parent.value shouldBe Total
        }
      }
    }
    describe("has every group after their parent group so that") {
      val groupsWithIndex = groupSequence.zipWithIndex
      groupsWithIndex.tail.foreach {
        case (g, gi) =>
          it(s"group $g is after ${g.parent}") {
            forExactly(1, groupsWithIndex) {
            x => inside(x) { case (p, pi) =>
              p shouldBe g.parent.value
              pi should be < gi
            }
          }
        }
      }
    }
    describe("has every group indented one more than their parent group so that") {
      groupSequence.tail.foreach { g =>
        it(s"group $g is indented one more than ${g.parent}") {
          g.indent should equal(g.parent.get.indent + 1)
        }
      }
    }
  }

  describe("The given list of four scenarios") {
    val scrA = createScenario(List[String](), "ftr5", "scrA", Set[String]())
    val scrB = createScenario(List[String](), "ftr1", "scrB", Set[String]("A"))
    val scrC = createScenario(List[String]("b"), "ftr11", "scrC", Set[String]("A"))
    val scrD = createScenario(List[String]("b"), "ftr3", "scrD", Set[String]("B"))

    val scenarios = List(scrA, scrB, scrC, scrD)
    val groupedScenarios = GroupScenarios(scenarios)
    val groupSequence = OrderGroupsDepthFirst(groupedScenarios.keySet)

    it("should yield the given group sequence") {
      val scB = ScenarioCategory("b", Total)
      val expected = Seq(
        Total,
        scB,
        Feature("ftr3", scB),
        Feature("ftr11", scB),
        Feature("ftr1", Total),
        Feature("ftr5", Total),
        Tag("A"),
        Tag("B")
      )

      groupSequence should equal(expected)
    }
  }

  describe("The given list of ten scenarios") {
    val scenarios = List(
      createScenario(List[String](), "ftr5 - a", "1", Set[String]()),
      createScenario(List[String](), "ftr1 - b", "1", Set[String]("A")),
      createScenario(List[String](), "ftr1 - b", "2", Set[String]("A")),
      createScenario(List[String]("b"), "ftr11 - c", "1", Set[String]("A", "C")),
      createScenario(List[String]("b"), "ftr11", "1", Set[String]("A", "C")),
      createScenario(List[String]("a", "b"), "ftr2", "1", Set[String]("C")),
      createScenario(List[String]("a", "b"), "ftr", "1", Set[String]("D")),
      createScenario(List[String]("b"), "ftr11 - b", "1", Set[String]("D", "2")),
      createScenario(List[String]("b"), "ftr3", "1", Set[String]("B")),
      createScenario(List[String]("a", "b"), "ftrX", "1", Set[String]("11")),
    )
    val groupedScenarios = GroupScenarios(scenarios)
    val groupSequence = OrderGroupsDepthFirst(groupedScenarios.keySet)

    it("should yield the given group sequence") {
      val scB = ScenarioCategory("b", Total)
      val scA = ScenarioCategory("a", Total)
      val scAB = ScenarioCategory("b", scA)
      val expected = Seq(
        Total,
        scA,
        scAB,
        Feature("ftr", scAB),
        Feature("ftr2", scAB),
        Feature("ftrX", scAB),
        scB,
        Feature("ftr3", scB),
        Feature("ftr11", scB),
        Feature("ftr11 - b", scB),
        Feature("ftr11 - c", scB),
        Feature("ftr1 - b", Total),
        Feature("ftr5 - a", Total),
        Tag("11"),
        Tag("2"),
        Tag("A"),
        Tag("B"),
        Tag("C"),
        Tag("D")
      )

      groupSequence should equal(expected)
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
