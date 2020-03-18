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
package org.opencypher.tools.tck.inspection.browser.cli

import java.net.URI
import java.util

import gherkin.pickles.Argument
import gherkin.pickles.Pickle
import gherkin.pickles.PickleLocation
import gherkin.pickles.PickleStep
import gherkin.pickles.PickleTag
import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.inspection.collect.Group
import org.opencypher.tools.tck.inspection.collect.GroupCollection
import org.opencypher.tools.tck.inspection.diff
import org.opencypher.tools.tck.inspection.diff.GroupCollectionDiff
import org.opencypher.tools.tck.inspection.diff.GroupDiff
import org.scalatest.FunSuite
import org.scalatest.Matchers

class CountScenariosTest extends FunSuite with Matchers {
  private val dummyPickle = new Pickle("", "", new util.ArrayList[PickleStep](), new util.ArrayList[PickleTag](), new util.ArrayList[PickleLocation]())
  private val dummyPickleStep = new PickleStep("", new util.ArrayList[Argument](), new util.ArrayList[PickleLocation]())
  private def namedDummyPickleStep(name: String) = new PickleStep(name, new util.ArrayList[Argument](), new util.ArrayList[PickleLocation]())
  private def dummyStep(name: String): Step = Dummy(namedDummyPickleStep(name))
  private def dummyPath(path: String): java.nio.file.Path = new java.io.File("ftr1.feature").toPath

  test("Count single top-level scenario without tags") {
    val scenarios: Seq[Scenario] = Seq(Scenario(List[String](), "ftr1", "scr1", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total                 1
        || Feature: ftr1       1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count single top-level scenario with tag") {
    val scenarios: Seq[Scenario] = Seq(Scenario(List[String](), "ftr1", "scr1", None, Set[String]("A", "@B", "C"), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total                 1
        || Feature: ftr1       1
        || @B                  1
        || A                   1
        || C                   1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count single sub-level scenario without tags") {
    val scenarios: Seq[Scenario] = Seq(Scenario(List[String]("A", "B", "C"), "ftr1", "scr1", None, Set[String](), List[Step](), dummyPickle, dummyPath("A/B/C/ftr1.feature")))
    val expectedCountOutput: String =
      """Total                       1
        || A                         1
        || | B                       1
        || | | C                     1
        || | | | Feature: ftr1       1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count two top-level scenarios in same feature without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total                 2
        || Feature: ftr1       2""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count three top-level scenarios in two features without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String](), "ftr2", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature")),
      Scenario(List[String](), "ftr1", "scrC", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total                 3
        || Feature: ftr1       2
        || Feature: ftr2       1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count three top-level scenarios in two features with tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String]("C", "B"), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String](), "ftr2", "scrB", None, Set[String]("C", "D", "A"), List[Step](), dummyPickle, dummyPath("ftr2.feature")),
      Scenario(List[String](), "ftr1", "scrC", None, Set[String]("A", "C"), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total                 3
        || Feature: ftr1       2
        || Feature: ftr2       1
        || A                   2
        || B                   1
        || C                   3
        || D                   1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count three mixed-level scenarios without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String]("T", "C", "K"), "ftr2", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("T/C/K/ftr2.feature")),
      Scenario(List[String]("T", "A", "K"), "ftr1", "scrC", None, Set[String](), List[Step](), dummyPickle, dummyPath("T/C/A/ftr1.feature")))
    val expectedCountOutput: String =
      """Total                       3
        || T                         2
        || | A                       1
        || | | K                     1
        || | | | Feature: ftr1       1
        || | C                       1
        || | | K                     1
        || | | | Feature: ftr2       1
        || Feature: ftr1             1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count five mixed-level scenarios with tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String]("C", "B"), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String]("T", "C", "K"), "ftr2", "scrB", None, Set[String]("C", "D", "A"), List[Step](), dummyPickle, dummyPath("T/C/K/ftr2.feature")),
      Scenario(List[String]("T", "A", "K"), "ftr1", "scrC", None, Set[String]("A", "C"), List[Step](), dummyPickle, dummyPath("T/A/K/ftr1.feature")),
      Scenario(List[String]("T", "C", "K"), "ftr2", "scrD", None, Set[String](), List[Step](), dummyPickle, dummyPath("T/C/K/ftr2.feature")),
      Scenario(List[String]("T"), "ftr3", "scrE", None, Set[String]("B", "A", "C"), List[Step](), dummyPickle, dummyPath("T/ftr3.feature")))
    val expectedCountOutput: String =
      """Total                       5
        || T                         4
        || | A                       1
        || | | K                     1
        || | | | Feature: ftr1       1
        || | C                       2
        || | | K                     2
        || | | | Feature: ftr2       2
        || | Feature: ftr3           1
        || Feature: ftr1             1
        || A                         3
        || B                         2
        || C                         4
        || D                         1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Count scenarios through TCK API") {
    val fooUri: URI = getClass.getResource("cucumber").toURI
    val scenarios: Seq[Scenario] = CypherTCK.parseFeatures(fooUri).flatMap(_.scenarios)
    val expectedCountOutput: String =
      """Total                        13
        || foo                         8
        || | bar                       6
        || | | boo                     4
        || | | | Feature: Boo          1
        || | | | Feature: Test 2       3
        || | | Feature: Test 1         2
        || | dummy                     2
        || | | Feature: Dummy          2
        || Feature: Foo                5
        || @Fail                       3
        || @TestA                      2
        || @TestB                      1
        || @TestC                      3""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(GroupCollection(scenarios)) should equal(expectedCountOutput)
  }

