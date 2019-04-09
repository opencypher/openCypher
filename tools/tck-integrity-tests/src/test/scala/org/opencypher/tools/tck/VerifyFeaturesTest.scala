package org.opencypher.tools.tck

class VerifyFeaturesTest extends TckTestSupport {

  test("Validate scenarios") {
    FeatureFormatChecker.validate()
  }
}
