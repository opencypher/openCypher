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
package org.opencypher.tools.tck.inspection.diff

import org.opencypher.tools.tck.api.Pickle
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.inspection.diff.ScenarioDiffTag._

sealed trait ScenarioDiffTag

object ScenarioDiffTag {
  case object SourceUnchanged extends ScenarioDiffTag
  case object Unchanged extends ScenarioDiffTag
  case object Moved extends ScenarioDiffTag
  case object NumberChanged extends ScenarioDiffTag
  case object Retagged extends ScenarioDiffTag
  case object StepsChanged extends ScenarioDiffTag
  case object SourceChanged extends ScenarioDiffTag
  case object ExampleIndexChanged extends ScenarioDiffTag
  case object ExampleNameChanged extends ScenarioDiffTag
  case object PotentiallyRenamed extends ScenarioDiffTag
  case object Different extends ScenarioDiffTag
}

case class ScenarioDiff(before: Scenario, after: Scenario) extends Diff[Scenario] {

  lazy val categories: TreePathDiff[String] = TreePathDiff(before.categories, after.categories)

  lazy val featureName: ElementDiff[String] = ElementDiff(before.featureName, after.featureName)

  lazy val number: ElementDiff[Option[Int]] = ElementDiff(before.number, after.number)

  lazy val name: ElementDiff[String] = ElementDiff(before.name, after.name)

  lazy val exampleIndex: ElementDiff[Option[Int]] = ElementDiff(before.exampleIndex, after.exampleIndex)

  lazy val exampleName: ElementDiff[Option[String]] = ElementDiff(before.exampleName, after.exampleName)

  lazy val tags: SetDiff[String] = SetDiff(before.tags, after.tags)

  lazy val steps: LCSbasedListDiff[Step] = LCSbasedListDiff(before.steps, after.steps)

  lazy val diffTags: Set[ScenarioDiffTag] = diffTags(before, after)

  lazy val potentialDuplicate: Boolean = diffTags subsetOf Set[ScenarioDiffTag](Moved, Retagged, ExampleIndexChanged, ExampleNameChanged, StepsChanged, PotentiallyRenamed)

  private def diffTags(before: Scenario, after: Scenario): Set[ScenarioDiffTag] = {
    val diff = Set[ScenarioDiffTag](Unchanged, SourceUnchanged, SourceChanged, Moved, NumberChanged, Retagged, StepsChanged, ExampleIndexChanged, ExampleNameChanged, PotentiallyRenamed).filter {
      case Unchanged => before.equals(after)
      case SourceUnchanged =>
        before.equals(after) &&
          Pickle(after.source, withLocation = true) == Pickle(before.source, withLocation = true)
      case SourceChanged =>
        before.equals(after) &&
          Pickle(after.source, withLocation = true) != Pickle(before.source, withLocation = true)
      case Moved =>
        (categories.changed || featureName.changed) &&
          !name.changed &&
          !exampleIndex.changed
      case NumberChanged =>
        !name.changed &&
          number.changed
      case Retagged =>
        !name.changed &&
          !exampleIndex.changed &&
          tags.changed
      case StepsChanged =>
        !name.changed &&
          !exampleIndex.changed &&
          steps.changed
      case ExampleIndexChanged =>
        !categories.changed &&
          !featureName.changed &&
          !number.changed &&
          !name.changed &&
          exampleIndex.changed &&
          !tags.changed &&
          !steps.changed &&
          Pickle(after.source) == Pickle(before.source)
      case ExampleNameChanged =>
        !categories.changed &&
          !featureName.changed &&
          !number.changed &&
          !name.changed &&
          !exampleIndex.changed &&
          exampleName.changed &&
          !tags.changed &&
          !steps.changed &&
          Pickle(after.source) == Pickle(before.source)
      case PotentiallyRenamed =>
        name.changed &&
          !steps.changed
      case _ => false
    }
    if(diff.isEmpty)
      Set[ScenarioDiffTag](Different)
    else
      diff
  }

  override def tag: ElementaryDiffTag =
    if(diffTags == Set(Unchanged)) {
      ElementaryDiffTag.Unchanged
    } else if(diffTags == Set(Different)) {
      ElementaryDiffTag.Different
    } else {
      ElementaryDiffTag.Changed
    }
}