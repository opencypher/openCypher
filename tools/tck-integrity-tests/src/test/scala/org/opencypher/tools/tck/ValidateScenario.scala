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
package org.opencypher.tools.tck

import org.opencypher.tools.tck.api.ExpectError
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.constants.TCKTags
import org.scalatest.AppendedClues
import org.scalatest.Assertion
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.funspec.AsyncFunSpecLike
import org.scalatest.matchers.should.Matchers

trait ValidateScenario extends AppendedClues with Matchers with OptionValues with ValidateSteps {

  private val scenarioNamesByFeature = scala.collection.mutable.HashMap[(List[String], String), scala.collection.mutable.HashMap[String, List[Int]]]()

  def validateScenario(scenario: Scenario): Assertion = {
    withClue("scenario has a number, greater than zero") {
      scenario.number.value should be > 0
    }

    withClue("scenario has a unique name in feature") {
      val key: (List[String], String) = (scenario.categories, scenario.featureName)
      val name = scenario.name
      val lineNumber = scenario.source.getLocation.getLine
      val scenarioNames = scenarioNamesByFeature.getOrElseUpdate(key, scala.collection.mutable.HashMap[String, List[Int]]())
      val lineNumbers = scenarioNames.getOrElseUpdate(name, List[Int]())
      lineNumbers should not contain lineNumber
    }

    withClue("scenario has a `@NegativeTest` tag and a `Then expect error` step or neither") {
      (scenario.steps exists {
        case _: ExpectError => true
        case _ => false
      }) should equal(scenario.tags contains TCKTags.NEGATIVE_TEST)
    }

    validateSteps(scenario.steps, scenario.tags)
  }
}
