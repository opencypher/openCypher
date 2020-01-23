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
package org.opencypher.tools.tck.reporting

import java.net.URI
import java.util

import gherkin.pickles.Pickle
import gherkin.pickles.PickleLocation
import gherkin.pickles.PickleStep
import gherkin.pickles.PickleTag
import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.scalatest.FunSuite
import org.scalatest.Matchers

class CountScenariosTest extends FunSuite with Matchers {
  val dummyPickle = new Pickle("", "", new util.ArrayList[PickleStep](), new util.ArrayList[PickleTag](), new util.ArrayList[PickleLocation]())

  test("Count single top-level scenario without tags") {
    val scenarios: Seq[Scenario] = Seq(Scenario("ftr1", "scr1", List[String](), Set[String](), List[Step](), dummyPickle))
    val expectedCountOutput =
      """Total                 1
        || Feature: ftr1       1""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count single top-level scenario with tag") {
    val scenarios: Seq[Scenario] = Seq(Scenario("ftr1", "scr1", List[String](), Set[String]("A", "@B", "C"), List[Step](), dummyPickle))
    val expectedCountOutput =
      """Total                 1
        || Feature: ftr1       1
        || @B                  1
        || A                   1
        || C                   1""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count single sub-level scenario without tags") {
    val scenarios: Seq[Scenario] = Seq(Scenario("ftr1", "scr1", List[String]("A", "B", "C"), Set[String](), List[Step](), dummyPickle))
    val expectedCountOutput =
      """Total                       1
        || A                         1
        || | B                       1
        || | | C                     1
        || | | | Feature: ftr1       1""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count two top-level scenarios in same feature without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario("ftr1", "scrA", List[String](), Set[String](), List[Step](), dummyPickle),
      Scenario("ftr1", "scrB", List[String](), Set[String](), List[Step](), dummyPickle))
    val expectedCountOutput =
      """Total                 2
        || Feature: ftr1       2""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count three top-level scenarios in two features without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario("ftr1", "scrA", List[String](), Set[String](), List[Step](), dummyPickle),
      Scenario("ftr2", "scrB", List[String](), Set[String](), List[Step](), dummyPickle),
      Scenario("ftr1", "scrC", List[String](), Set[String](), List[Step](), dummyPickle))
    val expectedCountOutput =
      """Total                 3
        || Feature: ftr1       2
        || Feature: ftr2       1""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count three top-level scenarios in two features with tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario("ftr1", "scrA", List[String](), Set[String]("C", "B"), List[Step](), dummyPickle),
      Scenario("ftr2", "scrB", List[String](), Set[String]("C", "D", "A"), List[Step](), dummyPickle),
      Scenario("ftr1", "scrC", List[String](), Set[String]("A", "C"), List[Step](), dummyPickle))
    val expectedCountOutput =
      """Total                 3
        || Feature: ftr1       2
        || Feature: ftr2       1
        || A                   2
        || B                   1
        || C                   3
        || D                   1""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count three mixed-level scenarios without tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario("ftr1", "scrA", List[String](), Set[String](), List[Step](), dummyPickle),
      Scenario("ftr2", "scrB", List[String]("T", "C", "K"), Set[String](), List[Step](), dummyPickle),
      Scenario("ftr1", "scrC", List[String]("T", "A", "K"), Set[String](), List[Step](), dummyPickle))
    val expectedCountOutput =
      """Total                       3
        || T                         2
        || | A                       1
        || | | K                     1
        || | | | Feature: ftr1       1
        || | C                       1
        || | | K                     1
        || | | | Feature: ftr2       1
        || Feature: ftr1             1""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count five mixed-level scenarios with tags") {
    val scenarios: Seq[Scenario] = Seq(
      Scenario("ftr1", "scrA", List[String](), Set[String]("C", "B"), List[Step](), dummyPickle),
      Scenario("ftr2", "scrB", List[String]("T", "C", "K"), Set[String]("C", "D", "A"), List[Step](), dummyPickle),
      Scenario("ftr1", "scrC", List[String]("T", "A", "K"), Set[String]("A", "C"), List[Step](), dummyPickle),
      Scenario("ftr2", "scrD", List[String]("T", "C", "K"), Set[String](), List[Step](), dummyPickle),
      Scenario("ftr3", "scrE", List[String]("T"), Set[String]("B", "A", "C"), List[Step](), dummyPickle))
    val expectedCountOutput =
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
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }

  test("Count scenarios through TCK API") {
    val fooUri: URI = getClass.getResource("cucumber").toURI
    val scenarios: Seq[Scenario] = CypherTCK.parseFeatures(fooUri).flatMap(_.scenarios)
    val expectedCountOutput =
      """Total                      13
        || foo                       8
        || | bar                     6
        || | | boo                   4
        || | | | Feature: Boo        1
        || | | | Feature: Test       3
        || | | Feature: Test         2
        || | dummy                   2
        || | | Feature: Dummy        2
        || Feature: Foo              5
        || @Fail                     3
        || @TestA                    2
        || @TestB                    1
        || @TestC                    3""".stripMargin
    CountScenarios.reportPrettyPrint(scenarios) should equal(expectedCountOutput)
  }
}
