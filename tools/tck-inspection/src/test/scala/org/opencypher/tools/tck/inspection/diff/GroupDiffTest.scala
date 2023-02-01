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
import org.opencypher.tools.tck.inspection.diff.ScenarioDiffTag._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GroupDiffTest extends AnyFunSuite with Matchers {
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

  test("Diff a group with one scenario added") {
    val scrA = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrB), Seq(scrA, scrB))

    result.unchangedScenarios should equal(Set(scrB))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set())
    result.addedScenarios should equal(Set(scrA))
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with one scenario removed") {
    val scrA = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA, scrB), Seq(scrB))

    result.unchangedScenarios should equal(Set(scrB))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set())
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set(scrA))
  }

  test("Diff a group with one scenario moved to another feature") {
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA1, scrB), Seq(scrA2, scrB))

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))

    result.unchangedScenarios should equal(Set(scrB))
    result.movedScenarios should equal(Set(scrA1scrA2Diff))
    result.changedScenarios should equal(Set())
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with one scenario moved to another sub-feature") {
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String]("X"), "ftr2", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("X/ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA1, scrB), Seq(scrA2, scrB))

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))

    result.unchangedScenarios should equal(Set(scrB))
    result.movedScenarios should equal(Set(scrA1scrA2Diff))
    result.changedScenarios should equal(Set())
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with one scenario moved to another feature and one with a changed tag") {
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", Some(1), "scrA", None, None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr2.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String]("A"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String]("B"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA1, scrB1), Seq(scrA2, scrB2))

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))
    val scrB1scrB2Diff = ScenarioDiff(scrB1, scrB2)
    scrB1scrB2Diff.diffTags should equal(Set(Retagged))

    result.unchangedScenarios should equal(Set())
    result.movedScenarios should equal(Set(scrA1scrA2Diff))
    result.changedScenarios should equal(Set(scrB1scrB2Diff))
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with one scenario from an outline in a feature has a changed tags") {
    val scrA = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB0 = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(0), None, Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(1), Some("a"), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(2), None, Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1x = Scenario(List[String](), "ftr1", Some(2), "scrB", Some(1), Some("a"), Set[String]("B"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA, scrB0, scrB1, scrB2), Seq(scrA, scrB0, scrB1x, scrB2))

    val scrB1scrB1xDiff = ScenarioDiff(scrB1, scrB1x)
    scrB1scrB1xDiff.diffTags should equal(Set(Retagged))

    result.unchangedScenarios should equal(Set(scrA, scrB0, scrB2))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set(scrB1scrB1xDiff))
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with scenarios from an outline with a change in example name") {
    val scr0 = Scenario(List[String](), "ftr1", Some(1), "scrB", Some(0), None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scr1 = Scenario(List[String](), "ftr1", Some(1), "scrB", Some(1), Some("a"), Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scr2 = Scenario(List[String](), "ftr1", Some(1), "scrB", Some(2), None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scr1x = Scenario(List[String](), "ftr1", Some(1), "scrB", Some(1), Some("b"), Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scr0, scr1, scr2), Seq(scr0, scr1x, scr2))

    val scr1scr1xDiff = ScenarioDiff(scr1, scr1x)
    scr1scr1xDiff.diffTags should equal(Set(ExampleNameChanged))

    result.unchangedScenarios should equal(Set(scr0, scr2))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set(scr1scr1xDiff))
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with one scenario has changed tags, one is added, one is removed") {
    val stepsA = List[Step](Dummy(dummyPickleStep))
    val stepsB = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val stepsC = List[Step](Measure(dummyPickleStep), Dummy(dummyPickleStep))
    val stepsD = List[Step](Measure(dummyPickleStep))
    val scrA = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String](), stepsA, dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String]("A"), stepsB, dummyPickle, dummyPath("ftr1.feature"))
    val scrC1 = Scenario(List[String](), "ftr1", Some(3), "scrC", None, None, Set[String]("A"), stepsC, dummyPickle, dummyPath("ftr1.feature"))
    val scrC2 = Scenario(List[String](), "ftr1", Some(3), "scrC", None, None, Set[String]("B"), stepsC, dummyPickle, dummyPath("ftr1.feature"))
    val scrD = Scenario(List[String](), "ftr1", Some(4), "scrD", None, None, Set[String]("B"), stepsD, dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA, scrB, scrC1), Seq(scrA, scrC2, scrD))

    val scrC1scrC2Diff = ScenarioDiff(scrC1, scrC2)
    scrC1scrC2Diff.diffTags should equal(Set(Retagged))

    result.unchangedScenarios should equal(Set(scrA))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set(scrC1scrC2Diff))
    result.addedScenarios should equal(Set(scrD))
    result.removedScenarios should equal(Set(scrB))
  }

  test("Diff a group with one scenario changed in categories, tags, and content of steps") {
    val stepsA1 = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val stepsA2 = List[Step](Measure(dummyPickleStep), Dummy(dummyPickleStep))
    val scrA1 = Scenario(List[String](), "ftr1", Some(1), "scrA", None, None, Set[String]("T"), stepsA1, dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", Some(1), "scrA", None, None, Set[String]("X"), stepsA2, dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", Some(2), "scrB", None, None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA1, scrB), Seq(scrA2, scrB))

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved, Retagged, StepsChanged))

    result.unchangedScenarios should equal(Set(scrB))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set(scrA1scrA2Diff))
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set())
  }

}
