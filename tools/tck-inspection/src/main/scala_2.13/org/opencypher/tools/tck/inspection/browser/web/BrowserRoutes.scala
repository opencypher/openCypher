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
package org.opencypher.tools.tck.inspection.browser.web

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.inspection.util.CallingSystemProcesses
import org.opencypher.tools.tck.inspection.util.ProcessReturn
import scalatags.Text.all._

case class BrowserRoutes()(implicit val log: cask.Logger) extends cask.Routes with PageBasic {
  val path2browserPages = new scala.collection.mutable.HashMap[String, BrowserPages]
  def browserPages(path: String): BrowserPages = path2browserPages(path)

  def browserReportURL(browserPages: BrowserPages): String = {
    val pathEnc = URLEncoder.encode(browserPages.browserModel.path, StandardCharsets.UTF_8.toString)
    s"/browser/$pathEnc"
  }

  @cask.get("/browser/:pathEnc")
  def browseReport(pathEnc: String): doctype = secureBrowserPage(
    pathEnc = pathEnc,
    pageFrag = _.browserReportPage(),
    refresh = true
  )

  def secureBrowserPage(pathEnc: String, pageFrag: BrowserPages => doctype, refresh: Boolean = false): doctype = {
    val path = URLDecoder.decode(pathEnc, StandardCharsets.UTF_8.toString)
    val browserPages =
      if(refresh) {
        val browserPages = BrowserPages(BrowserModel(path), this)
        path2browserPages.put(path, browserPages)
        browserPages
      } else {
        path2browserPages.getOrElseUpdate(path, BrowserPages(BrowserModel(path), this))
      }
    pageFrag(browserPages)
  }

  def listScenariosURL(browserPages: BrowserPages, group: Group): String = {
    s"${browserReportURL(browserPages)}/list/${browserPages.browserModel.group2GroupId(group)}"
  }

  @cask.get("/browser/:pathEnc/list/:groupId")
  def listScenarios(pathEnc: String, groupId: Int): doctype = secureBrowserPage(
    pathEnc = pathEnc,
    pageFrag = browserPages => securedGroupPage(browserPages, groupId, group => browserPages.listScenariosPage(
      scenarios = group => browserPages.browserModel.tckTree.groupedScenarios.get(group),
      group = group,
      kind = None,
      showSingleScenarioURL = scenario => showSingleScenarioURL(browserPages, scenario),
      openScenarioInEditorURL = scenario => openScenarioInEditorURL(browserPages, scenario),
    ))
  )

  private def securedGroupPage(browserPages: BrowserPages, groupId: Int, pageFrag: Group => doctype): doctype = {
    if(browserPages.browserModel.groupId2Group.isDefinedAt(groupId)) {
      pageFrag(browserPages.browserModel.groupId2Group(groupId))
    } else error("Unknown group: " + groupId)
  }

  def showSingleScenarioURL(browserPages: BrowserPages, scenario: Scenario) =
    s"${browserReportURL(browserPages)}/scenario/${browserPages.browserModel.scenario2ScenarioId(scenario) }"

  @cask.get("/browser/:pathEnc/scenario/:scenarioId")
  def showSingleScenario(pathEnc: String, scenarioId: Int): doctype = secureBrowserPage(
    pathEnc = pathEnc,
    pageFrag = browserPages => securedScenarioPage(browserPages, scenarioId, scenario => browserPages.scenarioPage(scenario))
  )

  def openScenarioInEditorURL(browserPages: BrowserPages, scenario: Scenario) =
    s"${browserReportURL(browserPages)}/open/${browserPages.browserModel.scenario2ScenarioId(scenario)}"

  @cask.get("/browser/:pathEnc/open/:scenarioId")
  def openScenarioInEditor(pathEnc: String, scenarioId: Int): doctype = secureBrowserPage(
    pathEnc = pathEnc,
    pageFrag = tckDiffPages => securedScenarioPage(tckDiffPages, scenarioId, scenario => {
      CallingSystemProcesses.openScenarioInEditor(scenario) match {
        case ProcessReturn(0, _, _, _) =>
          doctype("html")(
            html(
              head(
                script(
                  "window.close();"
                )
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

  private def securedScenarioPage(browserPages: BrowserPages, scenarioId: Int, pageFrag: Scenario => doctype): doctype = {
    if(browserPages.browserModel.scenarioId2Scenario.isDefinedAt(scenarioId)) {
      pageFrag(browserPages.browserModel.scenarioId2Scenario(scenarioId))
    } else error("Unknown scenario: " + scenarioId)
  }

  initialize()
}
