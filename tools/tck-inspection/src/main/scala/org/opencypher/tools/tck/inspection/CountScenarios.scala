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
import org.opencypher.tools.tck.api.Moved
import org.opencypher.tools.tck.api.PotentiallyDuplicated
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.ScenarioDiff

/*
 * This is a tiny tool to count TCK scenarios in the list returned by `CypherTCK.allTckScenarios`.
 * At the moment it count scenarios by total, feature, and tags and outputs the counts to stdout.
 * Run object `CountScenarios` to run the tool.
 */
case object CountScenarios {
  def main(args: Array[String]): Unit = {
    if(args.length == 0) {
      println(reportCountsInPrettyPrint(collectScenarioGroups(CypherTCK.allTckScenarios)))
    } else if(args.length == 1) {
      println(reportCountsInPrettyPrint(collectScenarioGroups(CypherTCK.allTckScenariosFromFilesystem(args(0)))))
    } else if(args.length == 2) {
      println(reportDiffCountsInPrettyPrint(
        scenarioGroupDiff(
          collectScenarioGroups(CypherTCK.allTckScenariosFromFilesystem(args(0))),
          collectScenarioGroups(CypherTCK.allTckScenariosFromFilesystem(args(1))))
        )
      )
    }
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
