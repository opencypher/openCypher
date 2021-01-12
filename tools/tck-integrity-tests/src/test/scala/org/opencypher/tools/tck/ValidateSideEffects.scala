package org.opencypher.tools.tck

import io.cucumber.core.gherkin.DataTableArgument
import org.opencypher.tools.tck.api.SideEffects
import org.opencypher.tools.tck.constants.TCKSideEffects
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

/**
 * Validates side effects expectations. A valid side effect has one of the specified names in TCKSideEffects, and a
 * quantity that is an integer greater than zero.
 */
trait ValidateSideEffects extends AnyFunSpecLike with Matchers {

  def validateSideEffects(step: SideEffects): Unit = {
    val keys = step.expected.v.keySet
    val values = step.expected.v.values

    it("has no invalid keys") {
      import org.scalatest.enablers.Emptiness.emptinessOfGenTraversable
      (keys -- TCKSideEffects.ALL) shouldBe empty
    }

    it("has only numbers greater than zero in step parameter (or no parameter)") {
      if (step.source.getArgument != null) {
        // note this tests that principally valid zero side effects are not listed in the scenario's gherkin code
        val dataTable = step.source.getArgument.asInstanceOf[DataTableArgument].cells().asScala.map(_.asScala.toList).toList
        val map = dataTable.map { r => r.head -> r.tail.head.toInt }.toMap
        all(map.values) should be > 0
      } else {
        succeed
      }
    }

    it("has only numbers greater or equal zero, after filled with zero") {
      all(values) should be >= 0
    }
  }
}
