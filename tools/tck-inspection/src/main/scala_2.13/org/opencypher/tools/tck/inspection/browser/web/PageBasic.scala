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

import org.opencypher.tools.tck.api.CypherValueRecords
import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.ExecQuery
import org.opencypher.tools.tck.api.ControlQuery
import org.opencypher.tools.tck.api.CsvFile
import org.opencypher.tools.tck.api.Execute
import org.opencypher.tools.tck.api.ExpectError
import org.opencypher.tools.tck.api.ExpectResult
import org.opencypher.tools.tck.api.InitQuery
import org.opencypher.tools.tck.api.Measure
import org.opencypher.tools.tck.api.Parameters
import org.opencypher.tools.tck.api.RegisterProcedure
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.SideEffectQuery
import org.opencypher.tools.tck.api.SideEffects
import org.opencypher.tools.tck.api.Step
import org.opencypher.tools.tck.constants.TCKSideEffects.ADDED_LABELS
import org.opencypher.tools.tck.constants.TCKSideEffects.ADDED_NODES
import org.opencypher.tools.tck.constants.TCKSideEffects.ADDED_PROPERTIES
import org.opencypher.tools.tck.constants.TCKSideEffects.ADDED_RELATIONSHIPS
import org.opencypher.tools.tck.constants.TCKSideEffects.DELETED_LABELS
import org.opencypher.tools.tck.constants.TCKSideEffects.DELETED_NODES
import org.opencypher.tools.tck.constants.TCKSideEffects.DELETED_PROPERTIES
import org.opencypher.tools.tck.constants.TCKSideEffects.DELETED_RELATIONSHIPS
import org.opencypher.tools.tck.api.groups.Group
import scalatags.Text
import scalatags.Text.all._
import scalatags.Text.tags2
import scalatags.generic
import scalatags.text.Builder

import scala.language.implicitConversions

trait PageBasic {
  class FragOption[T](option: Option[T]) {
    def mapToFrag(f: T => Frag): Frag = option match {
      case None => frag()
      case Some(value) => f(value)
    }
  }

  implicit def optionToFragOption[T](option: Option[T]): FragOption[T] = new FragOption[T](option)

  def categorySeparator = "⟩"

  def inlineSpacer(): Text.TypedTag[String] = span(width:=1.em, display.`inline-block`)(" ")

  def pageTitle(title: Frag*): Text.TypedTag[String] = h1(CSS.pageTitle)(title)

  def sectionTitle(title: Frag*): Text.TypedTag[String] = h2(CSS.sectionTitle)(title)

  def subSectionTitle(title: Frag*): Text.TypedTag[String] = h3(CSS.subSectionTitle)(title)

  def error(msg: Frag*): Text.all.doctype = page(div(color.red)(msg))

  def page(content: Frag*): Text.all.doctype = doctype("html")(
    html(
      head(
        meta(charset:="utf-8"),
        tags2.style(CSS.styleSheetText)
      ),
      body(content)
    )
  )

  def listScenariosPage(scenarios: Group => Option[Set[Scenario]], group: Group, kind: Option[Frag], showSingleScenarioURL: Scenario => String, openScenarioInEditorURL: Scenario => String ): Text.all.doctype = {
    val scenarioSeq = scenarios(group).map(_.toSeq.sortBy(s => (s.categories.mkString("/"), s.featureName, s.number, s.name, s.exampleIndex))).getOrElse(Seq.empty[Scenario])
    page(
      pageTitle(scenarioSeq.size, kind.mapToFrag(k => frag(" ", k)), " scenario(s) in group ", i(group.toString)),
      ul(
        for(s <- scenarioSeq) yield
          li(
            scenarioLocationFrag(s),
            inlineSpacer(),
            s.number.map(n => frag(s"[$n]", inlineSpacer())).getOrElse(frag()),
            link(showSingleScenarioURL(s), scenarioTitle(s)),
            inlineSpacer(),
            blankLink(openScenarioInEditorURL(s), "[code]"),
          )
      )
    )
  }

  def scenarioTitle(scenario: Scenario): String =
    scenario.name + scenario.exampleIndex.map(i => " #" + i).getOrElse("") + scenario.exampleName.map(n => " (" + n + ")").getOrElse("")

