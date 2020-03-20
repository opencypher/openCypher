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
package org.opencypher.tools.tck.inspection.browser.web

import org.opencypher.tools.tck.api.Pickle
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.inspection.collect
import org.opencypher.tools.tck.inspection.collect.Feature
import org.opencypher.tools.tck.inspection.collect.Group
import org.opencypher.tools.tck.inspection.collect.ScenarioCategory
import org.opencypher.tools.tck.inspection.collect.Total
import org.opencypher.tools.tck.inspection.diff.Diff
import org.opencypher.tools.tck.inspection.diff.ElementDiff
import org.opencypher.tools.tck.inspection.diff.ElementaryDiffTag._
import org.opencypher.tools.tck.inspection.diff.GroupDiff
import org.opencypher.tools.tck.inspection.diff.ScenarioDiff
import org.opencypher.tools.tck.inspection.diff.Tuple2Diff
import scalatags.Text
import scalatags.Text.all._

case class DiffPages(diffModel: DiffModel, diffRoutes: DiffRoutes) extends PageBasic {
  def diffReportPage(): Text.TypedTag[String] = {
    page(
      pageTitle("Diff report"),
      div(display.flex, flexDirection.row)(
        div(span(CSS.tckCollection)(BeforeCollection.toString), code(diffModel.beforePath)),
        div(paddingLeft:=1.em, paddingRight:=1.em)(categorySeparator),
        div(span(CSS.tckCollection)(AfterCollection.toString), code(diffModel.afterPath))
      ),
      sectionTitle("Counts"),
      diffCountsFrag(diffModel.diffs, diffModel.before, diffModel.after)
    )
  }

  def diffCountsFrag(diffs: Map[Group, GroupDiff], before: Map[Group, Seq[Scenario]], after: Map[Group, Seq[Scenario]]): Text.TypedTag[String] = {
    val groupsByParent = diffs.keys.groupBy(countCategory => countCategory.parent)

    // print counts to html table rows as a count group tree in dept first order
    def printDepthFirst(currentGroup: Group): Seq[scalatags.Text.TypedTag[String]] = {
      val thisRow = {
        val currentDiff = diffs.get(currentGroup)
        tr(
          td(textIndent:=currentGroup.indent.em)(
            currentGroup.toString
          ),
          td(),
          td(textAlign.right)(
            before.get(currentGroup).map(col =>
              a(href:=diffRoutes.listBeforeScenariosURL(this, currentGroup))(col.size)
            ).getOrElse("-")
          ),
          td(),
          td(textAlign.right)({
            val size = currentDiff.map(_.unchangedScenarios.size).getOrElse(0)
            if(size > 0)
              a(href:=diffRoutes.listUnchangedScenariosURL(this, currentGroup))(size)
            else
              "-"
          }),
          td(),
          td(textAlign.right)({
            val size = currentDiff.map(_.movedScenarios.size).getOrElse(0)
            if(size > 0)
              a(href:=diffRoutes.listMovedScenariosURL(this, currentGroup))(size)
            else
              "-"
          }),
          td(),
          td(textAlign.right)({
            val size = currentDiff.map(_.changedScenarios.size).getOrElse(0)
            if (size > 0)
              a(href := diffRoutes.listChangedScenariosURL(this, currentGroup))(size)
            else
              "-"
          }),
          td(),
          td(textAlign.right)({
            val size = currentDiff.map(_.addedScenarios.size).getOrElse(0)
            if (size > 0)
              a(href:=diffRoutes.listAddedScenariosURL(this, currentGroup))(size)
            else
              "-"
          }),
          td(),
          td(textAlign.right)({
            val size = currentDiff.map(_.removedScenarios.size).getOrElse(0)
            if (size > 0)
              a(href:=diffRoutes.listRemovedScenariosURL(this, currentGroup))(size)
            else
              "-"
          }),
          td(),
          td(textAlign.right)(
            after.get(currentGroup).map(col =>
              a(href:=diffRoutes.listAfterScenariosURL(this, currentGroup))(col.size)
            ).getOrElse("-")
          ),
          td(),
        )
      }
      // on each level ordered in classes of Total, ScenarioCategories, Features, Tags
      val groupsByClasses = groupsByParent.getOrElse(Some(currentGroup), Iterable[Group]()).groupBy{
        case Total => 0
        case _:ScenarioCategory => 1
        case _:Feature => 2
        case _:collect.Tag => 3
      }
      // within each class ordered alphabetically by name
      val groupsOrdered = groupsByClasses.toSeq.sortBy(_._1).flatMap {
        case (_, countCategories) => countCategories.toSeq.sortBy(_.name)
      }
      thisRow +: groupsOrdered.flatMap(printDepthFirst)
    }

    //output header
    val header =
      tr(
        th("Group"),
        th(code("(:")),
        th(CSS.tckCollection)(BeforeCollection.toString),
        th(code(")-[:")),
        th("unchanged"),
        th(code("|")),
        th("moved only"),
        th(code("|")),
        th("changed more"),
        th(code("|")),
        th("added"),
        th(code("|")),
        th("removed"),
        th(code("]->(:")),
        th(CSS.tckCollection)(AfterCollection.toString),
        th(code(")")),
      )

    table(CSS.hoverTable)(header +: printDepthFirst(Total))
  }

  def listMovedScenarios(group: Group): Text.TypedTag[String] = {
    val diffs =
      diffModel.diffs.get(group).map(_.movedScenarios).getOrElse(Set.empty[ScenarioDiff])
    val byScenario = diffs.toSeq.sortBy(d => d.before.toString + d.after.toString)
    val byLocation = diffs.groupBy {
      case ScenarioDiff(before, after) => before.categories.mkString("/")+"/"+before.featureName+">>>"+after.categories.mkString("/")+"/"+after.featureName
    }.toSeq.sortBy(_._1)

    case object ByScenario extends Anchor
    case object ByLocation extends Anchor
    page(
      pageTitle(diffs.size, " scenario(s) moved in group ", i(group.toString)),
      ol(
        li(link2LocalAnchor(ByScenario, "By scenario")),
        li(link2LocalAnchor(ByLocation, "By location")),
      ),
      anchor(ByScenario),
      sectionTitle("By scenario"),
      dl(CSS.movedScenariosList)(
        for(ScenarioDiff(before, after) <- byScenario) yield frag(
          dt(CSS.movedScenariosName)(
            div(CSS.scenarioTitleSmall)(scenarioTitle(before))),
          dd(CSS.movedScenariosMove)(
            span(CSS.locationLine, marginBottom:=0.25.ex)(scenarioLocationFrag(
              scenario = before,
              showUrl = Some(diffRoutes.showSingleScenarioURL(this, before)),
              sourceUrl = Some(diffRoutes.openScenarioInEditorURL(this, before)),
            )),
            span(CSS.locationLine)(scenarioLocationFrag(
              scenario = after,
              showUrl = Some(diffRoutes.showSingleScenarioURL(this, after)),
              sourceUrl = Some(diffRoutes.openScenarioInEditorURL(this, after)),
            )),
          )
        )
      ),
      anchor(ByLocation),
      sectionTitle("By location"),
      table(width:=100.pct)(
        tr(
          th(CSS.tckCollection, colspan:=2)(BeforeCollection.toString),
          th("Scenarios"),
          th(CSS.tckCollection, colspan:=2)(AfterCollection.toString),
        ),
        for( (_, scenarioDiffs) <- byLocation ) yield frag(
          tr(verticalAlign.top)(
            td(colspan:=2, CSS.locationLine)(scenarioLocationFrag(scenario = scenarioDiffs.head.before)),
            td(textAlign.center)("--- " + scenarioDiffs.size.toString + " -->"),
            td(colspan:=2, CSS.locationLine)(scenarioLocationFrag(scenario = scenarioDiffs.head.after)),
          ),
          for( diff <- scenarioDiffs.toSeq.sortBy(d => (d.before.name, d.after.exampleIndex)) ) yield
            tr(
              td(textAlign.right)(
                showSingleScenarioLink(diff.before, "[show]"),
                openScenarioInEditorLink(diff.before, "[code]"),
              ),
              td(colspan:=3, textAlign.center)(scenarioTitle(diff.before)),
              td(
                showSingleScenarioLink(diff.after, "[show]"),
                openScenarioInEditorLink(diff.after, "[code]"),
              ),
            )
        )
      )
    )
  }

  def listChangedScenarios(group: Group): Text.TypedTag[String] = {
    val diffs =
      diffModel.diffs.get(group).map(_.changedScenarios).getOrElse(
        Set.empty[ScenarioDiff]
      ).toSeq.sortBy(d => d.before.toString + d.after.toString)
    page(
      pageTitle(diffs.size, " scenario(s) changed in group ", i(group.toString)),
      ul(
        for(diff@ScenarioDiff(before, after) <- diffs) yield li(
          scenarioLocationFrag(scenario = before, collection = Some(BeforeCollection.toString)),
          inlineSpacer(),
          showSingleScenarioLink(before, scenarioTitle(before)),
          inlineSpacer(),
          openScenarioInEditorLink(before, "[code]"),
          br,
          scenarioLocationFrag(scenario = after, collection = Some(AfterCollection.toString)),
          inlineSpacer(),
          showSingleScenarioLink(after, scenarioTitle(after)),
          inlineSpacer(),
          openScenarioInEditorLink(after, "[code]"),
          br,
          diff.diffTags.map(_.toString).toSeq.sorted.mkString(", "),
          inlineSpacer(),
          showDetailedScenarioDiffLink(before, after, "[Details]")
        )
      )
    )
  }

  private def showSingleScenarioLink(scenario: Scenario, linkContent: Frag*) =
    link(diffRoutes.showSingleScenarioURL(this, scenario), linkContent)

  private def openScenarioInEditorLink(scenario: Scenario, linkContent: Frag*) =
    blankLink(diffRoutes.openScenarioInEditorURL(this, scenario), linkContent)

  def scenarioPage(scenario: Scenario, withLocation: Boolean = true): Text.TypedTag[String] = {
    page(
      // location
      if(withLocation)
        frag(
          div(CSS.locationLine)(scenarioLocationFrag(
            scenario = scenario,
            collection = Some(diffModel.scenario2Collection(scenario).toString)
          )),
          openScenarioInEditorLink(scenario,
            div(CSS.fileLocation)(
              scenario.sourceFile.toAbsolutePath.toString + ":" + Pickle(scenario.source, withLocation = true).location.map(_.head.line).getOrElse(0)
            )
          )
        )
      else
        frag(),
      // title
      div(CSS.scenarioTitleBox, CSS.scenarioTitleBig)(scenarioTitle(scenario)),
      // tags
      if(scenario.tags.isEmpty)
        frag()
      else
        div(CSS.tagLine)(
          div("Tags:"),
          scenario.tags.toSeq.sorted.map(tag => div(CSS.tag)(tag))
        ),
      // steps
      scenario.steps.map(stepFrag)
    )
  }

  private def showDetailedScenarioDiffLink(before: Scenario, after: Scenario, linkContent: Frag*) =
    link(diffRoutes.showDetailedScenarioDiffURL(this, before, after), linkContent)

  def detailedScenarioDiffPage(before: Scenario, after: Scenario): Text.TypedTag[String] = {
    val scenarioDiff = ScenarioDiff(before, after)

    def diffLineFrag[A](before: Frag, diff: Diff[A], after: Frag) =
      div(CSS.scenarioDiffLine)(
        div(CSS.scenarioDiffBefore)(before),
        div(if(diff.tag == Unchanged) CSS.scenarioDiffIndicatorUnchanged else CSS.scenarioDiffIndicatorChanged)(
          diff.tag match {
            case Added => "+"
            case Removed => "-"
            case _ => frag()
          }
        ),
        div(CSS.scenarioDiffAfter)(after)
      )

    page(
      // scenario location
      diffLineFrag(
        frag(
          div(CSS.locationLine)(scenarioLocationFrag(scenario = before, collection = Some(BeforeCollection.toString))),
          openScenarioInEditorLink(before,
            div(CSS.fileLocation)(
              before.sourceFile.toAbsolutePath.toString + ":" + Pickle(before.source, withLocation = true).location.map(_.head.line).getOrElse(0)
            )
          )
        ),
        Tuple2Diff(scenarioDiff.categories, scenarioDiff.featureName),
        frag(
          div(CSS.locationLine)(scenarioLocationFrag(scenario = after, collection = Some(AfterCollection.toString))),
          openScenarioInEditorLink(after,
            div(CSS.fileLocation)(
              after.sourceFile.toAbsolutePath.toString + ":" + Pickle(after.source, withLocation = true).location.map(_.head.line).getOrElse(0)
            )
          )
        )
      ),
      // scenario title
      diffLineFrag(
        div(CSS.scenarioTitleBox, CSS.scenarioTitleBig)(scenarioTitle(before)),
        Tuple2Diff(scenarioDiff.name, scenarioDiff.exampleIndex),
        div(CSS.scenarioTitleBox, CSS.scenarioTitleBig)(scenarioTitle(after)),
      ),
      // tags
      if(before.tags.isEmpty && after.tags.isEmpty) {
        frag()
      } else {
        def tagCss(tag: String) = scenarioDiff.tags.elementTags(tag) match {
          case Added => CSS.tagAdded
          case Removed => CSS.tagRemoved
          case _ => CSS.tag
        }
        diffLineFrag(
          div(CSS.tagLine)(
            div("Tags:"),
            before.tags.toSeq.sorted.map(tag => div(tagCss(tag))(tag))
          ),
          scenarioDiff.tags,
          div(CSS.tagLine)(
            div("Tags:"),
            after.tags.toSeq.sorted.map(tag => div(tagCss(tag))(tag))
          )
        )
      },
      // steps
      scenarioDiff.steps.paired.map {
        case diff@ElementDiff(before, after) =>
          diffLineFrag(
            if(before.isDefined) stepFrag(before.get) else frag(),
            diff,
            if(after.isDefined) stepFrag(after.get) else frag(),
          )
      }
    )
  }
}