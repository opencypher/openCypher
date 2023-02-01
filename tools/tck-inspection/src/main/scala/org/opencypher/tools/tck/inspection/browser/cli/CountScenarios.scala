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
package org.opencypher.tools.tck.inspection.browser.cli

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.groups.ExampleItem
import org.opencypher.tools.tck.api.groups.ScenarioItem
import org.opencypher.tools.tck.api.groups.ScenarioOutline
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.inspection.diff.ScenarioDiff
import org.opencypher.tools.tck.inspection.diff.TckTreeDiff

/*
 * This is a tiny tool to count TCK scenarios in the list returned by `CypherTCK.allTckScenarios`.
 * At the moment it count scenarios by total, feature, and tags and outputs the counts to stdout.
 * Run object `CountScenarios` to run the tool.
 */
case object CountScenarios {
  def main(args: Array[String]): Unit = {
    if(args.length == 0) {
      println(reportCountsInPrettyPrint(TckTree(CypherTCK.allTckScenarios)))
    } else if(args.length == 1) {
      println(reportCountsInPrettyPrint(TckTree(CypherTCK.allTckScenariosFromFilesystem(args(0)))))
    } else if(args.length == 2) {
      println(reportDiffCountsInPrettyPrint(
        TckTreeDiff(
          TckTree(CypherTCK.allTckScenariosFromFilesystem(args(0))),
          TckTree(CypherTCK.allTckScenariosFromFilesystem(args(1)))
        )
      ))
    }
  }

  def potentialDuplicates(scenarios: Seq[Scenario]): Seq[(Scenario, Scenario, ScenarioDiff)] = {
    val duplicates = scenarios.zipWithIndex.flatMap {
      case (a, i) =>
        scenarios.slice(i + 1, scenarios.size).map(b => (a, b, ScenarioDiff(a, b))).filter {
          case (_, _, ds) => ds.potentialDuplicate
        }
    }
    duplicates
  }

  def reportCountsInPrettyPrint(tckTree: TckTree): String = {
    val groupsFiltered = tckTree.groupsOrderedDepthFirst filter {
      case _:ScenarioItem | _:ScenarioOutline | _:ExampleItem => false
      case _ => true
    }

    val outputs = groupsFiltered.map(group =>
      group -> (("| " * group.indent) + group.name)
    ).toMap
    // maxOutputLength is needed to align the counts
    val maxOutputLength = outputs.values.map(_.length).max

    val outputLines = groupsFiltered.map( group => {
      val thisOutput = outputs(group)
      val thisOutputLine = "%s%s%8d".format(
        thisOutput,
        " " * (maxOutputLength - thisOutput.length),
        tckTree.groupedScenarios(group).size
      )
      thisOutputLine
    })

    outputLines.mkString(System.lineSeparator)
  }

  def reportDiffCountsInPrettyPrint(tckTreeDiff: TckTreeDiff): String = {
    val groupSequence = tckTreeDiff.groupsOrderedDepthFirst

    val outputs = groupSequence.map(group => group -> {
      ("  " * (group.indent - 1)) + ("- " * (if (group.indent > 0) 1 else 0)) + group.name
    }).toMap
    // maxOutputLength is needed to align the counts
    val maxOutputLength = outputs.values.map(_.length).max

    val outputLines = groupSequence.map( group => {
      val thisOutput = outputs(group)
      val thisOutputLine = "%s%s%10d%12d%14d%7d%9d".format(
        thisOutput,
        " " * (maxOutputLength - thisOutput.length),
        tckTreeDiff.diffs(group).unchangedScenarios.size,
        tckTreeDiff.diffs(group).movedScenarios.size,
        tckTreeDiff.diffs(group).changedScenarios.size,
        tckTreeDiff.diffs(group).addedScenarios.size,
        tckTreeDiff.diffs(group).removedScenarios.size
      )
      thisOutputLine
    })

    //output header
    val columnNames = "Group" + (" " * (maxOutputLength-"Group".length)) +
      " unchanged" + "  moved only" + "  changed more" + "  added" + "  removed"
    val header = columnNames + System.lineSeparator + ("–" * columnNames.length) + System.lineSeparator

    header + outputLines.mkString(System.lineSeparator)
  }
}
