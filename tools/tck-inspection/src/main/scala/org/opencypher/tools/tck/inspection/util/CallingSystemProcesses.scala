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
package org.opencypher.tools.tck.inspection.util

import org.opencypher.tools.tck.api.Pickle
import org.opencypher.tools.tck.api.Scenario

import scala.collection.mutable.ArrayBuffer
import scala.sys.process._

case class ProcessReturn(exitCode: Int, out: Seq[String], err: Seq[String], cmd: String)

object CallingSystemProcesses {

  def openScenarioInEditor(scenario: Scenario): ProcessReturn = {
    val cmdSeq =  Seq("subl", scenario.sourceFile.toAbsolutePath.toString + ":" + Pickle(scenario.source, withLocation = true).location.map(_.line).getOrElse(0))
    val cmd = Process(cmdSeq)

    val out = ArrayBuffer[String]()
    val err = ArrayBuffer[String]()
    val logger = ProcessLogger(line => out += line, line => err += line)

    val exitCode = cmd.!(logger)

    ProcessReturn(exitCode, out.toSeq, err.toSeq, cmdSeq.mkString(" "))
  }

  def checkoutRepoCommit(repositoryUrl: String, commit: String, localDirectory: String): ProcessReturn = {
    val gitCloneCmdSeq = Seq("git", "clone", repositoryUrl, localDirectory)
    val gitFetchCmdSeq = Seq("git", "-C", localDirectory, "fetch")
    val gitCheckoutCmdSeq = Seq("git", "-C", localDirectory, "checkout", commit)

    cloneFetchCheckout(gitCloneCmdSeq, gitFetchCmdSeq, gitCheckoutCmdSeq)
  }

  def checkoutMergedPR(repositoryUrl: String, pr: String, localDirectory: String): ProcessReturn = {
    val gitCloneCmdSeq = Seq("git", "clone", repositoryUrl, localDirectory)
    val gitFetchCmdSeq = Seq("git", "-C", localDirectory, "fetch", "origin", s"+refs/pull/$pr/merge")
    val gitCheckoutCmdSeq = Seq("git", "-C", localDirectory, "checkout", "-qf", "FETCH_HEAD")

    cloneFetchCheckout(gitCloneCmdSeq, gitFetchCmdSeq, gitCheckoutCmdSeq)
  }

  private def cloneFetchCheckout(gitCloneCmdSeq: Seq[String], gitFetchCmdSeq: Seq[String], gitCheckoutCmdSeq: Seq[String]): ProcessReturn = {
    val cmd =
      gitCloneCmdSeq #&&
        gitFetchCmdSeq #&&
        gitCheckoutCmdSeq
    val out = ArrayBuffer[String]()
    val err = ArrayBuffer[String]()
    val logger = ProcessLogger(line => out += line, line => err += line)

    val exitCode = cmd.!(logger)

    ProcessReturn(exitCode, out.toSeq, err.toSeq,
      gitCloneCmdSeq.mkString(" ") + "&&" +
        gitFetchCmdSeq.mkString(" ") + "&&" +
        gitCheckoutCmdSeq.mkString(" "))
  }
}