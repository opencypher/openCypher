package org.opencypher.tools.tck

import org.opencypher.tools.tck.constants.TCKErrorDetails
import org.opencypher.tools.tck.constants.TCKErrorTypes
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

/**
 * This function validates a TCK error specification, which consists of three parts: type, phase and detail. Each
 * of these parts needs to be one of a pre-defined set of constants in order to be valid.
 */
trait ValidateError extends AnyFunSpecLike with Matchers {

  def validateError(typ: String, phase: String, detail: String): Unit = {
    it("has valid type") {
      TCKErrorTypes.ALL should contain(typ)
    }
    it("has valid phase") {
      Set("runtime", "compile time") should contain(phase)
    }
    it("has valid detail") {
      TCKErrorDetails.ALL should contain(detail)
    }
  }
}
