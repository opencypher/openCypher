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

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import cask.model.Response
import org.opencypher.tools.tck.api.CypherValueRecords
import org.opencypher.tools.tck.api.Dummy
import org.opencypher.tools.tck.api.ExecQuery
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
import scalatags.Text
import scalatags.Text.all._
import scalatags.Text.tags2
import scalatags.generic
import scalatags.text.Builder

trait PageBasic {
  def categorySeparator = "⟩"

  def inlineSpacer(): Text.TypedTag[String] = span(width:=1.em, display.`inline-block`)(" ")

  def pageTitle(title: Frag*): Text.TypedTag[String] = h1(CSS.pageTitle)(title)

  def sectionTitle(title: Frag*): Text.TypedTag[String] = h2(CSS.sectionTitle)(title)

  def subSectionTitle(title: Frag*): Text.TypedTag[String] = h3(CSS.subSectionTitle)(title)

  def error(msg: Frag*): Text.TypedTag[String] = page(div(color.red)(msg))

  def page(content: Frag*): Text.TypedTag[String] = html(
    head(
      meta(charset:="utf-8"),
      tags2.style(CSS.styleSheetText)
    ),
    body(content)
  )

  def scenarioTitle(scenario: Scenario): String =
    scenario.name + scenario.exampleIndex.map(i => " #" + i).getOrElse("")

  def scenarioLocationFrag(scenario: Scenario,
                           collection: Option[String] = None,
                           showUrl: Option[String] = None,
                           sourceUrl: Option[String] = None): generic.Frag[Builder, String] =
    frag(
      collection match {
        case None => frag()
        case Some(col) => span(CSS.tckCollection)(col)
      },
      scenario.categories.flatMap(c => Seq(span(CSS.categorySepInLocationLine)(categorySeparator), span(CSS.categoryNameInLocationLine)(c))),
      span(CSS.featureIntroInLocationLine)(categorySeparator, categorySeparator), span(CSS.featureNameInLocationLine)(scenario.featureName),
      showUrl match {
        case None => frag()
        case Some(url) => span(CSS.scenarioLinkInLocationLine)(link(url, "[show]"))
      },
      sourceUrl match {
        case None => frag()
        case Some(url) => span(CSS.scenarioLinkInLocationLine)(blankLink(url,"[code]"))
      },
    )

  def link(url: String, linkContent: Frag*): Text.TypedTag[String] =
    a(href:=url)(linkContent)

  def blankLink(url: String, linkContent: Frag*): Text.TypedTag[String] =
    a(href:=url, target:="_blank")(linkContent)

  def stepFrag(step: Step): Text.TypedTag[String] = {
    def stepFrag(name: String, content: Frag*) =
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
                td()(k),
                td()(v.toString)
              )
          ))
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

