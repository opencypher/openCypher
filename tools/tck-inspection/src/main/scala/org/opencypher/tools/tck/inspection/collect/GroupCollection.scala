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
package org.opencypher.tools.tck.inspection.collect

import org.opencypher.tools.tck.api.Scenario

object GroupCollection extends (Seq[Scenario] => Map[Group, Seq[Scenario]]) {
  def apply(scenarios: Seq[Scenario]): Map[Group, Seq[Scenario]] = {
    // collect individual group for each scenario as 2-tuples of (Scenario,CountCategory)
    val individualCounts: Seq[(Scenario,Group)] = scenarios.flatMap(scenario => {
      // category
      def mapToGroups(categories: List[String], parent: Group): Seq[(Scenario, Group)] = {
        categories match {
          case Nil => Seq[(Scenario, Group)]()
          case category :: remainingCategories =>
            val categoryGroup = (scenario, ScenarioCategory(category, parent.indent + 1, Some(parent)))
            categoryGroup +: mapToGroups(remainingCategories, categoryGroup._2)
        }
      }
      val categoryGroups: Seq[(Scenario, Group)] = mapToGroups(scenario.categories, Total)
      // feature
      val feature: Feature = {
        val indent = categoryGroups.lastOption.map(_._2.indent).getOrElse(0) + 1
        Feature(scenario.featureName, indent, Some(categoryGroups.lastOption.getOrElse((scenario, Total))._2))
      }
      // tags
      val tagGroups: Seq[(Scenario, Group)] = scenario.tags.map(tag => (scenario, Tag(tag))).toSeq

      (scenario, Total) +: (scenario, feature) +: (categoryGroups ++ tagGroups)
    })
    // group pairs by group
    val allGroups = individualCounts.groupBy(_._2).mapValues(_.map(_._1))
    allGroups
  }
}
