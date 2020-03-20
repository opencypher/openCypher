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
package org.opencypher.tools.tck.inspection.diff

import java.util

import gherkin.pickles.Argument
import gherkin.pickles.Pickle
import gherkin.pickles.PickleLocation
import gherkin.pickles.PickleStep
import gherkin.pickles.PickleTag
import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.inspection.diff.ScenarioDiffTag._
import org.scalatest.FunSuite
import org.scalatest.Matchers

class GroupDiffTest extends FunSuite with Matchers {
  private val dummyPickle = new Pickle("", "", new util.ArrayList[PickleStep](), new util.ArrayList[PickleTag](), new util.ArrayList[PickleLocation]())
  private val dummyPickleStep = new PickleStep("", new util.ArrayList[Argument](), new util.ArrayList[PickleLocation]())
  private def namedDummyPickleStep(name: String) = new PickleStep(name, new util.ArrayList[Argument](), new util.ArrayList[PickleLocation]())
  private def dummyStep(name: String): Step = Dummy(namedDummyPickleStep(name))
  private def dummyPath(path: String): java.nio.file.Path = new java.io.File("ftr1.feature").toPath

  test("Diff a group with one scenario added") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrB), Seq(scrA, scrB))

    result.unchangedScenarios should equal(Set(scrB))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set())
    result.addedScenarios should equal(Set(scrA))
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with one scenario removed") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA, scrB), Seq(scrB))

    result.unchangedScenarios should equal(Set(scrB))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set())
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set(scrA))
  }

  test("Diff a group with one scenario moved to another feature") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
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
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String]("X"), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("X/ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
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
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr2.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("A"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("B"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
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
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB0 = Scenario(List[String](), "ftr1", "scrB", Some(0), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", Some(2), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1x = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("B"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val result = GroupDiff(Seq(scrA, scrB0, scrB1, scrB2), Seq(scrA, scrB0, scrB1x, scrB2))

    val scrB1scrB1xDiff = ScenarioDiff(scrB1, scrB1x)
    scrB1scrB1xDiff.diffTags should equal(Set(Retagged))

    result.unchangedScenarios should equal(Set(scrA, scrB0, scrB2))
    result.movedScenarios should equal(Set())
    result.changedScenarios should equal(Set(scrB1scrB1xDiff))
    result.addedScenarios should equal(Set())
    result.removedScenarios should equal(Set())
  }

  test("Diff a group with one scenario has changed tags, one is added, one is removed") {
    val stepsA = List[Step](Dummy(dummyPickleStep))
    val stepsB = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val stepsC = List[Step](Measure(dummyPickleStep), Dummy(dummyPickleStep))
    val stepsD = List[Step](Measure(dummyPickleStep))
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), stepsA, dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("A"), stepsB, dummyPickle, dummyPath("ftr1.feature"))
    val scrC1 = Scenario(List[String](), "ftr1", "scrC", None, Set[String]("A"), stepsC, dummyPickle, dummyPath("ftr1.feature"))
    val scrC2 = Scenario(List[String](), "ftr1", "scrC", None, Set[String]("B"), stepsC, dummyPickle, dummyPath("ftr1.feature"))
    val scrD = Scenario(List[String](), "ftr1", "scrD", None, Set[String]("B"), stepsD, dummyPickle, dummyPath("ftr1.feature"))
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
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String]("T"), stepsA1, dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String]("X"), stepsA2, dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
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
