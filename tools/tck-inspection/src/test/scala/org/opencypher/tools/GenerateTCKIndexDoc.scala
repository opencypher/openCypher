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
package org.opencypher.tools

import org.junit.jupiter.api.Test
import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.inspection.collect.Feature
import org.opencypher.tools.tck.inspection.collect.Group
import org.opencypher.tools.tck.inspection.collect.GroupCollection
import org.opencypher.tools.tck.inspection.collect.ScenarioCategory
import org.opencypher.tools.tck.inspection.collect.Tag
import org.opencypher.tools.tck.inspection.collect.Total

class GenerateTCKIndexDocTest {
  @Test
  def runAsTest(): Unit = GenerateTCKIndexDoc.main(Array())
}

object GenerateTCKIndexDoc {
  private val indexDocFile = System.getProperty("buildDirectory") + "/../../../tck/index.adoc"

  def main(args: Array[String]): Unit = {
    val scenarios = CypherTCK.allTckScenarios
    val groups = GroupCollection(scenarios).keySet

    val groupsByParent = groups.groupBy(_.parent)

    // print counts to stdout as a count group tree in dept first order
    def printDepthFirst(currentGroup: Group): List[String] = {
      val groupHead = currentGroup match {
        case Total =>
          """= TCK Index
            |
            |The TCK is split into categories based on language constructs.
            |The two main groups are clauses and expressions.
            |Each group enumerates its members.
            |Within each member, there are additional categories.
            |
            |There is also an `uncategorized` and `precategorized` directory containing uncategorized features.
            |""".stripMargin
        case ScenarioCategory(name, indent, _) =>
          System.getProperty("line.separator") + s"=${("=" * indent)} $name" + System.getProperty("line.separator")
        case Feature(name, _, _) =>
          s"* $name"
        case _ => ""
      }
      // on each level ordered in classes of Total, ScenarioCategories, Features, Tags
      val groupsByClasses = groupsByParent.getOrElse(Some(currentGroup), Iterable[Group]()).groupBy{
        case Total => 0
        case _:Feature => 1
        case _:ScenarioCategory => 2
        case _:Tag => 3
      }
      // within each group ordered alphabetically by name
      val groupsOrdered = groupsByClasses.toSeq.sortBy(_._1).flatMap {
        case (_, countCategories) => countCategories.toSeq.sortBy(_.name)
      }
      // filter out tags and uncategorized
      val groupsOrderedFiltered = groupsOrdered.filter {
        case ScenarioCategory("precategorized", _, _) => false
        case ScenarioCategory("uncategorized", _, _) => false
        case Tag(_) => false
        case _ => true
      }

      groupHead :: groupsOrderedFiltered.flatMap(printDepthFirst).toList
    }

    val indexDoc = printDepthFirst(Total).mkString(System.lineSeparator)

    import java.io._
    val pw = new PrintWriter(new File(indexDocFile))
    pw.write(indexDoc)
    pw.close

    println("Generated " + indexDocFile)
  }
}
