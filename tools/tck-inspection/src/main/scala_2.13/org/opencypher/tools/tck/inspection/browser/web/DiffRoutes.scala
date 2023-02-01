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

case class DiffRoutes()(implicit val log: cask.Logger) extends cask.Routes with PageBasic {
  val paths2DiffPages = new scala.collection.mutable.HashMap[(String, String), DiffPages]
  def diffPages(beforePath: String, afterPath: String): DiffPages = paths2DiffPages((beforePath, afterPath))

  def diffReportURL(diffPages: DiffPages): String = {
    val beforePathEnc = URLEncoder.encode(diffPages.diffModel.beforePath, StandardCharsets.UTF_8.toString)
    val afterPathEnc = URLEncoder.encode(diffPages.diffModel.afterPath, StandardCharsets.UTF_8.toString)
    s"/diff/$beforePathEnc/$afterPathEnc"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc")
  def diffReport(beforePathEnc: String, afterPathEnc: String): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = _.diffReportPage(),
    refresh = true
  )

  def secureDiffPage(beforePathEnc: String, afterPathEnc: String, pageFrag: DiffPages => doctype, refresh: Boolean = false): doctype = {
    val beforePath = URLDecoder.decode(beforePathEnc, StandardCharsets.UTF_8.toString)
    val afterPath = URLDecoder.decode(afterPathEnc, StandardCharsets.UTF_8.toString)
    val diffPages =
      if(refresh) {
        val diffPages = DiffPages(DiffModel(beforePath, afterPath), this)
        paths2DiffPages.put((beforePath, afterPath), diffPages)
        diffPages
      } else {
        paths2DiffPages.getOrElseUpdate((beforePath, afterPath), DiffPages(DiffModel(beforePath, afterPath), this))
      }
    pageFrag(diffPages)
  }

  def listBeforeScenariosURL(diffPages: DiffPages, group: Group): String = {
    s"${diffReportURL(diffPages)}/before/${diffPages.diffModel.group2GroupId(group)}"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/before/:groupId")
  def listBeforeScenarios(beforePathEnc: String, afterPathEnc: String, groupId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages =>
      securedGroupPage(diffPages, groupId, group =>
        listScenariosPage(
          scenarios = group => diffPages.diffModel.tckTreeDiff.before.groupedScenarios.get(group),
          group = group,
          kind = Some(span(CSS.tckCollection)(BeforeCollection.toString)),
          showSingleScenarioURL = scenario => showSingleScenarioURL(diffPages, scenario),
          openScenarioInEditorURL = scenario => openScenarioInEditorURL(diffPages, scenario),
        )
      )
  )

  def listAfterScenariosURL(diffPages: DiffPages, group: Group): String = {
    s"${diffReportURL(diffPages)}/after/${diffPages.diffModel.group2GroupId(group)}"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/after/:groupId")
  def listAfterScenarios(beforePathEnc: String, afterPathEnc: String, groupId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages =>
      securedGroupPage(diffPages, groupId, group =>
        listScenariosPage(
          scenarios = group => diffPages.diffModel.tckTreeDiff.after.groupedScenarios.get(group),
          group = group,
          kind = Some(span(CSS.tckCollection)(AfterCollection.toString)),
          showSingleScenarioURL = scenario => showSingleScenarioURL(diffPages, scenario),
          openScenarioInEditorURL = scenario => openScenarioInEditorURL(diffPages, scenario),
        )
      )
  )

  def listUnchangedScenariosURL(diffPages: DiffPages, group: Group): String = {
    s"${diffReportURL(diffPages)}/unchanged/${diffPages.diffModel.group2GroupId(group)}"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/unchanged/:groupId")
  def listUnchangedScenarios(beforePathEnc: String, afterPathEnc: String, groupId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages =>
      securedGroupPage(diffPages, groupId, group =>
        listScenariosPage(
          scenarios = group => diffPages.diffModel.tckTreeDiff.diffs.get(group).map(_.unchangedScenarios),
          group = group,
          kind = Some("unchanged"),
          showSingleScenarioURL = scenario => showSingleScenarioURL(diffPages, scenario),
          openScenarioInEditorURL = scenario => openScenarioInEditorURL(diffPages, scenario),
        )
      )
  )

  def listAddedScenariosURL(diffPages: DiffPages, group: Group): String = {
    s"${diffReportURL(diffPages)}/added/${diffPages.diffModel.group2GroupId(group)}"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/added/:groupId")
  def listAddedScenarios(beforePathEnc: String, afterPathEnc: String, groupId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages =>
      securedGroupPage(diffPages, groupId, group =>
        listScenariosPage(
          scenarios = group => diffPages.diffModel.tckTreeDiff.diffs.get(group).map(_.addedScenarios),
          group = group,
          kind = Some("added"),
          showSingleScenarioURL = scenario => showSingleScenarioURL(diffPages, scenario),
          openScenarioInEditorURL = scenario => openScenarioInEditorURL(diffPages, scenario),
        )
      )
  )

  def listRemovedScenariosURL(diffPages: DiffPages, group: Group): String = {
    s"${diffReportURL(diffPages)}/removed/${diffPages.diffModel.group2GroupId(group)}"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/removed/:groupId")
  def listRemovedScenarios(beforePathEnc: String, afterPathEnc: String, groupId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages =>
      securedGroupPage(diffPages, groupId, group =>
        listScenariosPage(
          scenarios = group => diffPages.diffModel.tckTreeDiff.diffs.get(group).map(_.removedScenarios),
          group = group,
          kind = Some("removed"),
          showSingleScenarioURL = scenario => showSingleScenarioURL(diffPages, scenario),
          openScenarioInEditorURL = scenario => openScenarioInEditorURL(diffPages, scenario),
        )
      )
  )

  def listMovedScenariosURL(diffPages: DiffPages, group: Group): String = {
    s"${diffReportURL(diffPages)}/moved/${diffPages.diffModel.group2GroupId(group)}"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/moved/:groupId")
  def listMovedScenarios(beforePathEnc: String, afterPathEnc: String, groupId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages => securedGroupPage(diffPages, groupId, group => diffPages.listMovedScenarios(group))
  )

  def listChangedScenariosURL(diffPages: DiffPages, group: Group): String = {
    s"${diffReportURL(diffPages)}/changed/${diffPages.diffModel.group2GroupId(group)}"
  }

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/changed/:groupId")
  def listChangedScenarios(beforePathEnc: String, afterPathEnc: String, groupId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages => securedGroupPage(diffPages, groupId, group => diffPages.listChangedScenarios(group))
  )

  private def securedGroupPage(diffPages: DiffPages, groupId: Int, pageFrag: Group => doctype): doctype = {
    if(diffPages.diffModel.groupId2Group.isDefinedAt(groupId)) {
      pageFrag(diffPages.diffModel.groupId2Group(groupId))
    } else error("Unknown group: " + groupId)
  }

  def showSingleScenarioURL(diffPages: DiffPages, scenario: Scenario): String =
    s"${diffReportURL(diffPages)}/scenario/${diffPages.diffModel.scenario2ScenarioId(scenario) }"

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/scenario/:scenarioId")
  def showSingleScenario(beforePathEnc: String, afterPathEnc: String, scenarioId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages => securedScenarioPage(diffPages, scenarioId, scenario => diffPages.scenarioPage(scenario))
  )

  def openScenarioInEditorURL(diffPages: DiffPages, scenario: Scenario) =
    s"${diffReportURL(diffPages)}/open/${diffPages.diffModel.scenario2ScenarioId(scenario)}"

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/open/:scenarioId")
  def openScenarioInEditor(beforePathEnc: String, afterPathEnc: String, scenarioId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages => securedScenarioPage(diffPages, scenarioId, scenario => {
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

  private def securedScenarioPage(diffPages: DiffPages, scenarioId: Int, pageFrag: Scenario => doctype): doctype = {
    if(diffPages.diffModel.scenarioId2Scenario.isDefinedAt(scenarioId)) {
      pageFrag(diffPages.diffModel.scenarioId2Scenario(scenarioId))
    } else error("Unknown scenario: " + scenarioId)
  }

  def showDetailedScenarioDiffURL(diffPages: DiffPages, before: Scenario, after: Scenario): String =
    s"${diffReportURL(diffPages)}/scenarioDiff/${diffPages.diffModel.scenario2ScenarioId(before)}/${diffPages.diffModel.scenario2ScenarioId(after)}"

  @cask.get("/diff/:beforePathEnc/:afterPathEnc/scenarioDiff/:beforeId/:afterId")
  def showDetailedScenarioDiff(beforePathEnc: String, afterPathEnc: String, beforeId: Int, afterId: Int): doctype = secureDiffPage(
    beforePathEnc = beforePathEnc,
    afterPathEnc = afterPathEnc,
    pageFrag = diffPages => {
      if(diffPages.diffModel.scenarioId2Scenario.isDefinedAt(beforeId) && diffPages.diffModel.scenarioId2Scenario.isDefinedAt(afterId)) {
        diffPages.detailedScenarioDiffPage(diffPages.diffModel.scenarioId2Scenario(beforeId), diffPages.diffModel.scenarioId2Scenario(afterId))
      } else {
        error(
          if(!diffPages.diffModel.scenarioId2Scenario.isDefinedAt(beforeId)) "Unknown scenario: " + beforeId else frag(),
          br(),
          if(!diffPages.diffModel.scenarioId2Scenario.isDefinedAt(afterId)) "Unknown scenario: " + afterId else frag(),
        )
      }
    }
  )

  initialize()
}