  test("Report pretty diff counts with one scenario added from a top-level same feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = GroupCollectionDiff(
      GroupCollection(Seq(scrB)),
      GroupCollection(Seq(scrA, scrB))
    )

    val expectedResult: String =
      """Group           unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                   1           0             0      1        0
        |- Feature: ftr1         1           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario removed from a top-level same feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      GroupCollection(Seq(scrA, scrB)),
      GroupCollection(Seq(scrB))
    )

    val expectedResult: String =
      """Group           unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                   1           0             0      0        1
        |- Feature: ftr1         1           0             0      0        1""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario moved to another top-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      GroupCollection(Seq(scrA1, scrB)),
      GroupCollection(Seq(scrA2, scrB))
    )

    val expectedResult: String =
      """Group           unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                   1           1             0      0        0
        |- Feature: ftr1         1           0             0      0        1
        |- Feature: ftr2         0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario moved to another sub-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String]("X"), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("X/ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      GroupCollection(Seq(scrA1, scrB)),
      GroupCollection(Seq(scrA2, scrB))
    )

    val expectedResult: String =
      """Group             unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                     1           1             0      0        0
        |- X                       0           0             0      1        0
        |  - Feature: ftr2         0           0             0      1        0
        |- Feature: ftr1           1           0             0      0        1""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario moved to another top-level feature and a changed tag") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr2.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("A"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("B"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      GroupCollection(Seq(scrA1, scrB1)),
      GroupCollection(Seq(scrA2, scrB2))
    )

    val expectedResult: String =
      """Group           unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                   0           1             1      0        0
        |- Feature: ftr1         0           0             1      0        1
        |- Feature: ftr2         0           0             0      1        0
        |- A                     0           0             0      0        1
        |- B                     0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario from a outline in a top-level feature has a changed tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB0 = Scenario(List[String](), "ftr1", "scrB", Some(0), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", Some(2), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1x = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("B"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      GroupCollection(Seq(scrA, scrB0, scrB1, scrB2)),
      GroupCollection(Seq(scrA, scrB0, scrB1x, scrB2))
    )

    val expectedResult: String =
      """Group           unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                   3           0             1      0        0
        |- Feature: ftr1         3           0             1      0        0
        |- A                     2           0             0      0        1
        |- B                     0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario changed in categories, tags, and content of steps") {
    val stepsA1: List[Step] = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val stepsA2: List[Step] = List[Step](Measure(dummyPickleStep), Dummy(dummyPickleStep))
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String]("T"), stepsA1, dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String]("X"), stepsA2, dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      GroupCollection(Seq(scrA1, scrB)),
      GroupCollection(Seq(scrA2, scrB))
    )

    val expectedResult: String =
      """Group           unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                   1           0             1      0        0
        |- Feature: ftr1         1           0             0      0        1
        |- Feature: ftr2         0           0             0      1        0
        |- T                     0           0             0      0        1
        |- X                     0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Diff scenarios through TCK API") {
    val fooUriBefore: URI = getClass.getResource("cucumber").toURI
    val fooUriAfter: URI = getClass.getResource("cucumberDiff").toURI
    val scenariosBefore: Seq[Scenario] = CypherTCK.parseFeatures(fooUriBefore).flatMap(_.scenarios)
    val scenariosAfter: Seq[Scenario] = CypherTCK.parseFeatures(fooUriAfter).flatMap(_.scenarios)
    val expectedResult: String =
      """Group                   unchanged  moved only  changed more  added  removed
        |–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                          12           1             0      0        0
        |- foo                           7           1             0      0        0
        |  - bar                         5           0             0      0        1
        |    - boo                       3           0             0      0        1
        |      - Feature: Boo            0           0             0      0        1
        |      - Feature: Test 2         3           0             0      0        0
        |    - Feature: Test 1           2           0             0      0        0
        |  - dummy                       2           0             0      0        0
        |    - Feature: Dummy            2           0             0      0        0
        |  - new                         0           0             0      1        0
        |    - Feature: New              0           0             0      1        0
        |- Feature: Foo                  5           0             0      0        0
        |- @Fail                         3           0             0      0        0
        |- @TestA                        2           0             0      0        0
        |- @TestB                        1           0             0      0        0
        |- @TestC                        2           1             0      0        0""".stripMargin
    CountScenarios.reportDiffCountsInPrettyPrint(diff.GroupCollectionDiff(
      GroupCollection(scenariosBefore),
      GroupCollection(scenariosAfter)
    )) should equal(expectedResult)
  }
}
