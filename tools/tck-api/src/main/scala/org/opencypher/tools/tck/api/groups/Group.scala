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

import org.opencypher.tools.tck.api.Scenario

trait Group {
  def name: String
  def description: String = name
  def indent: Int
  def parent: Option[Group]
  def children(implicit tck: TckTree): Set[Group] = tck.groupChildren.getOrElse(this, Set[Group]())

  override def toString: String = name
}

object Group {
  implicit def canonicalOrdering[A <: Group]: Ordering[A] = (x: A, y: A) => (x, y) match {
    case (x: ScenarioCategory, y: ScenarioCategory) => ScenarioCategory.canonicalOrdering.compare(x, y)
    case (x: Feature, y: Feature) => Feature.canonicalOrdering.compare(x, y)
    case (x: Numbered, y: Numbered) => Numbered.canonicalOrdering.compare(x, y)
    case (x: ExampleItem, y: ExampleItem) => ExampleItem.canonicalOrdering.compare(x, y)
    case (x: Tag, y: Tag) => Tag.canonicalOrdering.compare(x, y)
    case (Total, _) => -1
    case (_, Total) => 1
    case (_: ScenarioCategory, _) => -1
    case (_, _: ScenarioCategory) => 1
    case (_: Feature, _) => -1
    case (_, _: Feature) => 1
    case (_: Numbered, _) => -1
    case (_, _: Numbered) => 1
    case (_: ExampleItem, _) => -1
    case (_, _: ExampleItem) => 1
    case (_: Tag, _) => -1
    case (_, _: Tag) => 1
    case (_, _) => 0
  }
}

trait ContainerGroup extends Group

trait ContainedGroup extends Group {
  def parentGroup: ContainerGroup
  override lazy val indent: Int = parentGroup.indent + 1
  override lazy val parent: Option[Group] = Some(parentGroup)
  override def toString: String = parentGroup.toString + name + "/"
}

trait ScenarioContainer extends ContainerGroup

case object Total extends ContainerGroup {
  override val name = "Total"
  override val indent = 0
  override val parent: Option[Group] = None
  override def toString: String = "/"
}

case class ScenarioCategory(override val name: String, override val parentGroup: ContainerGroup) extends ContainedGroup with ContainerGroup {
  override def toString: String = parentGroup.toString + name + "/"
}

object ScenarioCategory {
  implicit def canonicalOrdering[A <: ScenarioCategory]: Ordering[A] = Ordering.by(_.name)
}

case class Feature(override val name: String, override val parentGroup: ContainerGroup) extends ContainedGroup with ScenarioContainer

object Feature {
  private val namePatternWithDescription = "^([^0-9]+)([0-9]+)( - .+)$".r
  private val namePatternWithoutDescription = "^([^0-9]+)([0-9]+)$".r
  implicit def canonicalOrdering[A <: Feature]: Ordering[A] = Ordering.by(f => {
    f.name match {
      case namePatternWithDescription(name, number, description)
        => (name, number.toInt, description)
      case namePatternWithoutDescription(name, number)
        => (name, number.toInt, "")
      case _
        => (f.name, -1, "")
    }
  })
}

trait Numbered extends ContainedGroup {
  def number: Option[Int]

  override def description = s"${number.map(n => "["+n+"] ").getOrElse("")}$name"

  override def toString: String = parentGroup.toString + description + "/"
}

object Numbered {
  implicit def canonicalOrdering[A <: Numbered]: Ordering[A] = Ordering.by(n => (n.number.getOrElse(Int.MaxValue), n.name))
}

case class ScenarioOutline(override val number: Option[Int], override val name: String, override val parentGroup: ScenarioContainer) extends Numbered with ContainedGroup with ContainerGroup

trait Item extends ContainedGroup {
  def scenario: Scenario
}

case class ScenarioItem(override val scenario: Scenario, override val parentGroup: ScenarioContainer) extends Numbered with Item {
  override def number: Option[Int] = scenario.number
  override def name: String = scenario.name
}

case class ExampleItem(index: Int, exampleName: Option[String], override val scenario: Scenario, override val parentGroup: ScenarioOutline) extends Item {
  override def description = s"#$index${exampleName.map(n => " ("+n+")").getOrElse("")}"
  override def name: String = s"#$index${exampleName.map(n => " ("+n+")").getOrElse("")}"
}

object ExampleItem {
  implicit def canonicalOrdering[A <: ExampleItem]: Ordering[A] = Ordering.by(_.index)
}

case class Tag(override val name: String) extends ContainedGroup with ScenarioContainer {
  override val parentGroup: ContainerGroup = Total
}

object Tag {
  implicit def canonicalOrdering[A <: Tag]: Ordering[A] = Ordering.by(_.name)
}