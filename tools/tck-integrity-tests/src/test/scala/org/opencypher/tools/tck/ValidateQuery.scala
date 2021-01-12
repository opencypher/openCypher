package org.opencypher.tools.tck

import org.opencypher.tools.grammar.Antlr4TestUtils
import org.opencypher.tools.tck.api.ExecQuery
import org.opencypher.tools.tck.api.Execute
import org.opencypher.tools.tck.constants.TCKTags
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait ValidateQuery extends AnyFunSpecLike with Matchers {

  def validateQuery(execute: Execute, tags: Set[String] = Set.empty[String]): Unit = {
    val query = execute.query
    execute.qt match {
      case ExecQuery =>
        it(s"has either a syntax conforming to the grammar or the scenario has the ${TCKTags.SKIP_GRAMMAR_CHECK} tag") {
          val parsed = Try(Antlr4TestUtils.parse(query))
          val hasSkipGrammarCheckTag = tags contains TCKTags.SKIP_GRAMMAR_CHECK
          (parsed, hasSkipGrammarCheckTag) should matchPattern {
            case (Failure(_), true) =>
            case (Success(_), false) =>
          }
        }

        it(s"has either a syntax conforming to the style requirements or the scenario has the ${TCKTags.SKIP_STYLE_CHECK} tag") {
          val normalizedQuery = NormalizeQueryStyle(query)
          (query, tags contains TCKTags.SKIP_STYLE_CHECK) should matchPattern {
            case (x, false) if normalizedQuery == x =>
            case (_, true) =>
          }
        }
      case _ =>
        it(s"has a syntax conforming to the grammar") {
          val parsed = Try(Antlr4TestUtils.parse(query))
          parsed should matchPattern {
            case Success(_) =>
          }
        }

        it(s"has a syntax conforming to the style requirements") {
          query shouldBe NormalizeQueryStyle(query)
        }
    }
  }
}
