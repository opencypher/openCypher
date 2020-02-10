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
package org.opencypher.tools.tck.inspection

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Different
import org.opencypher.tools.tck.api.Moved
import org.opencypher.tools.tck.api.PotentiallyDuplicated
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.ScenarioDiff

trait Group {
  def name: String
  def indent: Int
  def parent: Option[Group]

  override def toString: String = name
}

case object Total extends Group {
  val name = "Total"
  val indent = 0
  val parent: Option[Group] = None
}

case class Tag(name: String) extends Group {
  val indent = 1
  val parent: Option[Group] = Some(Total)
}

case class Feature(name: String, indent: Int, parent: Option[Group]) extends Group {
  override def toString: String = "Feature: " + name
}

case class ScenarioCategory(name: String, indent: Int, parent: Option[Group]) extends Group

case class GroupDiff(unchanged: Set[Scenario], changed: Set[(Scenario, Scenario, Set[ScenarioDiff])], added: Set[Scenario], removed: Set[Scenario])

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
      println(reportDiffCountsInPrettyPrint(
        diff(
          collect(CypherTCK.allTckScenariosFromFilesystem(args(0))),
          collect(CypherTCK.allTckScenariosFromFilesystem(args(1))))
        )
      )
    }
  }

  def collect(scenarios: Seq[Scenario]): Map[Group, Seq[Scenario]] = {
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

  def diff(before: Map[Group, Seq[Scenario]],
           after: Map[Group, Seq[Scenario]]): Map[Group, GroupDiff] = {
    val allGroups = before.keySet ++ after.keySet
    allGroups.map(f = group => {
      val scenariosBefore: Set[Scenario] = before.getOrElse(group, Seq[Scenario]()).toSet
      val scenariosAfter: Set[Scenario] = after.getOrElse(group, Seq[Scenario]()).toSet
      val unchangedScenarios = scenariosAfter intersect scenariosBefore

      val removedOrChangedScenarios: Set[Scenario] = scenariosBefore -- unchangedScenarios
      val addedOrChangedScenarios: Set[Scenario] = scenariosAfter -- unchangedScenarios

      val removeScenarios = removedOrChangedScenarios.filter(b => addedOrChangedScenarios.forall(_.diff(b) == Set(Different)))
      val addedScenarios = addedOrChangedScenarios.filter(a => removedOrChangedScenarios.forall(_.diff(a) == Set(Different)))

      val changedScenariosBefore = removedOrChangedScenarios -- removeScenarios
      val changedScenariosAfter = addedOrChangedScenarios -- addedScenarios

      val changedScenarios: Set[(Scenario, Scenario, Set[ScenarioDiff])] = changedScenariosBefore.flatMap(
        b => changedScenariosAfter.map(a => (b, a, b.diff(a) - PotentiallyDuplicated)).filter {
          case (_, _, d) if d contains Different => false
          case _ => true
        }
      )

      val diff = GroupDiff(unchangedScenarios, changedScenarios, addedScenarios, removeScenarios)
      (group, diff)
    }).toMap
  }

  def potentialDuplicates(scenarios: Seq[Scenario]): Seq[(Scenario, Scenario, Set[ScenarioDiff])] = {
    val duplicates = scenarios.zipWithIndex.flatMap {
      case (a, i) =>
        scenarios.slice(i + 1, scenarios.size).map(b => (a, b, a.diff(b))).filter {
          case (_, _, ds) => ds contains PotentiallyDuplicated
        }
    }
    duplicates
  }

  def reportCountsInPrettyPrint(groups: Map[Group, Seq[Scenario]]): String = {
    val groupsByParent = groups.keys.groupBy(countCategory => countCategory.parent)
    val outputs = groups.keys.map(cat => cat -> {
      ("| " * cat.indent) + cat
    }).toMap
    // maxOutputLength is needed to align the counts
    val maxOutputLength = outputs.values.map(_.length).max

    // print counts to stdout as a count group tree in dept first order
    def printDepthFirst(currentGroup: Group): List[String] = {
      val thisOutput = outputs(currentGroup)
      val thisOutputLine = "%s%s%8d".format(
        thisOutput,
        " " * (maxOutputLength-thisOutput.length),
        groups.getOrElse(currentGroup, Seq()).size
      )
      // on each level ordered in classes of Total, ScenarioCategories, Features, Tags
      val groupsByClasses = groupsByParent.getOrElse(Some(currentGroup), Iterable[Group]()).groupBy{
        case Total => 0
        case _:ScenarioCategory => 1
        case _:Feature => 2
        case _:Tag => 3
      }
      // within each group ordered alphabetically by name
      val groupsOrdered = groupsByClasses.toSeq.sortBy(_._1).flatMap {
        case (_, countCategories) => countCategories.toSeq.sortBy(_.name)
      }

      thisOutputLine :: groupsOrdered.flatMap(printDepthFirst).toList
    }

    printDepthFirst(Total).mkString(System.lineSeparator)
  }

  def reportDiffCountsInPrettyPrint(diffs: Map[Group, GroupDiff]): String = {
    val groupsByParent = diffs.keys.groupBy(countCategory => countCategory.parent)
    val outputs = diffs.keys.map(cat => cat -> {
      ("  " * (cat.indent - 1)) + ("- " * (if (cat.indent > 0) 1 else 0)) + cat
    }).toMap
    // maxOutputLength is needed to align the counts
    val maxOutputLength = outputs.values.map(_.length).max

    // print counts to stdout as a count group tree in dept first order
    def printDepthFirst(currentGroup: Group): List[String] = {
      val thisOutput = outputs(currentGroup)
      val thisOutputLine = "%s%s%10d%12d%14d%7d%9d".format(
        thisOutput,
        " " * (maxOutputLength - thisOutput.length),
        diffs.get(currentGroup).map(_.unchanged.size).getOrElse(0),
        diffs.get(currentGroup).map(_.changed.count(_._3 == Set(Moved))).getOrElse(0),
        diffs.get(currentGroup).map(_.changed.count(_._3 != Set(Moved))).getOrElse(0),
        diffs.get(currentGroup).map(_.added.size).getOrElse(0),
        diffs.get(currentGroup).map(_.removed.size).getOrElse(0)
      )
      // on each level ordered in classes of Total, ScenarioCategories, Features, Tags
      val groupsByClasses = groupsByParent.getOrElse(Some(currentGroup), Iterable[Group]()).groupBy{
        case Total => 0
        case _:ScenarioCategory => 1
        case _:Feature => 2
        case _:Tag => 3
      }
      // within each class ordered alphabetically by name
      val groupsOrdered = groupsByClasses.toSeq.sortBy(_._1).flatMap {
        case (_, countCategories) => countCategories.toSeq.sortBy(_.name)
      }
      thisOutputLine :: groupsOrdered.flatMap(printDepthFirst).toList
    }

    //output header
    val columnNames = "Group" + (" " * (maxOutputLength-"Group".length)) +
      " unchanged" + "  moved only" + "  changed more" + "  added" + "  removed"
    val header = columnNames + System.lineSeparator + ("–" * columnNames.length) + System.lineSeparator

    header + printDepthFirst(Total).mkString(System.lineSeparator)
  }

  def reportDiffCountsInGFMPrint(diffs: Map[Group, GroupDiff]): String = {
    val groupsByParent = diffs.keys.groupBy(countCategory => countCategory.parent)
    val outputs = diffs.keys.map(cat => cat -> {
      "`/" + ("…/" * (cat.indent - 1)) + ("…/" * (if (cat.indent > 0) 1 else 0)) + "` " + cat
    }).toMap
    // maxOutputLength is needed to align the counts
    val maxOutputLength = outputs.values.map(_.length).max

    // print counts to stdout as a count group tree in dept first order
    def printDepthFirst(currentGroup: Group): List[String] = {
      val thisOutput = outputs(currentGroup)
      val thisOutputLine = "%s | %d | %d | %d | %d | %d".format(
        thisOutput,
        diffs.get(currentGroup).map(_.unchanged.size).getOrElse(0),
        diffs.get(currentGroup).map(_.changed.count(_._3 == Set(Moved))).getOrElse(0),
        diffs.get(currentGroup).map(_.changed.count(_._3 != Set(Moved))).getOrElse(0),
        diffs.get(currentGroup).map(_.added.size).getOrElse(0),
        diffs.get(currentGroup).map(_.removed.size).getOrElse(0)
      )
      // on each level ordered in classes of Total, ScenarioCategories, Features, Tags
      val groupsByClasses = groupsByParent.getOrElse(Some(currentGroup), Iterable[Group]()).groupBy{
        case Total => 0
        case _:ScenarioCategory => 1
        case _:Feature => 2
        case _:Tag => 3
      }
      // within each class ordered alphabetically by name
      val groupsOrdered = groupsByClasses.toSeq.sortBy(_._1).flatMap {
        case (_, countCategories) => countCategories.toSeq.sortBy(_.name)
      }
      thisOutputLine :: groupsOrdered.flatMap(printDepthFirst).toList
    }

    //output header
    val header =
      """Group | unchanged | moved only | changed more | added | removed
        |------|-----------|------------|--------------|-------|--------""".stripMargin

    (header :: printDepthFirst(Total)).mkString(System.lineSeparator)
  }
}
