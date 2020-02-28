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

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import org.opencypher.tools.tck.api.Scenario
import scalatags.Text.all._

case class InspectRoutes()(implicit val log: cask.Logger) extends cask.Routes with PageBasic {
  val path2inspectPages = new scala.collection.mutable.HashMap[String, InspectPages]
  def inspectPages(path: String): InspectPages = path2inspectPages(path)

  def inspectReportURL(inspectPages: InspectPages): String = {
    val pathEnc = URLEncoder.encode(inspectPages.inspectModel.path, StandardCharsets.UTF_8.toString)
    s"/inspect/$pathEnc"
  }

  @cask.get("/inspect/:pathEnc")
  def diffReport(pathEnc: String): String = secureInspectPage(
    pathEnc = pathEnc,
    pageFrag = _.inspectReportPage(),
    refresh = true
  )

  def secureInspectPage(pathEnc: String, pageFrag: InspectPages => Frag, refresh: Boolean = false): String = {
    val path = URLDecoder.decode(pathEnc, StandardCharsets.UTF_8.toString)
    val inspectPages =
      if(refresh) {
        val inspectPages = InspectPages(InspectModel(path), this)
        path2inspectPages.put(path, inspectPages)
        inspectPages
      } else {
        path2inspectPages.getOrElseUpdate(path, InspectPages(InspectModel(path), this))
      }
    pageFrag(inspectPages).toString
  }

  def listScenariosURL(inspectPages: InspectPages, group: Group): String = {
    s"${inspectReportURL(inspectPages)}/list/${inspectPages.inspectModel.group2GroupId(group)}"
  }

  @cask.get("/inspect/:pathEnc/list/:groupId")
  def listScenarios(pathEnc: String, groupId: Int): String = secureInspectPage(
    pathEnc = pathEnc,
    pageFrag = inspectPages => securedGroupPage(inspectPages, groupId, group => inspectPages.listScenariosInGroup(group))
  )

  private def securedGroupPage(inspectPages: InspectPages, groupId: Int, pageFrag: Group => Frag) = {
    if(inspectPages.inspectModel.groupId2Group.isDefinedAt(groupId)) {
      pageFrag(inspectPages.inspectModel.groupId2Group(groupId))
    } else error("Unknown group: " + groupId)
  }

  def showSingleScenarioURL(inspectPages: InspectPages, scenario: Scenario) =
    s"${inspectReportURL(inspectPages)}/scenario/${inspectPages.inspectModel.scenario2ScenarioId(scenario) }"

  @cask.get("/inspect/:pathEnc/scenario/:scenarioId")
  def showSingleScenario(pathEnc: String, scenarioId: Int): String = secureInspectPage(
    pathEnc = pathEnc,
    pageFrag = inspectPages => securedScenarioPage(inspectPages, scenarioId, scenario => inspectPages.scenarioPage(scenario))
  )

  def openScenarioInEditorURL(inspectPages: InspectPages, scenario: Scenario) =
    s"${inspectReportURL(inspectPages)}/open/${inspectPages.inspectModel.scenario2ScenarioId(scenario)}"

  @cask.get("/inspect/:pathEnc/open/:scenarioId")
  def openScenarioInEditor(pathEnc: String, scenarioId: Int): String = secureInspectPage(
    pathEnc = pathEnc,
    pageFrag = tckDiffPages => securedScenarioPage(tckDiffPages, scenarioId, scenario => {
      CallingSystemProcesses.openScenarioInEditor(scenario) match {
        case ProcessReturn(0, _, _, _) =>
          html(
            head(
              script(
                "window.close();"
              )
            )
          )
        case ProcessReturn(x, out, err, cmd) =>
          error(
            "Cannot open editor on scenario",
            dl(
              dt("Command"),
              dd(code(cmd)),
              dt("stdout"),
              dd(code(out.flatMap(line => Seq[Frag](line, br())).dropRight(1))),
              dt("stderr"),
              dd(code(err.flatMap(line => Seq[Frag](line, br())).dropRight(1))),
              dt("exit code"),
              dd(code(x))
            )
          )
      }
    })
  )

  private def securedScenarioPage(inspectPages: InspectPages, scenarioId: Int, pageFrag: Scenario => Frag) = {
    if(inspectPages.inspectModel.scenarioId2Scenario.isDefinedAt(scenarioId)) {
      pageFrag(inspectPages.inspectModel.scenarioId2Scenario(scenarioId))
    } else error("Unknown scenario: " + scenarioId)
  }

  initialize()
}
