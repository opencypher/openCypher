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

import gherkin.pickles
import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.inspection.diff.ScenarioDiffTag._
import org.scalatest.FunSuite
import org.scalatest.Matchers

class ScenarioDiffTest extends FunSuite with Matchers {
  val rand = new scala.util.Random(1)

  val noPickleSteps = new util.ArrayList[gherkin.pickles.PickleStep]()
  val noPickleTags = new util.ArrayList[gherkin.pickles.PickleTag]()
  val noPickleLocations = new util.ArrayList[gherkin.pickles.PickleLocation]()

  def pickleLocation(line: Int, column: Int): pickles.PickleLocation = {
    new gherkin.pickles.PickleLocation(line, column)
  }

  def pickle(name: String, loc: Int): pickles.Pickle = {
    val pickleArguments = new util.ArrayList[gherkin.pickles.Argument]()
    pickleArguments.add(new gherkin.pickles.PickleString(pickleLocation(1, loc), "a1", "x"))
    pickleArguments.add(new gherkin.pickles.PickleString(pickleLocation(1, loc), "a2", "x"))

    val stepLocations1 = new util.ArrayList[gherkin.pickles.PickleLocation]()
    stepLocations1.add(pickleLocation(2, loc))
    val stepLocations2 = new util.ArrayList[gherkin.pickles.PickleLocation]()
    stepLocations2.add(pickleLocation(3, loc))

    val steps = new util.ArrayList[gherkin.pickles.PickleStep]()
    steps.add(new gherkin.pickles.PickleStep("exec", pickleArguments, stepLocations1))
    steps.add(new gherkin.pickles.PickleStep("result", new util.ArrayList[gherkin.pickles.Argument](), stepLocations2))

    val tags = new util.ArrayList[gherkin.pickles.PickleTag]()
    tags.add(new gherkin.pickles.PickleTag(pickleLocation(4, loc), "S"))
    tags.add(new gherkin.pickles.PickleTag(pickleLocation(5, loc), "T"))

    val locations = new util.ArrayList[gherkin.pickles.PickleLocation]()
    locations.add(pickleLocation(6, loc))
    locations.add(pickleLocation(7, loc))

    new gherkin.pickles.Pickle(name, "x", steps, tags, locations)
  }

  test("Diff equal scenarios not differing in source") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Unchanged, SourceUnchanged))
  }

  test("Diff equal scenarios differing in source") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 2).getSteps.get(0)), Measure(pickle("s", 2).getSteps.get(1))),
      pickle("s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Unchanged, SourceChanged))
  }

  test("Diff scenarios differing in category only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("XX", "A"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 2).getSteps.get(0)), Measure(pickle("s", 2).getSteps.get(1))),
      pickle("s", 2), new java.io.File("XX/A/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Moved))
  }

  test("Diff scenarios differing in tags only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("exec", 1).getSteps.get(0)), Measure(pickle("result", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "XX"),
      List[Step](Dummy(pickle("s", 2).getSteps.get(0)), Measure(pickle("s", 2).getSteps.get(1))),
      pickle("s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Retagged))
  }

  test("Diff scenarios differing in kind of steps only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Measure(pickle("s", 2).getSteps.get(0)), Dummy(pickle("s", 2).getSteps.get(1))),
      pickle("s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](StepsChanged))
  }

  test("Diff scenarios differing in content of steps only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 2).getSteps.get(1)), Measure(pickle("s", 2).getSteps.get(0))),
      pickle("s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](StepsChanged))
  }

  test("Diff equal scenarios differing in example index only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", Some(0), Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", Some(1), Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 2).getSteps.get(0)), Measure(pickle("s", 2).getSteps.get(1))),
      pickle("s", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](ExampleIndexChanged))
  }

  test("Diff scenarios differing in categories, tags, and content of steps only") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("XX", "B"), "f", "s", None, Set[String]("S", "XX"),
      List[Step](Dummy(pickle("s", 2).getSteps.get(1)), Measure(pickle("s", 2).getSteps.get(0))),
      pickle("s", 2), new java.io.File("XX/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Moved, Retagged, StepsChanged))
  }

  test("Diff different scenarios with different name") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", None, Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "XX", None, Set[String]("S", "XX"),
      List[Step](Measure(pickle("XX", 2).getSteps.get(0)), Dummy(pickle("XX", 2).getSteps.get(1))),
      pickle("XX", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Different))
  }

  test("Diff different scenarios with different example number") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", Some(0), Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", Some(1), Set[String]("S", "XX"),
      List[Step](Measure(pickle("XX", 2).getSteps.get(0)), Dummy(pickle("XX", 2).getSteps.get(1))),
      pickle("XX", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Different))
  }

  test("Diff different scenarios with different name and example number") {
    val scenarioBefore: Scenario = Scenario(
      List[String]("A", "B"), "f", "s", Some(0), Set[String]("S", "T"),
      List[Step](Dummy(pickle("s", 1).getSteps.get(0)), Measure(pickle("s", 1).getSteps.get(1))),
      pickle("s", 1), new java.io.File("A/B/f.feature").toPath
    )
    val scenarioAfter: Scenario = Scenario(
      List[String]("A", "B"), "f", "XX", Some(1), Set[String]("S", "XX"),
      List[Step](Measure(pickle("XX", 2).getSteps.get(0)), Dummy(pickle("XX", 2).getSteps.get(1))),
      pickle("XX", 2), new java.io.File("A/B/f.feature").toPath
    )

    ScenarioDiff(scenarioBefore, scenarioAfter).diffTags should equal(Set[ScenarioDiffTag](Different))
  }
}
