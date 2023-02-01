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

import java.net.URI
import java.util

import io.cucumber.core.gherkin.Pickle
import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.scalatest.matchers.should.Matchers

trait GroupTest extends Matchers {
  def createScenario(categories: List[String], featureName: String, number: Option[Int], name: String, index: Option[Int], exampleName: Option[String], tags: Set[String]): Scenario = {
    val dummyPickle: Pickle = new io.cucumber.core.gherkin.Pickle() {
      override val getKeyword: String = ""

      override val getLanguage: String = "EN"

      override val getName: String = "name"

      override val getLocation: io.cucumber.core.gherkin.Location = new io.cucumber.core.gherkin.Location() {
        override val getLine: Int = 1

        override val getColumn: Int = 1
      }

      override val getScenarioLocation: io.cucumber.core.gherkin.Location = new io.cucumber.core.gherkin.Location() {
        override val getLine: Int = 1

        override val getColumn: Int = 1
      }

      override val getSteps: util.List[io.cucumber.core.gherkin.Step] = new util.ArrayList[io.cucumber.core.gherkin.Step]()

      override val getTags: util.List[String] = new util.ArrayList[String]()

      override val getUri: URI = new URI("http://www.opencypher.org/")

      override val getId: String = "id"
    }

    val dummyPickleStep: io.cucumber.core.gherkin.Step = new io.cucumber.core.gherkin.Step() {
      override val getLine: Int = 1

      override val getArgument: io.cucumber.core.gherkin.Argument = new io.cucumber.core.gherkin.DocStringArgument() {
        override val getContent: String = "text"

        override val getContentType: String = ""

        override val getLine: Int = 1
      }

      override val getKeyWord: String = "keyWord"

      override val getType: io.cucumber.core.gherkin.StepType = io.cucumber.core.gherkin.StepType.GIVEN

      override val getPreviousGivenWhenThenKeyWord: String = ""

      override val getText: String = "xyz"

      override val getId: String = "id"
    }

    val dummySteps: List[Step] = List[Step](Dummy(dummyPickleStep), Measure(dummyPickleStep))
    val dummyPath: java.nio.file.Path = new java.io.File("ftr1.feature").toPath

    Scenario(categories, featureName, number, name, index, exampleName, tags, dummySteps, dummyPickle, dummyPath)
  }

}
