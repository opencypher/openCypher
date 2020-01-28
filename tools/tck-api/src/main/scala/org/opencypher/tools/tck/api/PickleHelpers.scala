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
package org.opencypher.tools.tck.api

import scala.collection.JavaConverters._

case class PickleLocation(line: Int, column: Int)

case object PickleLocation {
  def apply(pickleLocation: gherkin.pickles.PickleLocation): PickleLocation =
    PickleLocation(pickleLocation.getLine, pickleLocation.getColumn)

  def apply(pickleLocation: gherkin.pickles.PickleLocation, withLocation: Boolean): Option[PickleLocation] =
    if(withLocation)
      Some(PickleLocation(pickleLocation.getLine, pickleLocation.getColumn))
    else
      None
}

trait PickleArgument

case object PickleArgument {
  def apply(pickleArgument: gherkin.pickles.Argument, withLocation: Boolean = false): PickleArgument = pickleArgument match {
    case ps: gherkin.pickles.PickleString => PickleString(ps.getContent, ps.getContentType, PickleLocation(ps.getLocation, withLocation))
    case pt: gherkin.pickles.PickleTable => PickleTable(
      pt.getRows.asScala.map(
        row => row.getCells.asScala.map(
          cell => PickleCell(cell.getValue, PickleLocation(cell.getLocation, withLocation))
        ).toList
      ).toList,
      pt.getRows.asScala.headOption.flatMap(
        _.getCells.asScala.headOption.flatMap(
          c => PickleLocation(c.getLocation, withLocation)
        )
      )
    )
  }
}

case class PickleString(content: String, contentType: String, location: Option[PickleLocation]) extends PickleArgument

case class PickleTable(row: List[List[PickleCell]], location: Option[PickleLocation]) extends PickleArgument

case class PickleCell(value: String, location: Option[PickleLocation])

case class PickleStep(text: String, arguments: List[PickleArgument], locations: Option[List[PickleLocation]])

case object PickleStep {
  def apply(pickleStep: gherkin.pickles.PickleStep, withLocation: Boolean = false): PickleStep =
    PickleStep(
      pickleStep.getText,
      pickleStep.getArgument.asScala.map(a => PickleArgument(a, withLocation)).toList,
      if(withLocation)
        Some(pickleStep.getLocations.asScala.map(l => PickleLocation(l)).toList)
      else
        None
    )
}

case class Pickle(name: String, language: String, steps: List[PickleStep], tags: List[String], location: Option[List[PickleLocation]])

case object Pickle {
  def apply(pickle: gherkin.pickles.Pickle, withLocation: Boolean = false): Pickle =
    Pickle(
      pickle.getName,
      pickle.getLanguage,
      pickle.getSteps.asScala.map(s => PickleStep(s, withLocation)).toList,
      pickle.getTags.asScala.map(_.getName).toList,
      if(withLocation)
        Some(pickle.getLocations.asScala.map(l => PickleLocation(l)).toList)
      else
        None
    )
}
