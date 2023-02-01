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
package org.opencypher.tools.tck

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Scenario
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.language.implicitConversions
import scala.language.reflectiveCalls

class ValidateScenarioTest extends AnyFunSpecLike with Matchers {
  private val correctScenario: Scenario = {
    CypherTCK.allTckScenarios.filter(scenario => {
      scenario.featureName.contains("Create5 - Multiple hops create patterns") &&
      scenario.number.contains(1) &&
      scenario.name.contains("Create a pattern with multiple hops")
    }).head
  }

  private def fixture =
    new {
      val validator: ValidateScenario = new ValidateScenario() {}
    }

  it("should detect scenario with the same name and number in the same feature") {
    val v = fixture.validator
    assertCorrect(v, correctScenario.copy(categories = List("a", "b"), featureName = "ftr", number = Some(1), name = "scenario", exampleIndex = None))
    assertIncorrect(v, correctScenario.copy(categories = List("a", "b"), featureName = "ftr", number = Some(1), name = "scenario", exampleIndex = None))
  }

  it("should detect scenario with the same name and different number in the same feature") {
    val v = fixture.validator
    assertCorrect(v, correctScenario.copy(categories = List("a", "b"), featureName = "ftr", number = Some(1), name = "scenario", exampleIndex = None))
    assertIncorrect(v, correctScenario.copy(categories = List("a", "b"), featureName = "ftr", number = Some(2), name = "scenario", exampleIndex = None))
  }

  it("should pass scenario with the same name and different example index in the same feature") {
    val v = fixture.validator
    assertCorrect(v, correctScenario.copy(categories = List("a", "b"), featureName = "ftr", number = Some(1), name = "scenario", exampleIndex = Some(1)))
    assertCorrect(v, correctScenario.copy(categories = List("a", "b"), featureName = "ftr", number = Some(1), name = "scenario", exampleIndex = Some(2)))
  }

  private def assertCorrect(validator: ValidateScenario, scenario: Scenario) = {
    validator.validateScenario(scenario)
    succeed
  }

  private def assertIncorrect(validator: ValidateScenario, scenario: Scenario) = {
    an [TestFailedException] should be thrownBy validator.validateScenario(scenario)
  }
}
