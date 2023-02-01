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
package org.opencypher.tools.tck.inspection.diff

import java.net.URI
import java.util

import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.api.groups.Feature
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.api.groups.ScenarioCategory
import org.opencypher.tools.tck.api.groups.ScenarioOutline
import org.opencypher.tools.tck.api.groups.Tag
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total
import org.opencypher.tools.tck.inspection.diff
import org.opencypher.tools.tck.inspection.diff.ScenarioDiffTag._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TckTreeDiffTest extends AnyFunSuite with Matchers {
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

  private def dummyStep(name: String): Step = Dummy(namedDummyPickleStep(name))
  private def dummyPath(path: String): java.nio.file.Path = new java.io.File("ftr1.feature").toPath

  test("Diff with one scenario added to the same top-level feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA, scrB)
    val collectBefore = TckTree(scenariosBefore)
    val collectAfter = TckTree(scenariosAfter)

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Seq(scrB), Seq(scrA, scrB)),
      Feature("ftr1", Total) -> GroupDiff(Seq(scrB), Seq(scrA, scrB)),
    )

    TckTreeDiff(collectBefore, collectAfter).diffs should equal(expectedResult)
  }

  test("Diff with one scenario removed from the same top-level feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrB)
    val collectBefore = TckTree(scenariosBefore)
    val collectAfter = TckTree(scenariosAfter)

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Seq(scrA, scrB), Seq(scrB)),
      Feature("ftr1", Total) -> GroupDiff(Seq(scrA, scrB), Seq(scrB)),
    )

    TckTreeDiff(collectBefore, collectAfter).diffs should equal(expectedResult)
  }

  test("Diff with one scenario moved to another top-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB)
    val collectBefore = TckTree(scenariosBefore)
    val collectAfter = TckTree(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Seq(scrA1, scrB), Seq(scrA2, scrB)),
      Feature("ftr1", Total) -> GroupDiff(Seq(scrA1, scrB), Seq(scrB)),
      Feature("ftr2", Total) -> GroupDiff(Seq(), Seq(scrA2)),
    )

    TckTreeDiff(collectBefore, collectAfter).diffs should equal(expectedResult)
  }

  test("Diff with one scenario moved to another sub-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String]("X"), "ftr2", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("X/ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB)
    val collectBefore = TckTree(scenariosBefore)
    val collectAfter = TckTree(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))

    val catX = ScenarioCategory("X", Total)
    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Seq(scrA1, scrB), Seq(scrA2, scrB)),
      Feature("ftr1", Total) -> GroupDiff(Seq(scrA1, scrB), Seq(scrB)),
      catX -> GroupDiff(Seq(), Seq(scrA2)),
      Feature("ftr2", catX) -> GroupDiff(Seq(), Seq(scrA2)),
    )

    TckTreeDiff(collectBefore, collectAfter).diffs should equal(expectedResult)
  }

  test("Diff with one scenario moved to another top-level feature and a changed tag") {
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", Some(1), "scrA", None, None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr2.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String]("A"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String]("B"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB1)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB2)
    val collectBefore = TckTree(scenariosBefore)
    val collectAfter = TckTree(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))
    val scrB1scrB2Diff = ScenarioDiff(scrB1, scrB2)
    scrB1scrB2Diff.diffTags should equal(Set(Retagged))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Seq(scrA1, scrB1), Seq(scrA2, scrB2)),
      Feature("ftr1", Total) -> GroupDiff(Seq(scrA1, scrB1), Seq(scrB2)),
      Feature("ftr2", Total) -> GroupDiff(Seq(), Seq(scrA2)),
      Tag("A") -> GroupDiff(Seq(scrB1), Seq()),
      Tag("B") -> GroupDiff(Seq(), Seq(scrB2)),
    )

    TckTreeDiff(collectBefore, collectAfter).diffs should equal(expectedResult)
  }

  test("Diff with two scenarios from an outline in a top-level feature have a changed tags") {
    val scrA = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB0 = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(0), None, Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(1), Some("a"), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrC = Scenario(List[String](), "ftr1", Some(3), "scrC", None, None, Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB0x = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(0), None, Set[String]("B"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1x = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(1), Some("a"), Set[String]("B"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA, scrB0, scrB1, scrC)
    val scenariosAfter: Seq[Scenario] = Seq(scrA, scrB0x, scrB1x, scrC)
    val collectBefore = TckTree(scenariosBefore)
    val collectAfter = TckTree(scenariosAfter)

    val scrB1scrB1xDiff = ScenarioDiff(scrB1, scrB1x)
    scrB1scrB1xDiff.diffTags should equal(Set(Retagged))
    val scrB0scrB0xDiff = ScenarioDiff(scrB0, scrB0x)
    scrB0scrB0xDiff.diffTags should equal(Set(Retagged))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Seq(scrA, scrB0, scrB1, scrC), Seq(scrA, scrB0x, scrB1x, scrC)),
      Feature("ftr1", Total) -> GroupDiff(Seq(scrA, scrB0, scrB1, scrC), Seq(scrA, scrB0x, scrB1x, scrC)),
      Tag("A") -> GroupDiff(Seq(scrB0, scrB1, scrC), Seq(scrC)),
      Tag("B") -> GroupDiff(Seq(), Seq(scrB0x, scrB1x)),
    )

    TckTreeDiff(collectBefore, collectAfter).diffs should equal(expectedResult)
  }

  test("Diff with one scenario changed in categories, tags, and content of steps") {
    val stepsA1 = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val stepsA2 = List[Step](Measure(dummyPickleStep), Dummy(dummyPickleStep))
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String]("T"), stepsA1, dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", Some(1), "scrA", None, None, Set[String]("X"), stepsA2, dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB)
    val collectBefore = TckTree(scenariosBefore)
    val collectAfter = TckTree(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved, Retagged, StepsChanged))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Seq(scrA1, scrB), Seq(scrA2, scrB)),
      Feature("ftr1", Total) -> GroupDiff(Seq(scrA1, scrB), Seq(scrB)),
      Feature("ftr2", Total) -> GroupDiff(Seq(), Seq(scrA2)),
      Tag("T") -> GroupDiff(Seq(scrA1), Seq()),
      Tag("X") -> GroupDiff(Seq(), Seq(scrA2)),
    )

    TckTreeDiff(collectBefore, collectAfter).diffs should equal(expectedResult)
  }

}
