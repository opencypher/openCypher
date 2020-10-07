package org.opencypher.tools.tck

import java.util

import cypher.features.InterpretedTCKTests
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class TCKImplementationTest {
  @TestFactory
  def runNeo4j(): util.Collection[DynamicTest] =
    (new InterpretedTCKTests()).runInterpreted()
}
