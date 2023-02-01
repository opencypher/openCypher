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
package org.opencypher.tools.tck

import java.nio.file.Files
import java.nio.file.Paths

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.groups.Feature
import org.opencypher.tools.tck.api.groups.ScenarioCategory
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.sys.process._

class GenerateTCKIndexDocTest extends AnyFunSuite {
  //used test(...)/ignore(...) if index.adoc should/should not be generated during build
  test("generate index doc") {
    GenerateTCKIndexDoc.main(Array())
  }

  //used test(...)/ignore(...) if generated index.adoc should/should not be checked against working copy HEAD during build
  ignore("generate index doc should be committed") {
    val tmpFile = Files.createTempFile("tck-", "-index.adoc")
    val gitCmd = Seq("git", "-C", Paths.get(System.getProperty("projectRootdir")).toAbsolutePath.toString,
      "show", s"HEAD:${GenerateTCKIndexDoc.indexDocFileRelative}")
    val cmd = gitCmd #>> tmpFile.toFile

    val out = ArrayBuffer[String]()
    val err = ArrayBuffer[String]()
    val logger = ProcessLogger(line => out += line, line => err += line)
    val exitCode = cmd.!(logger)

    if(exitCode == 0) {
      val generated = GenerateTCKIndexDoc.generate
      val checkedIn = {
        val src = Source.fromFile(tmpFile.toString)
        val content = src.getLines().mkString(System.lineSeparator)
        src.close
        content
      }
      assert(generated == checkedIn, s"generated ${GenerateTCKIndexDoc.indexDocFileRelative} does not equal the version found in HEAD")
      tmpFile.toFile.deleteOnExit()
    } else {
      fail(s"""could not get version of ${GenerateTCKIndexDoc.indexDocFileRelative} in current HEAD:
              |
              |${gitCmd.mkString(" ")} > ${tmpFile.toString}
              |
              |${err.mkString(System.lineSeparator())}""".stripMargin)
    }
  }
}

/*
 * Call with -DprojectRootdir=<repo-root> (as absolut path)
 */
object GenerateTCKIndexDoc {
  val indexDocFileRelative = "tck/index.adoc"
  private val indexDocFileAbsolute = System.getProperty("projectRootdir") + "/" + indexDocFileRelative

  def main(args: Array[String]): Unit = {
    write(generate)
    println("Generated " + indexDocFileRelative)
  }

  def generate: String = {
    val scenarios = CypherTCK.allTckScenarios
    val tckTree = TckTree(scenarios).filter({
      case ScenarioCategory("precategorized", _) => false
      case ScenarioCategory("uncategorized", _) => false
      case _ => true
    })

    tckTree.groupsOrderedDepthFirst map {
      case Total =>
        Some("""= TCK Index
          |
          |The TCK is split into categories based on language constructs.
          |The two main groups are clauses and expressions.
          |Each group enumerates its members.
          |Within each member, there are additional categories.
          |""".stripMargin)
      case sc@ScenarioCategory(name, _) =>
        val prettyName = name.substring(0,1).toUpperCase + name.substring(1).replaceAll("([A-Z])", " $1")
        Some(System.lineSeparator() + s"=${"=" * sc.indent} $prettyName" + System.lineSeparator())
      case Feature(name, _) =>
        Some(s"* $name")
      case _ => None
    } collect {
      case Some(line) => line
    } mkString System.lineSeparator()
  }

  def write(indexDoc: String): Unit = {
    import java.io._
    val pw = new PrintWriter(new File(indexDocFileAbsolute))
    pw.write(indexDoc)
    pw.close()
  }
}
