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

import scala.collection.compat._

case class TckTree(scenarios: Seq[Scenario]) extends GroupTreeBasics {
  lazy val groups: Set[Group] = scenarios.flatMap(scenario => {
    // category
    def mapToCategoryGroups(categories: List[String], parent: ContainerGroup): Seq[ContainerGroup] = {
      categories match {
        case Nil => Seq[ContainerGroup]()
        case category :: remainingCategories =>
          val categoryGroup = ScenarioCategory(category, parent)
          categoryGroup +: mapToCategoryGroups(remainingCategories, categoryGroup)
      }
    }
    val categoryGroups: Seq[ContainerGroup] = mapToCategoryGroups(scenario.categories, Total)

    // feature
    val feature: Feature = {
      Feature(scenario.featureName, categoryGroups.lastOption.getOrElse(Total))
    }

    // tags
    val tagGroups = scenario.tags.map(tag => Tag(tag)).toSeq

    val tagsAndFeature: Seq[ScenarioContainer] = tagGroups :+ feature

    // scenario outline
    val outline: Seq[Group] = tagsAndFeature.flatMap(parent =>
      scenario.exampleIndex.
        map(i => {
          val o = ScenarioOutline(scenario.number, scenario.name, parent)
          val e = ExampleItem(i, scenario.exampleName, scenario, o)
          Seq[Group](o, e)
        }).getOrElse(Seq[Group](ScenarioItem(scenario, parent)))
    )

    //putting all together
    Total +: (categoryGroups ++ outline ++ tagsAndFeature)
  }).toSet

  lazy val groupedScenarios: Map[Group, Set[Scenario]] = {
    def collectScenario(group: Group): Map[Group, Set[Scenario]] = {
      group match {
        case i: Item => Map(i -> Set(i.scenario))
        case g =>
          val children = groupChildren(g).flatMap(collectScenario).toMap
          Map(g -> children.values.flatten.toSet) ++ children
      }
    }
    collectScenario(Total)
  }

  def filter(filter: Group => Boolean): TckTree = {
    def collectGroups(g: Group): Set[Group] = {
      if(filter(g))
        groupChildren(g).flatMap(collectGroups) + g
      else
        Set.empty
    }
    val filteredGroups = collectGroups(Total)

    def scenarioAndTheirItems(groups: Set[Group]) =
      groups.collect {
        case i:Item => (i.scenario, i)
      }.groupMap(_._1)(_._2)

    // get scenarios with their set of items from the original groups
    val scenariosFromGroups = scenarioAndTheirItems(groups)
    // get scenarios with their set of items from the filtered groups
    val scenariosFromFilteredGroups = scenarioAndTheirItems(filteredGroups)
    // only keep scenarios from the filtered groups that still have the same set of items as in the original groups
    val filteredScenarios = scenariosFromFilteredGroups.collect {
      case (scenario, items) if scenariosFromGroups.get(scenario).contains(items) => scenario
    }.toSeq
    // build new tree
    TckTree(filteredScenarios)
  }
}

object TckTree {
  def apply(scenarios: Set[Scenario]): TckTree = TckTree(scenarios.toSeq)
}

trait GroupTreeBasics {
  def groups: Set[Group]

  lazy val groupsOrderedDepthFirst: Seq[Group] = {
    def orderDepthFirst(currentGroup: Group): Seq[Group] =
      currentGroup +: groupChildren(currentGroup).toSeq.sorted.flatMap(orderDepthFirst)

    orderDepthFirst(Total)
  }

  lazy val groupChildren: Map[Group, Set[Group]] = {
    val containerGroups = groups.collect {
      case cg: ContainedGroup => cg
    }.groupBy(_.parentGroup).asInstanceOf[Map[Group, Set[Group]]]

    groups.map(g => g -> containerGroups.getOrElse(g, Set.empty[Group])).toMap
  }
}