  def scenarioLocationFrag(scenario: Scenario,
                           collection: Option[String] = None,
                           showUrl: Option[String] = None,
                           sourceUrl: Option[String] = None): generic.Frag[Builder, String] =
    frag(
      collection.mapToFrag(col =>
        span(CSS.tckCollection)(col)
      ),
      scenario.categories.flatMap(c =>
        Seq(span(CSS.categorySepInLocationLine)(categorySeparator), span(CSS.categoryNameInLocationLine)(c))
      ),
      span(CSS.featureIntroInLocationLine)(categorySeparator, categorySeparator), span(CSS.featureNameInLocationLine)(scenario.featureName),
      showUrl.mapToFrag(url =>
        span(CSS.scenarioLinkInLocationLine)(link(url, "[show]"))
      ),
      sourceUrl.mapToFrag(url =>
        span(CSS.scenarioLinkInLocationLine)(blankLink(url,"[code]"))
      ),
    )

  trait Anchor

  def anchor(anchor: Anchor): Text.TypedTag[String] = a(name:=anchor.toString)

  def link2LocalAnchor(anchor: Anchor, linkContent: Frag*): Text.TypedTag[String] =
    a(href:="#"+anchor.toString)(linkContent)

  def link(url: String, linkContent: Frag*): Text.TypedTag[String] =
    a(href:=url)(linkContent)

  def blankLink(url: String, linkContent: Frag*): Text.TypedTag[String] =
    a(href:=url, target:="_blank")(linkContent)

  def stepFrag(step: Step): Text.TypedTag[String] = {
    def stepFrag(name: String, content: Frag*): Text.TypedTag[String] =
      div(CSS.step)(
        div(if (content.isEmpty) CSS.emptyStepName else CSS.stepName)(name),
        if(content.nonEmpty) div(CSS.stepContent)(content) else frag()
      )

    step match {
      case Dummy(_) =>
        stepFrag(
          "Setup an empty graph"
        )
      case Parameters(values, _) =>
        stepFrag(
          "Parameters",
          div(table()(
            for((k,v) <- values.toSeq) yield
              tr(
                td()(code(k)),
                td()("="),
                td()(code(v.toString))
              )
          ))
        )
      case CsvFile(urlParameter, values, _) =>
        stepFrag(
          "Create CSV file",
          div("containing"),
          div(cypherValueRecordsFrag(values)),
          div("file URL available in parameter ", code("$", urlParameter))
        )
      case RegisterProcedure(signature, values, _) =>
        stepFrag(
          "Registered procedure",
          div(code(signature)),
          div(cypherValueRecordsFrag(values))
        )
      case Measure(source) =>
        stepFrag(
          "Measure side effects"
        )
      case Execute(query, qt, source) =>
        stepFrag(
          qt match {
            case InitQuery => "Initialize with"
            case ExecQuery => "Execute query"
            case ControlQuery => "Control query"
            case SideEffectQuery => "Execute update"
          },
          div(pre(fontFamily:="Monospace")(query))
        )
      case ExpectResult(expectedResult, _, sorted) =>
        stepFrag(
          "Expect result, " + (if (sorted) "in order" else "in any order"),
          div(cypherValueRecordsFrag(expectedResult))
        )
      case SideEffects(expected, _) =>
        val sideEffectOrder = Seq(ADDED_NODES, ADDED_RELATIONSHIPS, ADDED_LABELS, ADDED_PROPERTIES,
          DELETED_NODES, DELETED_RELATIONSHIPS, DELETED_LABELS, DELETED_PROPERTIES)
        stepFrag(
          "Check side effects",
          div(table()(
            for(eff <- sideEffectOrder) yield
              tr(
                td()(eff),
                td()(expected.v.getOrElse(eff, 0).toString)
              )
          ))
        )
      case ExpectError(errorType, phase, detail, _) =>
        stepFrag("Expect error",
          div(table()(
            tr(td()(b("Type:")), td()(errorType)),
            tr(td()(b("Phase:")), td()(phase)),
            tr(td()(b("Detail:")), td()(detail)),
          ))
        )
    }
  }

  def cypherValueRecordsFrag(values: CypherValueRecords): Text.TypedTag[String] =
    table()(
      tr(
        for(col <- values.header) yield th(col)
      ),
      for(row <- values.rows) yield
        tr(
          for(col <- values.header) yield td(code(row(col).toString))
        )
    )
}
