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
import org.opencypher.tools.tck.inspection.collect.Feature
import org.opencypher.tools.tck.inspection.collect.Group
import org.opencypher.tools.tck.inspection.collect.GroupCollection
import org.opencypher.tools.tck.inspection.collect.ScenarioCategory
import org.opencypher.tools.tck.inspection.collect.Tag
import org.opencypher.tools.tck.inspection.collect.Total
import org.opencypher.tools.tck.inspection.diff
import org.opencypher.tools.tck.inspection.diff.ScenarioDiffTag._
import org.scalatest.FunSuite
import org.scalatest.Matchers

class GroupCollectionDiffTest extends FunSuite with Matchers {
  private val dummyPickle = new Pickle("", "", new util.ArrayList[PickleStep](), new util.ArrayList[PickleTag](), new util.ArrayList[PickleLocation]())
  private val dummyPickleStep = new PickleStep("", new util.ArrayList[Argument](), new util.ArrayList[PickleLocation]())
  private def namedDummyPickleStep(name: String) = new PickleStep(name, new util.ArrayList[Argument](), new util.ArrayList[PickleLocation]())
  private def dummyStep(name: String): Step = Dummy(namedDummyPickleStep(name))
  private def dummyPath(path: String): java.nio.file.Path = new java.io.File("ftr1.feature").toPath

