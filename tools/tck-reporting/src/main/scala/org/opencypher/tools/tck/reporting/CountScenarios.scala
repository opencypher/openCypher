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
}

case object Total extends CountCategory { val name = "Total" }

case class Tag(name: String) extends CountCategory

case class Feature(name: String) extends CountCategory

/*
 * This is a tiny tool to count TCK scenarios in the list returned by `CypherTCK.allTckScenarios`.
 * At the moment it count scenarios by total, feature, and tags and outputs the counts to stdout.
 * Run object `CountScenarios` to run the tool.
 */
case object CountScenarios {
  def main(args: Array[String]): Unit = {
    val scenarios = CypherTCK.allTckScenarios
    // create individual counts to each scenario
    val individualCounts = scenarios.map(s => {
        // total
        val totalMap = Map[CountCategory,Int](Total -> 1)
        // feature
        val featureMap = Map[CountCategory,Int](Feature(s.featureName) -> 1)
        // tags
        val tagsMap = s.tags.map(tag => (Tag(tag) -> 1)).toMap[CountCategory,Int]

        totalMap ++ featureMap ++ tagsMap
      })
    // total up the counts over all scenarios
    val totalCounts = individualCounts.foldLeft(Map[CountCategory,Int]()){
      case (cnt, cnts) => {
        (cnts.keySet ++ cnt.keySet).map {
          case key => key -> (cnts.getOrElse(key, 0) + cnt.getOrElse(key, 0))
        }.toMap
      }
    }
    // print counts to stdout
    println(totalCounts.map{ case (cat, count) => "" + cat + "\t" + count}.mkString(System.lineSeparator))
  }
}
