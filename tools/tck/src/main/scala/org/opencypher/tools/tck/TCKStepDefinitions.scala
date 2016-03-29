package org.opencypher.tools.tck

object TCKStepDefinitions {

  // for Background
  val BACKGROUND = "^$"

  // for Given
  val ANY = "^any graph$"
  val EMPTY = "^an empty graph$"
  val NAMED_GRAPH = "^the (.*) graph$"

  // for And
  val INIT_QUERY = "^having executed: (.*)$"
  val PARAMETERS = "^parameters are:$"
  val SIDE_EFFECTS = "^the side effects should be:$"

  // for When
  val EXECUTING_QUERY = "^executing query: (.*)$"

  // for Then
  val EXPECT_RESULT = "^the result should be:$"
  val EXPECT_SORTED_RESULT = "^the result should be, in order:$"
  val EXPECT_EMPTY_RESULT = "^the result should be empty$"
  val EXPECT_ERROR = "^a (.+) should be raised at (.+): (.+)$"
}
