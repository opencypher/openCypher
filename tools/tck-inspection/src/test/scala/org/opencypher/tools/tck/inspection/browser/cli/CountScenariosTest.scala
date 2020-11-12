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

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.api.groups.{Group, CollectGroups}
import org.opencypher.tools.tck.inspection.diff
import org.opencypher.tools.tck.inspection.diff.GroupCollectionDiff
import org.opencypher.tools.tck.inspection.diff.GroupDiff
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CountScenariosTest extends AnyFunSuite with Matchers {
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

  test("Count single top-level scenario without tags") {
    val scenarios: Seq[Scenario] = Seq(Scenario(List[String](), "ftr1", "scr1", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total        1
        || ftr1       1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count single top-level scenario with tag") {
    val scenarios: Seq[Scenario] = Seq(Scenario(List[String](), "ftr1", "scr1", None, Set[String]("A", "@B", "C"), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total        1
        || ftr1       1
        || @B         1
        || A          1
        || C          1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count single sub-level scenario without tags") {
    val scenarios: Seq[Scenario] = Seq(Scenario(List[String]("A", "B", "C"), "ftr1", "scr1", None, Set[String](), List[Step](), dummyPickle, dummyPath("A/B/C/ftr1.feature")))
    val expectedCountOutput: String =
      """Total              1
        || A                1
        || | B              1
        || | | C            1
        || | | | ftr1       1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count two top-level scenarios in same feature without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total        2
        || ftr1       2""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count three top-level scenarios in two features without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String](), "ftr2", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature")),
      Scenario(List[String](), "ftr1", "scrC", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total        3
        || ftr1       2
        || ftr2       1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count three top-level scenarios in two features with tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String]("C", "B"), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String](), "ftr2", "scrB", None, Set[String]("C", "D", "A"), List[Step](), dummyPickle, dummyPath("ftr2.feature")),
      Scenario(List[String](), "ftr1", "scrC", None, Set[String]("A", "C"), List[Step](), dummyPickle, dummyPath("ftr1.feature")))
    val expectedCountOutput: String =
      """Total        3
        || ftr1       2
        || ftr2       1
        || A          2
        || B          1
        || C          3
        || D          1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count three mixed-level scenarios without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String]("T", "C", "K"), "ftr2", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("T/C/K/ftr2.feature")),
      Scenario(List[String]("T", "A", "K"), "ftr1", "scrC", None, Set[String](), List[Step](), dummyPickle, dummyPath("T/C/A/ftr1.feature")))
    val expectedCountOutput: String =
      """Total              3
        || T                2
        || | A              1
        || | | K            1
        || | | | ftr1       1
        || | C              1
        || | | K            1
        || | | | ftr2       1
        || ftr1             1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count five mixed-level scenarios with tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario(List[String](), "ftr1", "scrA", None, Set[String]("C", "B"), List[Step](), dummyPickle, dummyPath("ftr1.feature")),
      Scenario(List[String]("T", "C", "K"), "ftr2", "scrB", None, Set[String]("C", "D", "A"), List[Step](), dummyPickle, dummyPath("T/C/K/ftr2.feature")),
      Scenario(List[String]("T", "A", "K"), "ftr1", "scrC", None, Set[String]("A", "C"), List[Step](), dummyPickle, dummyPath("T/A/K/ftr1.feature")),
      Scenario(List[String]("T", "C", "K"), "ftr2", "scrD", None, Set[String](), List[Step](), dummyPickle, dummyPath("T/C/K/ftr2.feature")),
      Scenario(List[String]("T"), "ftr3", "scrE", None, Set[String]("B", "A", "C"), List[Step](), dummyPickle, dummyPath("T/ftr3.feature")))
    val expectedCountOutput: String =
      """Total              5
        || T                4
        || | A              1
        || | | K            1
        || | | | ftr1       1
        || | C              2
        || | | K            2
        || | | | ftr2       2
        || | ftr3           1
        || ftr1             1
        || A                3
        || B                2
        || C                4
        || D                1""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Count scenarios through TCK API") {
    val fooUri: URI = getClass.getResource("cucumber").toURI
    val scenarios: Seq[Scenario] = CypherTCK.parseFeatures(fooUri).flatMap(_.scenarios)
    val expectedCountOutput: String =
      """Total               13
        || foo                8
        || | bar              6
        || | | boo            4
        || | | | Boo          1
        || | | | Test 2       3
        || | | Test 1         2
        || | dummy            2
        || | | Dummy          2
        || Foo                5
        || @Fail              3
        || @TestA             2
        || @TestB             1
        || @TestC             3""".stripMargin
    CountScenarios.reportCountsInPrettyPrint(CollectGroups(scenarios)) should equal(expectedCountOutput)
  }

  test("Report pretty diff counts with one scenario added from a top-level same feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = GroupCollectionDiff(
      CollectGroups(Seq(scrB)),
      CollectGroups(Seq(scrA, scrB))
    )

    val expectedResult: String =
      """Group  unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total          1           0             0      1        0
        |- ftr1         1           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario removed from a top-level same feature without tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      CollectGroups(Seq(scrA, scrB)),
      CollectGroups(Seq(scrB))
    )

    val expectedResult: String =
      """Group  unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total          1           0             0      0        1
        |- ftr1         1           0             0      0        1""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario moved to another top-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      CollectGroups(Seq(scrA1, scrB)),
      CollectGroups(Seq(scrA2, scrB))
    )

    val expectedResult: String =
      """Group  unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total          1           1             0      0        0
        |- ftr1         1           0             0      0        1
        |- ftr2         0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario moved to another sub-level feature without tags") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String]("X"), "ftr2", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("X/ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      CollectGroups(Seq(scrA1, scrB)),
      CollectGroups(Seq(scrA2, scrB))
    )

    val expectedResult: String =
      """Group    unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total            1           1             0      0        0
        |- X              0           0             0      1        0
        |  - ftr2         0           0             0      1        0
        |- ftr1           1           0             0      0        1""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario moved to another top-level feature and a changed tag") {
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String](), List[Step](dummyStep("A")), dummyPickle, dummyPath("ftr2.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("A"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", None, Set[String]("B"), List[Step](dummyStep("B")), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      CollectGroups(Seq(scrA1, scrB1)),
      CollectGroups(Seq(scrA2, scrB2))
    )

    val expectedResult: String =
      """Group  unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total          0           1             1      0        0
        |- ftr1         0           0             1      0        1
        |- ftr2         0           0             0      1        0
        |- A            0           0             0      0        1
        |- B            0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario from a outline in a top-level feature has a changed tags") {
    val scrA = Scenario(List[String](), "ftr1", "scrA", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB0 = Scenario(List[String](), "ftr1", "scrB", Some(0), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1 = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB2 = Scenario(List[String](), "ftr1", "scrB", Some(2), Set[String]("A"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val scrB1x = Scenario(List[String](), "ftr1", "scrB", Some(1), Set[String]("B"), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      CollectGroups(Seq(scrA, scrB0, scrB1, scrB2)),
      CollectGroups(Seq(scrA, scrB0, scrB1x, scrB2))
    )

    val expectedResult: String =
      """Group  unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total          3           0             1      0        0
        |- ftr1         3           0             1      0        0
        |- A            2           0             0      0        1
        |- B            0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Report pretty diff counts with one scenario changed in categories, tags, and content of steps") {
    val stepsA1: List[Step] = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val stepsA2: List[Step] = List[Step](Measure(dummyPickleStep), Dummy(dummyPickleStep))
    val scrA1 = Scenario(List[String](), "ftr1", "scrA", None, Set[String]("T"), stepsA1, dummyPickle, dummyPath("ftr1.feature"))
    val scrA2 = Scenario(List[String](), "ftr2", "scrA", None, Set[String]("X"), stepsA2, dummyPickle, dummyPath("ftr2.feature"))
    val scrB = Scenario(List[String](), "ftr1", "scrB", None, Set[String](), List[Step](), dummyPickle, dummyPath("ftr1.feature"))
    val groupCollectionDiff: Map[Group, GroupDiff] = diff.GroupCollectionDiff(
      CollectGroups(Seq(scrA1, scrB)),
      CollectGroups(Seq(scrA2, scrB))
    )

    val expectedResult: String =
      """Group  unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total          1           0             1      0        0
        |- ftr1         1           0             0      0        1
        |- ftr2         0           0             0      1        0
        |- T            0           0             0      0        1
        |- X            0           0             0      1        0""".stripMargin

    CountScenarios.reportDiffCountsInPrettyPrint(groupCollectionDiff) should equal(expectedResult)
  }

  test("Diff scenarios through TCK API") {
    val fooUriBefore: URI = getClass.getResource("cucumber").toURI
    val fooUriAfter: URI = getClass.getResource("cucumberDiff").toURI
    val scenariosBefore: Seq[Scenario] = CypherTCK.parseFeatures(fooUriBefore).flatMap(_.scenarios)
    val scenariosAfter: Seq[Scenario] = CypherTCK.parseFeatures(fooUriAfter).flatMap(_.scenarios)
    val expectedResult: String =
      """Group          unchanged  moved only  changed more  added  removed
        |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
        |Total                 12           1             0      0        0
        |- foo                  7           1             0      0        0
        |  - bar                5           0             0      0        1
        |    - boo              3           0             0      0        1
        |      - Boo            0           0             0      0        1
        |      - Test 2         3           0             0      0        0
        |    - Test 1           2           0             0      0        0
        |  - dummy              2           0             0      0        0
        |    - Dummy            2           0             0      0        0
        |  - new                0           0             0      1        0
        |    - New              0           0             0      1        0
        |- Foo                  5           0             0      0        0
        |- @Fail                3           0             0      0        0
        |- @TestA               2           0             0      0        0
        |- @TestB               1           0             0      0        0
        |- @TestC               2           1             0      0        0""".stripMargin
    CountScenarios.reportDiffCountsInPrettyPrint(diff.GroupCollectionDiff(
      CollectGroups(scenariosBefore),
      CollectGroups(scenariosAfter)
    )) should equal(expectedResult)
  }
}
