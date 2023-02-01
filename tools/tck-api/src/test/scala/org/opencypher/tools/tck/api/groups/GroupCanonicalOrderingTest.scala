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
package org.opencypher.tools.tck.api.groups

import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class GroupCanonicalOrderingTest extends AnyFunSpec with GroupTest {
  private val numberOfShuffles = 5
  private val rand = Random

  private def testOrderingOfSortedShuffles[A](expected: Seq[A], sort: Seq[A] => Seq[A]): Unit = {
    (1 to numberOfShuffles) foreach { i =>
      it(s"when sorted from random order $i") {
        rand.setSeed(i)
        sort(rand.shuffle(expected)) should equal(expected)
      }
    }
  }

  describe("ScenarioCategory groups should be ordered by their name") {
    val expected = Seq(
      ScenarioCategory("aac", Total),
      ScenarioCategory("abc", Total),
      ScenarioCategory("c3", Total),
      ScenarioCategory("xy", Total),
    )

    testOrderingOfSortedShuffles(expected, (s: Seq[ScenarioCategory]) => s.sorted)
  }

  describe("Feature groups should be ordered by their main name, number, and description contained in their name") {

    val expected = Seq(
      Feature("Abc1 - Alphabet fun", Total),
      Feature("Abc3", Total),
      Feature("Abc3 - Alphabet fun, too", Total),
      Feature("Abc22 - Alphabet fun 22", Total),
      Feature("Bc", Total),
      Feature("Bc1 - not DC", Total),
      Feature("Bc4 - nor JC", Total),
      Feature("Feature with a Space11 - three spaces actually", Total),
      Feature("Feature with a Space11 - three spaces actually, again", Total),
      Feature("Feature with a Space22 - three spaces actually, again", Total),
    )

    testOrderingOfSortedShuffles(expected, (s: Seq[Feature]) => s.sorted)
  }

  describe("Numbered groups should be ordered by their number") {
    val ftr = Feature("ftr", Total)

    val expected = Seq(
      ScenarioItem(createScenario(List(), ftr.name, Some(1), "alpha", None, None, Set()), ftr),
      ScenarioOutline(Some(1), "gamma", ftr),
      ScenarioItem(createScenario(List(), ftr.name, Some(2), "alpha", None, None, Set()), ftr),
      ScenarioOutline(Some(2), "beta", ftr),
      ScenarioItem(createScenario(List(), ftr.name, Some(2), "gamma", None, None, Set()), ftr),
      ScenarioOutline(Some(3), "gamma", ftr),
      ScenarioItem(createScenario(List(), ftr.name, Some(23), "alpha", None, None, Set()), ftr),
      ScenarioItem(createScenario(List(), ftr.name, Some(23), "delta", None, None, Set()), ftr),
      ScenarioItem(createScenario(List(), ftr.name, None, "aaa", None, None, Set()), ftr),
      ScenarioOutline(None, "ac", ftr),
      ScenarioItem(createScenario(List(), ftr.name, None, "uwvxyz", None, None, Set()), ftr),
    )

    testOrderingOfSortedShuffles(expected, (s: Seq[Numbered]) => s.sorted)
  }

  describe("ExampleItem groups should be ordered by their index") {
    val scrOut = ScenarioOutline(Some(1), "alpha", Feature("ftr", Total))
    val scr = createScenario(List(), scrOut.parentGroup.name, None, scrOut.name, Some(1), None, Set())

    val expected = Seq(
      ExampleItem(1, Some("xyz"), scr, scrOut),
      ExampleItem(2, Some("abc"), scr, scrOut),
      ExampleItem(4, None, scr, scrOut),
      ExampleItem(77, Some("123"), scr, scrOut),
      ExampleItem(123, Some("edf"), scr, scrOut),
    )

    testOrderingOfSortedShuffles(expected, (s: Seq[ExampleItem]) => s.sorted)
  }

  describe("Tag groups should be ordered by their name") {
    val expected = Seq(
      Tag("aac"),
      Tag("abc"),
      Tag("c3"),
      Tag("delta"),
      Tag("xy"),
    )

    testOrderingOfSortedShuffles(expected, (s: Seq[Tag]) => s.sorted)
  }

  describe("Mixed groups should be ordered by Total, ScenarioCategory, Feature, Tag") {
    val expected = Seq(
      Total,
      ScenarioCategory("Alpha", Total),
      ScenarioCategory("Beta", Total),
      ScenarioCategory("Gamma", Total),
      Feature("Alpha", Total),
      Feature("Beta", Total),
      Feature("Gamma", Total),
      Tag("Alpha"),
      Tag("Beta"),
      Tag("Gamma"),
    )

    testOrderingOfSortedShuffles(expected, (s: Seq[Group]) => s.sorted)
  }
}