  test("Diff with one scenario added from a top-level same feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA, scrB)
    val collectBefore = GroupCollection(scenariosBefore)
    val collectAfter = GroupCollection(scenariosAfter)

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Set(scrB), Set(), Set(), Set(scrA), Set()),
      Feature("ftr1", 1, Some(Total)) -> GroupDiff(Set(scrB), Set(), Set(), Set(scrA), Set())
    )

    GroupCollectionDiff(collectBefore, collectAfter) should equal(expectedResult)
  }

  test("Diff with one scenario removed from a top-level same feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrB)
    val collectBefore = GroupCollection(scenariosBefore)
    val collectAfter = GroupCollection(scenariosAfter)

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Set(scrB), Set(), Set(), Set(), Set(scrA)),
      Feature("ftr1", 1, Some(Total)) -> GroupDiff(Set(scrB), Set(), Set(), Set(), Set(scrA))
    )

    diff.GroupCollectionDiff(collectBefore, collectAfter) should equal(expectedResult)
  }

  test("Diff with one scenario moved to another top-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB)
    val collectBefore = GroupCollection(scenariosBefore)
    val collectAfter = GroupCollection(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Set(scrB), Set((scrA1, scrA2, scrA1scrA2Diff)), Set(), Set(), Set()),
      Feature("ftr1", 1, Some(Total)) -> GroupDiff(Set(scrB), Set(), Set(), Set(), Set(scrA1)),
      Feature("ftr2", 1, Some(Total)) -> GroupDiff(Set(), Set(), Set(), Set(scrA2), Set())
    )

    diff.GroupCollectionDiff(collectBefore, collectAfter) should equal(expectedResult)
  }

  test("Diff with one scenario moved to another sub-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String]("X"), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("X/ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB)
    val collectBefore = GroupCollection(scenariosBefore)
    val collectAfter = GroupCollection(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))

    val catX = ScenarioCategory("X", 1, Some(Total))
    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Set(scrB), Set((scrA1, scrA2, scrA1scrA2Diff)), Set(), Set(), Set()),
      Feature("ftr1", 1, Some(Total)) -> GroupDiff(Set(scrB), Set(), Set(), Set(), Set(scrA1)),
      catX -> GroupDiff(Set(), Set(), Set(), Set(scrA2), Set()),
      Feature("ftr2", 2, Some(catX)) -> GroupDiff(Set(), Set(), Set(), Set(scrA2), Set())
    )

    diff.GroupCollectionDiff(collectBefore, collectAfter) should equal(expectedResult)
  }

  test("Diff with one scenario moved to another top-level feature and a changed tag") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr2.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("A"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("B"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB1)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB2)
    val collectBefore = GroupCollection(scenariosBefore)
    val collectAfter = GroupCollection(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved))
    val scrB1scrB2Diff = ScenarioDiff(scrB1, scrB2)
    scrB1scrB2Diff.diffTags should equal(Set(Retagged))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Set(), Set((scrA1, scrA2, scrA1scrA2Diff)), Set((scrB1, scrB2, scrB1scrB2Diff)), Set(), Set()),
      Feature("ftr1", 1, Some(Total)) -> GroupDiff(Set(), Set(), Set((scrB1, scrB2, scrB1scrB2Diff)), Set(), Set(scrA1)),
      Feature("ftr2", 1, Some(Total)) -> GroupDiff(Set(), Set(), Set(), Set(scrA2), Set()),
      Tag("A") -> GroupDiff(Set(), Set(), Set(), Set(), Set(scrB1)),
      Tag("B") -> GroupDiff(Set(), Set(), Set(), Set(scrB2), Set())
    )

    diff.GroupCollectionDiff(collectBefore, collectAfter) should equal(expectedResult)
  }

  test("Diff with one scenario from a outline in a top-level feature has a changed tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB0 = Scenario(List[String](), "ftr1", "scrB", Some(0), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", Some(2), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1x = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("B"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA, scrB0, scrB1, scrB2)
    val scenariosAfter: Seq[Scenario] = Seq(scrA, scrB0, scrB1x, scrB2)
    val collectBefore = GroupCollection(scenariosBefore)
    val collectAfter = GroupCollection(scenariosAfter)

    val scrB1scrB1xDiff = ScenarioDiff(scrB1, scrB1x)
    scrB1scrB1xDiff.diffTags should equal(Set(Retagged))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Set(scrA, scrB0, scrB2), Set(), Set((scrB1, scrB1x, scrB1scrB1xDiff)), Set(), Set()),
      Feature("ftr1", 1, Some(Total)) -> GroupDiff(Set(scrA, scrB0, scrB2), Set(), Set((scrB1, scrB1x, scrB1scrB1xDiff)), Set(), Set()),
      Tag("A") -> GroupDiff(Set(scrB0, scrB2), Set(), Set(), Set(), Set(scrB1)),
      Tag("B") -> GroupDiff(Set(), Set(), Set(), Set(scrB1x), Set())
    )

    diff.GroupCollectionDiff(collectBefore, collectAfter) should equal(expectedResult)
  }

  test("Diff with one scenario changed in categories, tags, and content of steps") {
    val stepsA1 = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val stepsA2 = List[Step](Measure(dummyPickleStep), Dummy(dummyPickleStep))
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String]("T"), stepsA1, dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String]("X"), stepsA2, dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scenariosBefore: Seq[Scenario] = Seq(scrA1, scrB)
    val scenariosAfter: Seq[Scenario] = Seq(scrA2, scrB)
    val collectBefore = GroupCollection(scenariosBefore)
    val collectAfter = GroupCollection(scenariosAfter)

    val scrA1scrA2Diff = ScenarioDiff(scrA1, scrA2)
    scrA1scrA2Diff.diffTags should equal(Set(Moved, Retagged, StepsChanged))

    val expectedResult = Map[Group, GroupDiff](
      Total -> GroupDiff(Set(scrB), Set(), Set((scrA1, scrA2, scrA1scrA2Diff)), Set(), Set()),
      Feature("ftr1", 1, Some(Total)) -> GroupDiff(Set(scrB), Set(), Set(), Set(), Set(scrA1)),
      Feature("ftr2", 1, Some(Total)) -> GroupDiff(Set(), Set(), Set(), Set(scrA2), Set()),
      Tag("T") -> GroupDiff(Set(), Set(), Set(), Set(), Set(scrA1)),
      Tag("X") -> GroupDiff(Set(), Set(), Set(), Set(scrA2), Set())
    )

    diff.GroupCollectionDiff(collectBefore, collectAfter) should equal(expectedResult)
  }

}