case class MainRoutes()(implicit val log: cask.Logger) extends cask.Routes with PageBasic {
  //  val beforeDuplicates = CountScenarios.potentialDuplicates(scenariosBefore).filterNot {
//    case (a, b, _) => a.categories.head == "uncategorized" && b.categories.head == "uncategorized"
//  }
//
//  val afterDuplicates = CountScenarios.potentialDuplicates(scenariosAfter).filterNot {
//    case (a, b, _) => a.categories.head == "uncategorized" && b.categories.head == "uncategorized"
//  }
//
//  val missing = diffs(Total).removed.filterNot(s => beforeDuplicates.exists{
//    case (a, b, _) => s == a || s == b
//  })
//
//  val extra = diffs(Total).added.filterNot(s => afterDuplicates.exists{
//    case (a, b, _) => s == a || s == b
//  })

  @cask.get("/")
  def main(): String = {
    page(
      pageTitle("TCK Inspector"),
      sectionTitle("Diff"),
      subSectionTitle("From git repository commits"),
      form(action:="/diffRepositoryCommits", method:="get")(
        table(
          tr(
            td(),
            td("Repository"),
            td("Commit"),
            td("Subpath"),
            td(),
          ),
          tr(
            td(CSS.tckCollection)(BeforeCollection.toString),
            td(input(width:=25.em, `type`:="text", name:="beforeRepo", value:="https://github.com/openCypher/openCypher")),
            td(input(width:=10.em, `type`:="text", name:="beforeCommit", value:="HEAD")),
            td(input(width:=15.em, `type`:="text", name:="beforeSubPath", value:="tck/features")),
            td(),
          ),
          tr(
            td(CSS.tckCollection)(AfterCollection.toString),
            td(input(width:=25.em, `type`:="text", name:="afterRepo")),
            td(input(width:=10.em, `type`:="text", name:="afterCommit")),
            td(input(width:=15.em, `type`:="text", name:="afterSubPath", value:="tck/features")),
            td(input(`type`:="submit", value:="Show")),
          )
        )
      ),
      subSectionTitle("From local directories"),
      form(action:="/diffPaths", method:="get")(
        table(
          tr(
            td(CSS.tckCollection)(BeforeCollection.toString),
            td(input(width:=50.em, `type`:="text", name:="before")),
            td(),
          ),
          tr(
            td(CSS.tckCollection)(AfterCollection.toString),
            td(input(width:=50.em, `type`:="text", name:="after")),
            td(input(`type`:="submit", value:="Show")),
          )
        )
      ),
      sectionTitle("Inspect"),
      subSectionTitle("From git repository commit"),
      form(action:="/inspectRepositoryCommit", method:="get")(
        table(
          tr(
            td("Repository"),
            td("Commit"),
            td("Subdirectory"),
            td(),
          ),
          tr(
            td(input(width:=25.em, `type`:="text", name:="repo", value:="https://github.com/openCypher/openCypher")),
            td(input(width:=10.em, `type`:="text", name:="commit", value:="HEAD")),
            td(input(width:=15.em, `type`:="text", name:="subPath", value:="tck/features")),
            td(input(`type`:="submit", value:="Show")),
          )
        )
      ),
      subSectionTitle("From local directory"),
      form(action:="/inspectPath", method:="get")(
        table(
          tr(
            td(input(width:=50.em, `type`:="text", name:="path")),
            td(input(`type`:="submit", value:="Show")),
          )
        )
      ),
    ).toString()
  }

  @cask.get("/diffRepositoryCommits")
  def diffRepositoryCommits(beforeRepo: String, beforeCommit: String, beforeSubPath: String, afterRepo: String, afterCommit: String, afterSubPath: String): Response[String] = {
    val beforeRepoDir = Files.createTempDirectory("diffRepositoryCommits-before")
    val beforeCheckoutResult = checkoutRepoCommit(beforeRepo, beforeCommit, beforeRepoDir.toString)
    val beforeTckDir = beforeRepoDir.resolve(beforeSubPath).toAbsolutePath
    val beforePathEnc = URLEncoder.encode(beforeTckDir.toString, StandardCharsets.UTF_8.toString)

    val afterRepoDir = Files.createTempDirectory("diffRepositoryCommits-after")
    val afterCheckoutResult = checkoutRepoCommit(afterRepo, afterCommit, afterRepoDir.toString)
    val afterTckDir = afterRepoDir.resolve(afterSubPath).toAbsolutePath
    val afterPathEnc = URLEncoder.encode(afterTckDir.toString, StandardCharsets.UTF_8.toString)

    (beforeCheckoutResult, afterCheckoutResult) match {
      case (None, None) => cask.Redirect(s"/diff/$beforePathEnc/$afterPathEnc")
      case (beforeCheckoutResult, afterCheckoutResult) => cask.Response(
        error(
          p("Cannot checkout repos"),
          beforeCheckoutResult.map(f => p("Before:", br, f)).getOrElse(p("Before: Ok. TCK in: ", code(beforeTckDir.toString))),
          afterCheckoutResult.map(f => p("After:", br, f)).getOrElse(p("After: Ok. TCK in: ", code(afterTckDir.toString))),
        ).toString()
      )
    }
  }

  private def checkoutRepoCommit(repositoryUrl: String, commit: String, localDirectory: String) =
    CallingSystemProcesses.checkoutRepoCommit(repositoryUrl, commit, localDirectory) match {
      case ProcessReturn(0, _, _, _) => None
      case ProcessReturn(x, out, err, cmd) =>
        Some(
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

  @cask.get("/diffPaths")
  def diffPaths(before: String, after: String): Response[String] = {
    val beforePathEnc = URLEncoder.encode(before, StandardCharsets.UTF_8.toString)
    val afterPathEnc = URLEncoder.encode(after, StandardCharsets.UTF_8.toString)
    cask.Redirect(s"/diff/$beforePathEnc/$afterPathEnc")
  }
  @cask.get("/inspectRepositoryCommit")
  def inspectRepositoryCommit(repo: String, commit: String, subPath: String): Response[String] = {
    val repoDir = Files.createTempDirectory("inspectRepositoryCommit")
    val checkoutResult = checkoutRepoCommit(repo, commit, repoDir.toString)
    val tckDir = repoDir.resolve(subPath).toAbsolutePath
    val pathEnc = URLEncoder.encode(tckDir.toString, StandardCharsets.UTF_8.toString)

    checkoutResult match {
      case None => cask.Redirect(s"/inspect/$pathEnc")
      case Some(frag) => cask.Response(
        error(
          p("Cannot checkout repo"),
          p(frag)
        ).toString()
      )
    }
  }

  @cask.get("/inspectPath")
  def inspectPath(path: String): Response[String] = {
    val pathEnc = URLEncoder.encode(path, StandardCharsets.UTF_8.toString)
    cask.Redirect(s"/inspect/$pathEnc")
  }

  initialize()
}

object TckInspectorWeb extends cask.Main {
  val allRoutes = Seq(MainRoutes(), InspectRoutes(), DiffRoutes())
}