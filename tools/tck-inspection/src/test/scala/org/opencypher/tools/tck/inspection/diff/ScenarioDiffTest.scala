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

class ScenarioDiffTest extends AnyFunSuite with Matchers {
  val rand = new scala.util.Random(1)

  val noPickleSteps = new util.ArrayList[io.cucumber.core.gherkin.Step]()
  val noPickleTags = new util.ArrayList[String]()

  def location(line: Int, column: Int): io.cucumber.core.gherkin.Location = {
    new io.cucumber.core.gherkin.Location() {
      override def getLine: Int = line

      override def getColumn: Int = column
    }
  }

  def stringArgument(text: String, line: Int): io.cucumber.core.gherkin.DocStringArgument = {
    new io.cucumber.core.gherkin.DocStringArgument() {
      override def getContent: String = text

      override def getContentType: String = ""

      override def getLine: Int = line
    }
  }

  def step(id: String, stepType: io.cucumber.core.gherkin.StepType, keyWord: String, argument: io.cucumber.core.gherkin.Argument, text: String, line: Int): io.cucumber.core.gherkin.Step = {
    new io.cucumber.core.gherkin.Step() {
      override def getLine: Int = line

      override def getArgument: io.cucumber.core.gherkin.Argument = argument

      override def getKeyWord: String = keyWord

      override def getType: io.cucumber.core.gherkin.StepType = stepType

      override def getPreviousGivenWhenThenKeyWord: String = ""

      override def getText: String = text

      override def getId: String = id
    }
  }

  def pickle(id: String, name: String, col: Int): io.cucumber.core.gherkin.Pickle = {

    val steps = new util.ArrayList[io.cucumber.core.gherkin.Step]()
    steps.add(step("1", io.cucumber.core.gherkin.StepType.GIVEN, "exec", stringArgument("a", 2), "abc", 2))
    steps.add(step("1", io.cucumber.core.gherkin.StepType.THEN, "result", stringArgument("a", 3), "xyz", 3))

    val tags = new util.ArrayList[String]()
    tags.add("S")
    tags.add("T")

    val loc = location(6, col)

    new io.cucumber.core.gherkin.Pickle() {
      override def getKeyword: String = ""

      override def getLanguage: String = "EN"

      override def getName: String = name

      override def getLocation: io.cucumber.core.gherkin.Location = loc

      override def getScenarioLocation: io.cucumber.core.gherkin.Location = loc

      override def getSteps: util.List[io.cucumber.core.gherkin.Step] = steps

      override def getTags: util.List[String] = tags

      override def getUri: URI = new URI("http://www.opencypher.org/")

      override def getId: String = id
    }
  }

  test("Diff equal scenarios not differing in source") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Unchanged, SourceUnchanged))
  }

  test("Diff equal scenarios differing in source") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("2", "s", 2).getSteps.get(0)), Measure(pickle("2", "s", 2).getSteps.get(1))),
      pickle("2", "s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Unchanged, SourceChanged))
  }

  test("Diff scenarios differing in category only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("XX", "A"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("2", "s", 2).getSteps.get(0)), Measure(pickle("2", "s", 2).getSteps.get(1))),
      pickle("2", "s", 2), new java.io.File("XX/A/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Moved))
  }

  test("Diff scenarios differing in tags only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "exec", 1).getSteps.get(0)), Measure(pickle("1", "result", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "XX"),
      List[Step](Dummy(pickle("2", "s", 2).getSteps.get(0)), Measure(pickle("2", "s", 2).getSteps.get(1))),
      pickle("2", "s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Retagged))
  }

  test("Diff scenarios differing in kind of steps only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Measure(pickle("2", "s", 2).getSteps.get(0)), Dummy(pickle("2", "s", 2).getSteps.get(1))),
      pickle("2", "s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](StepsChanged))
  }

  test("Diff scenarios differing in content of steps only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("2", "s", 2).getSteps.get(1)), Measure(pickle("2", "s", 2).getSteps.get(0))),
      pickle("2", "s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](StepsChanged))
  }

  test("Diff equal scenarios differing in example index only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", Some(0), None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", Some(1), None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("2", "s", 2).getSteps.get(0)), Measure(pickle("2", "s", 2).getSteps.get(1))),
      pickle("2", "s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](ExampleIndexChanged))
  }

  test("Diff equal scenarios differing in example name only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", Some(0), Some("a"), Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", Some(0), Some("b"), Set[String]("S", "T"),
      List[Step](Dummy(pickle("2", "s", 2).getSteps.get(0)), Measure(pickle("2", "s", 2).getSteps.get(1))),
      pickle("2", "s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](ExampleNameChanged))
  }

  test("Diff scenarios differing in categories, tags, and content of steps only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("XX", "B"), "f", Some(1), "s", None, None, Set[String]("S", "XX"),
      List[Step](Dummy(pickle("2", "s", 2).getSteps.get(1)), Measure(pickle("2", "s", 2).getSteps.get(0))),
      pickle("2", "s", 2), new java.io.File("XX/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Moved, Retagged, StepsChanged))
  }

  test("Diff different scenarios with different name") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", None, None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "XX", None, None, Set[String]("S", "XX"),
      List[Step](Measure(pickle("2", "XX", 2).getSteps.get(0)), Dummy(pickle("2", "XX", 2).getSteps.get(1))),
      pickle("2", "XX", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Different))
  }

  test("Diff different scenarios with different example number") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", Some(0), None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", Some(1), None, Set[String]("S", "XX"),
      List[Step](Measure(pickle("2", "XX", 2).getSteps.get(0)), Dummy(pickle("2", "XX", 2).getSteps.get(1))),
      pickle("2", "XX", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Different))
  }

  test("Diff different scenarios with different name and example number") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "s", Some(0), None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("1", "s", 1).getSteps.get(0)), Measure(pickle("1", "s", 1).getSteps.get(1))),
      pickle("1", "s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", Some(1), "XX", Some(1), None, Set[String]("S", "XX"),
      List[Step](Measure(pickle("2", "XX", 2).getSteps.get(0)), Dummy(pickle("2", "XX", 2).getSteps.get(1))),
      pickle("2", "XX", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Different))
  }
}
