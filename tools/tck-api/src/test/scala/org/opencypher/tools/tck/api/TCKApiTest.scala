/*
 * Copyright (c) 2015-2021 "Neo Technology,"
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
package org.opencypher.tools.tck.api

import org.opencypher.tools.tck.constants.TCKTags

import java.net.URI
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TCKApiTest extends AnyFunSuite with Matchers {
  private val fooUri: URI = getClass.getResource("..").toURI
  private val scenarios: Seq[Scenario] = CypherTCK.parseFeatures(fooUri).flatMap(_.scenarios)

  test("category of top-level scenarios") {
    val topLevelScenarios = scenarios.filter(_.featureName == "Foo")
    topLevelScenarios.foreach(_.categories should equal(List[String]()))
  }

  test("category of some-level scenarios") {
    val someLevelScenarios = scenarios.filter(_.featureName == "Test")
    someLevelScenarios.foreach(_.categories should equal(List("foo", "bar", "boo")))
  }

  test("example index of non-outline scenarios") {
    val nonOutlineScenarios = scenarios.filterNot(s => s.featureName == "Outline" && s.name.startsWith("Outline Test"))
    all (nonOutlineScenarios) should have ('exampleIndex (None))
  }

  test("example index of outline scenarios") {
    val outlineScenarios = scenarios.filter(s => s.featureName == "Outline" && s.name == "Outline Test")
    outlineScenarios.map(_.exampleIndex) should equal(Seq(Some(0), Some(1), Some(2)))
    outlineScenarios.foreach(s => {
      outlineScenarios.filter(_.exampleIndex.get > s.exampleIndex.get).foreach(s2 => {
        s2.source.getLocation.getLine > s.source.getLocation.getLine
      })
    })
  }

  test("example name of non-outline scenarios should have not an example name") {
    val nonOutlineScenarios = scenarios.filterNot(s => s.featureName == "Outline" && s.name.startsWith("Outline Test"))
    nonOutlineScenarios.foreach(s => s.exampleName should be (None))
  }

  test("example name of outline scenarios with named examples should have an example name") {
    val namedOutlineScenarios = scenarios.filter(s => s.featureName == "Outline" && s.name.startsWith("Outline Test") && s.tags.contains("@fullyNamed"))
    namedOutlineScenarios.foreach(s => s.exampleName should not be None)
  }

  test("scenarios with an example name should have an example index") {
    val scenariosWithExampleName = scenarios.filter(_.exampleName.nonEmpty)
    scenariosWithExampleName.foreach(s => s.exampleIndex should not be None)
  }

  test("numbered scenarios have a number") {
    val numberedScenarios = scenarios.filter(s => s.tags.contains("@numbered"))
    numberedScenarios.foreach(s => s.number should not be None)
  }

  test("sourceFile of top-level scenarios") {
    val someLevelScenarios = scenarios.filter(_.featureName == "Foo")
    someLevelScenarios.foreach(_.sourceFile should
      equal(java.nio.file.Paths.get(fooUri).resolve("Foo.feature")))
  }

  test("sourceFile of some-level scenarios") {
    val someLevelScenarios = scenarios.filter(_.featureName == "Test")
    someLevelScenarios.foreach(_.sourceFile should
      equal(java.nio.file.Paths.get(fooUri).resolve("foo/bar/boo/Test.feature")))
  }

  test("scenarios with an ExpectError step have the TCKTags.NEGATIVE_TEST tag") {
    val negativeTestScenarios = scenarios.filter(_.name == "Fail")
    negativeTestScenarios.foreach(_.tags should contain (TCKTags.NEGATIVE_TEST))
  }
}
