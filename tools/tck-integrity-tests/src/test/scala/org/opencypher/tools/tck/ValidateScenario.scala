package org.opencypher.tools.tck

import org.opencypher.tools.tck.api.ExpectError
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.constants.TCKTags
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

/**
 * This function will check that the input query string adheres to the specified code styling rules for Cypher queries.
 * If the code had bad styling, a message will be returned showing the bad query and a prettified version of it,
 * otherwise None will be returned.
 */
trait ValidateScenario extends AnyFunSpecLike with Matchers with OptionValues with ValidateSteps {

  private val scenarioNamesByFeature = scala.collection.mutable.HashMap[(List[String], String), scala.collection.mutable.HashMap[String, List[Int]]]()

  def validateScenario(scenario: Scenario): Unit = {
    it("has a number, greater zero") {
      scenario.number.value should be > 0
    }

    it("has a unique name in feature") {
      val key: (List[String], String) = (scenario.categories, scenario.featureName)
      val name = scenario.name
      val lineNumber = scenario.source.getLocation.getLine
      val scenarioNames = scenarioNamesByFeature.getOrElseUpdate(key, scala.collection.mutable.HashMap[String, List[Int]]())
      val lineNumbers = scenarioNames.getOrElseUpdate(name, List[Int]())
      lineNumbers should not contain lineNumber
    }

    describe("has valid steps, that") {
      validateSteps(scenario.steps, scenario.tags)
    }

    it("has a `@NegativeTest` tag and a `Then expect error` step or neither") {
      (scenario.steps exists {
        case _: ExpectError => true
        case _ => false
      }) should equal(scenario.tags contains TCKTags.NEGATIVE_TEST)
    }
  }
}
