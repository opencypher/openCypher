package org.opencypher.tools.tck.reporting

import java.net.URI

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Scenario
import org.scalatest.FunSuite
import org.scalatest.Matchers

class CountScenariosTest extends FunSuite with Matchers {
  val fooUri: URI = getClass.getResource("cucumber").toURI
  val scenarios: Seq[Scenario] = CypherTCK.parseFeatures(fooUri).flatMap(_.scenarios)

  test("count scenario output") {
    val expectedCountOutput =
      """Total                      13
        || foo                       8
        || | bar                     6
        || | | boo                   4
        || | | | Feature: Boo        1
        || | | | Feature: Test       3
        || | | Feature: Test         2
        || | dummy                   2
        || | | Feature: Dummy        2
        || Feature: Foo              5
        || @Fail                     3
        || @TestA                    2
        || @TestB                    1
        || @TestC                    3""".stripMargin
    CountScenarios.report(scenarios).mkString(System.lineSeparator) should equal(expectedCountOutput)
  }
}
