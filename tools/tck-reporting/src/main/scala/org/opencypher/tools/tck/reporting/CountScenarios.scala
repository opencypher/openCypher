package org.opencypher.tools.tck.reporting

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Scenario

trait CountCategory {
  def name: String
}

case object Total extends CountCategory { val name = "Total" }

case class Tag(name: String) extends CountCategory

case class Feature(name: String) extends CountCategory

case object CountScenarios {
  def main(args: Array[String]): Unit = {
    val scenarios = CypherTCK.allTckScenarios
    val individualCounts = scenarios.map(s => {
        val totalMap = Map[CountCategory,Int](Total -> 1)
        // feature
        val featureMap = Map[CountCategory,Int](Feature(s.featureName) -> 1)
        // tags
        val tagsMap = s.tags.map(tag => (Tag(tag) -> 1)).toMap[CountCategory,Int]

        totalMap ++ featureMap ++ tagsMap
      })

    val totalCounts = individualCounts.foldLeft(Map[CountCategory,Int]()){
      case (cnt, cnts) => {
        (cnts.keySet ++ cnt.keySet).map {
          case key => key -> (cnts.getOrElse(key, 0) + cnt.getOrElse(key, 0))
        }.toMap
      }
    }

    println(totalCounts.map{ case (cat, count) => "" + cat + "\t" + count}.mkString(System.lineSeparator))
  }
}
