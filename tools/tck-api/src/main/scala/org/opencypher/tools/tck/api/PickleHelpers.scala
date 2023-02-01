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
package org.opencypher.tools.tck.api

import io.cucumber.core.gherkin.{DataTableArgument, DocStringArgument}
import io.cucumber.plugin.event.StepArgument

import scala.jdk.CollectionConverters._

trait PickleArgument

case object PickleArgument {
  def apply(pickleArgument: StepArgument, withLocation: Boolean = false): Option[PickleArgument] = pickleArgument match {
    case ps: DocStringArgument => Some(PickleString(ps.getContent, ps.getContentType, if(withLocation) Some(ps.getLine) else None))
    case pt: DataTableArgument => Some(PickleTable(pt.cells().asScala.map(_.asScala.toList).toList, if(withLocation) Some(pt.getLine) else None))
    case _ => None
  }
}

case class PickleString(content: String, contentType: String, location: Option[Int]) extends PickleArgument

case class PickleTable(row: List[List[String]], location: Option[Int]) extends PickleArgument

case class PickleStep(text: String, argument: Option[PickleArgument], locations: Option[Int])

case object PickleStep {
  def apply(step: io.cucumber.core.gherkin.Step, withLocation: Boolean = false): PickleStep =
    PickleStep(
      step.getText,
      PickleArgument(step.getArgument, withLocation),
      if(withLocation) Some(step.getLine) else None
    )
}

case class PickleLocation(line: Int, column: Int)

case object PickleLocation {
  def apply(location: io.cucumber.core.gherkin.Location): PickleLocation = {
    PickleLocation(location.getLine, location.getColumn)
  }
}

case class Pickle(name: String, language: String, steps: List[PickleStep], tags: List[String], location: Option[PickleLocation])

case object Pickle {
  def apply(pickle: io.cucumber.core.gherkin.Pickle, withLocation: Boolean = false): Pickle =
    Pickle(
      pickle.getName,
      pickle.getLanguage,
      pickle.getSteps.asScala.map(s => PickleStep(s, withLocation)).toList,
      pickle.getTags.asScala.toList,
      if(withLocation) Some(PickleLocation(pickle.getScenarioLocation)) else None
    )
}
