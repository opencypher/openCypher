/*
 * Copyright (c) 2015-2022 "Neo Technology,"
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
package org.opencypher.tools.tck.inspection.coverage

import org.antlr.v4.Tool
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.InterpreterRuleContext
import org.antlr.v4.runtime.ParserInterpreter
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.tool.Grammar
import org.opencypher.tools.grammar.Antlr4
import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.ExecQuery
import org.opencypher.tools.tck.api.Execute

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Paths

object SyntacticCoverage {
  private val RULES_PREFIX = "oC_"
  private val GRAMMAR_SOURCE = "/cypher.xml"

  private val oCGrammar = {
    val url = this.getClass.getResource(GRAMMAR_SOURCE)
    org.opencypher.grammar.Grammar.parseXML(Paths.get(url.toURI))
  }
  private val grammar: Grammar = {
    val grammarString = {
      val out = new ByteArrayOutputStream
      Antlr4.write(oCGrammar, out)
      out.toString(UTF_8.name)
    }
    val tool = new Tool()
    val ast = tool.parseGrammarFromString(grammarString)
    val grammar = tool.createGrammar(ast)
    tool.process(grammar, false)
    //println(grammar.getRuleNames.map(name => s"$name\t${grammar.getRule(name).numberOfAlts}").mkString(System.lineSeparator()))
    grammar
  }

  def main(args: Array[String]): Unit = {
    val scenarios = CypherTCK.allTckScenariosFromFilesystem("tck/features")
    val queries = scenarios.flatMap(_.steps.collect {
      case Execute(query, ExecQuery, _) => query
    })
    val (ruleUse, ruleUseDistinctByScenario) = collectRulesFromQueries(queries)
    val ruleCoverage: Map[Int, Seq[Int]] = ruleUse.groupBy(i => i)
    val scenarioDistinctRuleCoverage: Map[Int, Seq[Int]] = ruleUseDistinctByScenario.groupBy(i => i)

    val rulesNames = grammar.getRuleNames.map(_.substring(RULES_PREFIX.length))
    val rulesNamesMaxLength = rulesNames.map(_.length).max
    val lines = rulesNames.indices.map(i =>
      s"${rulesNames(i)}${" "*(rulesNamesMaxLength - rulesNames(i).length)}" +
      f"${ruleCoverage.get(i).map(_.size).getOrElse(0)}%7d" +
      f"${scenarioDistinctRuleCoverage.get(i).map(_.size).getOrElse(0)}%7d"
    )
    println(lines.mkString(System.lineSeparator()))
  }

  def collectRulesFromQueries(queries: Seq[String]): (Seq[Int], Seq[Int]) = {
    val trees = queries.map(query => Option(initParser(query).parse(grammar.getRule("oC_Cypher" ).index)))
    (trees.flatMap(t => collectRulesFromParseTree(t)), trees.flatMap(t => collectRulesFromParseTree(t).toSet))
  }

  def collectRulesFromParseTree(tree: Option[ParseTree]): Seq[Int] = tree match {
    case None => List[Int]()
    case Some(tree) =>
      tree.getPayload match {
        case payload: InterpreterRuleContext =>
          val children = (0 until tree.getChildCount).map(i => Option(tree.getChild(i)))
          val childRules = children.flatMap(child => collectRulesFromParseTree(child))
          val (rules, terminals) = children.foldLeft((List[InterpreterRuleContext](), List[TerminalNode]())){
            case (p, Some(child: InterpreterRuleContext)) => (child :: p._1, p._2)
            case (p, Some(child: TerminalNode)) => (p._1, child :: p._2)
            case (p, _) => p
          }
          val (numRules, numTerminals) = (rules.size, terminals.size)
          val isToCount = {
            if(numTerminals > 0) {
              true
            } else {
              val rule = grammar.getRule(payload.getRuleIndex)
              if(rule.name.endsWith("Expression") && numRules < 2 && rule.numberOfAlts < 2) {
                false
              } else {
                true
              }
            }
          }
          if(isToCount)
            payload.getRuleIndex +: childRules
          else
            childRules
        case _ => List[Int]()
      }
  }

  def initParser(query: String): ParserInterpreter = {
    val lexer = grammar.createLexerInterpreter(CharStreams.fromString(query))
    val parser = grammar.createParserInterpreter(new CommonTokenStream(lexer))
    lexer.removeErrorListeners()
    parser.removeErrorListeners()
    parser
  }
}
