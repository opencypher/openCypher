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

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import cask.model.Response
import org.opencypher.tools.tck.inspection.util.CallingSystemProcesses
import org.opencypher.tools.tck.inspection.util.ProcessReturn
import scalatags.Text.all._

case class MainRoutes()(implicit val log: cask.Logger) extends cask.Routes with PageBasic {

  @cask.get("/")
  def main(): doctype =
    page(
      pageTitle("TCK Browser"),
      sectionTitle("Browse"),
      subSectionTitle("Git repository commit"),
      form(action:="/browserRepositoryCommit", method:="get")(
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
      subSectionTitle("Git repository PR"),
      form(action:="/browserRepositoryPR", method:="get")(
        table(
          tr(
            td("Repository"),
            td("PR#"),
            td("Subdirectory"),
            td(),
          ),
          tr(
            td(input(width:=25.em, `type`:="text", name:="repo", value:="https://github.com/openCypher/openCypher")),
            td(input(width:=10.em, `type`:="text", name:="pr", value:="")),
            td(input(width:=15.em, `type`:="text", name:="subPath", value:="tck/features")),
            td(input(`type`:="submit", value:="Show")),
          )
        )
      ),
      subSectionTitle("Local directory"),
      form(action:="/browserPath", method:="get")(
        table(
          tr(
            td(input(width:=50.em, `type`:="text", name:="path", value:="tck/features")),
            td(input(`type`:="submit", value:="Show")),
          )
        )
      ),
      sectionTitle("Diff"),
      subSectionTitle("Git repository commits"),
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
      subSectionTitle("Git repository PR with its main"),
      form(action:="/diffRepositoryPR", method:="get")(
        table(
          tr(
            td("Repository"),
            td("PR#"),
            td("Subpath"),
            td(),
          ),
          tr(
            td(input(width:=25.em, `type`:="text", name:="repo", value:="https://github.com/openCypher/openCypher")),
            td(input(width:=10.em, `type`:="text", name:="pr", value:="")),
            td(input(width:=15.em, `type`:="text", name:="subPath", value:="tck/features")),
            td(input(`type`:="submit", value:="Show")),
          )
        )
      ),
      subSectionTitle("Local directories"),
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
    )

  @cask.get("/diffRepositoryCommits")
  def diffRepositoryCommits(beforeRepo: String, beforeCommit: String, beforeSubPath: String, afterRepo: String, afterCommit: String, afterSubPath: String): Response[String] = {
    val beforeRepoDir = Files.createTempDirectory("diffRepositoryCommits-before")
    val beforeCheckoutResult = checkoutRepoCommit(beforeRepo, beforeCommit, beforeRepoDir.toString)
    val beforeTckDir = beforeRepoDir.resolve(beforeSubPath).toAbsolutePath

    val afterRepoDir = Files.createTempDirectory("diffRepositoryCommits-after")
    val afterCheckoutResult = checkoutRepoCommit(afterRepo, afterCommit, afterRepoDir.toString)
    val afterTckDir = afterRepoDir.resolve(afterSubPath).toAbsolutePath

    diffCheckouts(beforeCheckoutResult, beforeTckDir, afterCheckoutResult, afterTckDir)
  }

  @cask.get("/diffRepositoryPR")
  def diffRepositoryPR(repo: String, pr: String, subPath: String): Response[String] = {
    val beforeRepoDir = Files.createTempDirectory("diffRepositoryPR-before")
    val beforeCheckoutResult = checkoutRepoCommit(repo, "HEAD", beforeRepoDir.toString)
    val beforeTckDir = beforeRepoDir.resolve(subPath).toAbsolutePath

    val afterRepoDir = Files.createTempDirectory("diffRepositoryPR-after")
    val afterCheckoutResult = checkoutMergedPR(repo, pr, afterRepoDir.toString)
    val afterTckDir = afterRepoDir.resolve(subPath).toAbsolutePath

    diffCheckouts(beforeCheckoutResult, beforeTckDir, afterCheckoutResult, afterTckDir)
  }

  private def diffCheckouts(beforeCheckoutResult: Option[Frag], beforeTckDir: Path, afterCheckoutResult: Option[Frag], afterTckDir: Path): Response[String] = {
    val beforePathEnc = URLEncoder.encode(beforeTckDir.toString, StandardCharsets.UTF_8.toString)
    val afterPathEnc = URLEncoder.encode(afterTckDir.toString, StandardCharsets.UTF_8.toString)

    (beforeCheckoutResult, afterCheckoutResult) match {
      case (None, None) => redirect(s"/diff/$beforePathEnc/$afterPathEnc")
      case (beforeCheckoutResult, afterCheckoutResult) => cask.Response(
        error(
          p("Cannot checkout repos"),
          beforeCheckoutResult.map(f => p("Before:", br, f)).getOrElse(p("Before: Ok. TCK in: ", code(beforeTckDir.toString))),
          afterCheckoutResult.map(f => p("After:", br, f)).getOrElse(p("After: Ok. TCK in: ", code(afterTckDir.toString))),
        ).toString()
      )
    }
  }

  private def checkoutRepoCommit(repositoryUrl: String, commit: String, localDirectory: String): Option[Frag] =
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

  private def checkoutMergedPR(repositoryUrl: String, pr: String, localDirectory: String): Option[Frag] =
    CallingSystemProcesses.checkoutMergedPR(repositoryUrl, pr, localDirectory) match {
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
    redirect(s"/diff/$beforePathEnc/$afterPathEnc")
  }

  @cask.get("/browserRepositoryCommit")
  def browserRepositoryCommit(repo: String, commit: String, subPath: String): Response[String] = {
    val repoDir = Files.createTempDirectory("browserRepositoryCommit")
    val checkoutResult = checkoutRepoCommit(repo, commit, repoDir.toString)
    val tckDir = repoDir.resolve(subPath).toAbsolutePath

    browseCheckouts(checkoutResult, tckDir)
  }

  @cask.get("/browserRepositoryPR")
  def browserRepositoryPR(repo: String, pr: String, subPath: String): Response[String] = {
    val repoDir = Files.createTempDirectory("browserRepositoryPR")
    val checkoutResult = checkoutMergedPR(repo, pr, repoDir.toString)
    val tckDir = repoDir.resolve(subPath).toAbsolutePath

    browseCheckouts(checkoutResult, tckDir)
  }

  private def browseCheckouts(checkoutResult: Option[Frag], tckDir: Path): Response[String] = {
    val pathEnc = URLEncoder.encode(tckDir.toString, StandardCharsets.UTF_8.toString)

    checkoutResult match {
      case None => redirect(s"/browser/$pathEnc")
      case Some(frag) => cask.Response(
        "" +
          error(
          p("Cannot checkout repo"),
          p(frag)
        )
      )
    }
  }

  @cask.get("/browserPath")
  def browserPath(path: String): Response[String] = {
    val pathEnc = URLEncoder.encode(path, StandardCharsets.UTF_8.toString)
    redirect(s"/browser/$pathEnc")
  }

  private def redirect(url: String): Response[String] = cask.Response("", statusCode = 307, Seq("Location" -> url), Nil)

  initialize()
}

object TckBrowserWeb extends cask.Main {
  val allRoutes = Seq(MainRoutes(), BrowserRoutes(), DiffRoutes())

  override def main(args: Array[String]): Unit = {
    super.main(args)
    println("Open your browser at http://localhost:8080")
  }
}