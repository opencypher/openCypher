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

import org.opencypher.tools.tck.api.ExpectError
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.constants.TCKTags
import org.scalatest.AppendedClues
import org.scalatest.Assertion
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers

trait ValidateScenario extends AppendedClues with Matchers with OptionValues with ValidateSteps {

  private val scenarioNamesByFeature = scala.collection.mutable.HashMap[(List[String], String), List[(String, Option[Int])]]()

  def validateScenario(scenario: Scenario): Assertion = {
    withClue("scenario has a number, greater than zero") {
      scenario.number.value should be > 0
    }

    withClue("scenario has a unique name in feature") {
      val featureSignature: (List[String], String) = (scenario.categories, scenario.featureName)
      val scenarioSignature = (scenario.name, scenario.exampleIndex)
      val scenarioSignaturesBefore = scenarioNamesByFeature.getOrElseUpdate(featureSignature, List[(String, Option[Int])]())
      scenarioNamesByFeature.update(featureSignature, scenarioSignaturesBefore :+ scenarioSignature)
      scenarioSignaturesBefore should not contain scenarioSignature
    }

    withClue("scenario with an example name should have an example index") {
      (scenario.exampleName, scenario.exampleIndex) should matchPattern {
        case (Some(_), Some(_)) =>
        case (None, _) =>
        // (Some(_), None) is the not allowed case
      }
    }

    validateSteps(scenario.steps, scenario.tags)
  }
}
