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
package org.opencypher.tools.tck.reporting

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Scenario

trait CountCategory {
  def name: String
  def indent: Int
  def parent: Option[CountCategory]

  override def toString: String = name
}

case object Total extends CountCategory {
  val name = "Total"
  val indent = 0
  val parent: Option[CountCategory] = None
}

case class Tag(name: String) extends CountCategory {
  val indent = 1
  val parent: Option[CountCategory] = Some(Total)
}

case class Feature(name: String, indent: Int, parent: Option[CountCategory]) extends CountCategory {
  override def toString: String = "Feature: " + name
}

case class ScenarioCategory(name: String, indent: Int, parent: Option[CountCategory]) extends CountCategory

case class GroupDiff(unchanged: Set[Scenario], changed: Set[(Scenario, Scenario)], added: Set[Scenario], removed: Set[Scenario])

/*
 * This is a tiny tool to count TCK scenarios in the list returned by `CypherTCK.allTckScenarios`.
 * At the moment it count scenarios by total, feature, and tags and outputs the counts to stdout.
 * Run object `CountScenarios` to run the tool.
 */
case object CountScenarios {
  def main(args: Array[String]): Unit = {
    if(args.length == 0) {
      println(reportCountsInPrettyPrint(collect(CypherTCK.allTckScenarios)))
    } else if(args.length == 1) {
      println(reportCountsInPrettyPrint(collect(CypherTCK.allTckScenariosFromFilesystem(args(0)))))
    } else if(args.length == 2) {
      println(reportCountsInPrettyPrint(collect(CypherTCK.allTckScenariosFromFilesystem(args(0)))))
    }
  }

  def collect(scenarios: Seq[Scenario]): Map[CountCategory, Seq[Scenario]] = {
    // collect individual group for each scenario as 2-tuples of (Scenario,CountCategory)
    val individualCounts: Seq[(Scenario,CountCategory)] = scenarios.flatMap(scenario => {
      // category
      def mapToCountCategories(categories: List[String], parent: CountCategory): Seq[(Scenario, CountCategory)] = {
        categories match {
          case Nil => Seq[(Scenario, CountCategory)]()
          case category :: remainingCategories =>
            val categoryGroup = (scenario, ScenarioCategory(category, parent.indent + 1, Some(parent)))
            categoryGroup +: mapToCountCategories(remainingCategories, categoryGroup._2)
        }
      }
      val categoryGroups: Seq[(Scenario, CountCategory)] = mapToCountCategories(scenario.categories, Total)
      // feature
      val feature: Feature = {
        val indent = categoryGroups.lastOption.map(_._2.indent).getOrElse(0) + 1
        Feature(scenario.featureName, indent, Some(categoryGroups.lastOption.getOrElse((scenario, Total))._2))
      }
      // tags
      val tagGroups: Seq[(Scenario, CountCategory)] = scenario.tags.map(tag => (scenario, Tag(tag))).toSeq

      (scenario, Total) +: (scenario, feature) +: (categoryGroups ++ tagGroups)
    })
    // group pairs by group
    val allGroups = individualCounts.groupBy(_._2).mapValues(_.map(_._1))
    allGroups
  }

  def diff(before: Map[CountCategory, Seq[Scenario]],
           after: Map[CountCategory, Seq[Scenario]]): Map[CountCategory, GroupDiff] = {
    val allGroups = before.keySet ++ after.keySet
    allGroups.map(group => {
      val scenariosBefore = before.getOrElse(group, Seq[Scenario]()).toSet
      val scenariosAfter = after.getOrElse(group, Seq[Scenario]()).toSet
      val unchangedScenarios = scenariosAfter intersect scenariosBefore

      val addedOrChangedScenarios = scenariosAfter -- unchangedScenarios
      val removedOrChangedScenarios = scenariosBefore -- unchangedScenarios
      val namesOfAddedOrChangedScenarios = addedOrChangedScenarios.groupBy(s => (s.name, s.exampleIndex)).keySet
      val namesOfRemovedOrChangedScenarios = removedOrChangedScenarios.groupBy(s => (s.name, s.exampleIndex)).keySet

      val addedScenarios = addedOrChangedScenarios.filterNot(s => namesOfRemovedOrChangedScenarios contains (s.name, s.exampleIndex))
      val removeScenarios = removedOrChangedScenarios.filterNot(s => namesOfAddedOrChangedScenarios contains (s.name, s.exampleIndex))

      val namesOfChangedScenarios = namesOfAddedOrChangedScenarios intersect namesOfRemovedOrChangedScenarios
      val changedScenarios = namesOfChangedScenarios.map {
        case (name, exampleIndex) =>
          val scenarioBefore = removedOrChangedScenarios.find(s => s.name == name && s.exampleIndex == exampleIndex).get
          val scenarioAfter = addedOrChangedScenarios.find(s => s.name == name && s.exampleIndex == exampleIndex).get
          (scenarioBefore, scenarioAfter)
      }

      val diff = GroupDiff(unchangedScenarios, changedScenarios, addedScenarios, removeScenarios)
      (group, diff)
    }).toMap
  }

  def reportCountsInPrettyPrint(totalCounts: Map[CountCategory, Seq[Scenario]]): String = {
    val countCategoriesByParent = totalCounts.keys.groupBy(countCategory => countCategory.parent)
    val outputs = totalCounts.keys.map(cat => cat -> {
      ("| " * cat.indent) + cat
    }).toMap
    // maxOutputLength is needed to align the counts
    val maxOutputLength = outputs.values.map(_.length).max

    // print counts to stdout as a count category tree in dept first order
    def printDepthFirst(currentCategory: CountCategory): List[String] = {
      val thisOutput = outputs(currentCategory)
      val thisOutputLine = "%s%s%8d".format(
        thisOutput,
        " " * (maxOutputLength-thisOutput.length),
        totalCounts.getOrElse(currentCategory, Seq()).size
      )
      // on each level ordered in groups of Total, ScenarioCategories, Features, Tags
      val groupedCountSubCategories = countCategoriesByParent.getOrElse(Some(currentCategory), Iterable[CountCategory]()).groupBy{
        case Total => 0
        case _:ScenarioCategory => 1
        case _:Feature => 2
        case _:Tag => 3
      }
      // within each group ordered alphabetically by name
      val groupedAndOrderedCountSubCategories = groupedCountSubCategories.toSeq.sortBy(_._1).flatMap {
        case (_, countCategories) => countCategories.toSeq.sortBy(_.name)
      }

      thisOutputLine :: groupedAndOrderedCountSubCategories.flatMap(printDepthFirst).toList
    }

    printDepthFirst(Total).mkString(System.lineSeparator)
  }

  def reportDiffCountsInPrettyPrint(diffs: Map[CountCategory, GroupDiff]): String = {
    val countCategoriesByParent = diffs.keys.groupBy(countCategory => countCategory.parent)
    val outputs = diffs.keys.map(cat => cat -> {
      ("  " * (cat.indent - 1)) + ("- " * (if (cat.indent > 0) 1 else 0)) + cat
    }).toMap
    // maxOutputLength is needed to align the counts
    val maxOutputLength = outputs.values.map(_.length).max

    // print counts to stdout as a count group tree in dept first order
    def printDepthFirst(currentCategory: CountCategory): List[String] = {
      val thisOutput = outputs(currentCategory)
      val thisOutputLine = "%s%s%10d%8d%8d%8d".format(
        thisOutput,
        " " * (maxOutputLength - thisOutput.length),
        diffs.get(currentCategory).map(_.unchanged.size).getOrElse(0),
        diffs.get(currentCategory).map(_.changed.size).getOrElse(0),
        diffs.get(currentCategory).map(_.added.size).getOrElse(0),
        diffs.get(currentCategory).map(_.removed.size).getOrElse(0)
      )
      // on each level ordered in classes of Total, ScenarioCategories, Features, Tags
      val groupedCountSubCategories = countCategoriesByParent.getOrElse(Some(currentCategory), Iterable[CountCategory]()).groupBy{
        case Total => 0
        case _:ScenarioCategory => 1
        case _:Feature => 2
        case _:Tag => 3
      }
      // within each class ordered alphabetically by name
      val groupedAndOrderedCountSubCategories = groupedCountSubCategories.toSeq.sortBy(_._1).flatMap {
        case (_, countCategories) => countCategories.toSeq.sortBy(_.name)
      }
      thisOutputLine :: groupedAndOrderedCountSubCategories.flatMap(printDepthFirst).toList
    }

    //output header
    val columnNames = "Group" + (" " * (maxOutputLength-"Group".length)) +
      " unchanged" + " changed" + "   added" + " removed"
    val header = columnNames + System.lineSeparator + ("–" * columnNames.length) + System.lineSeparator

    header + printDepthFirst(Total).mkString(System.lineSeparator)
  }
}
