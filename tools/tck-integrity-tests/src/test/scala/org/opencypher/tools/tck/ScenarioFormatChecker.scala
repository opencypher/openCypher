package org.opencypher.tools.tck

import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.groups.ContainerGroup
import org.opencypher.tools.tck.api.groups.Group
import org.opencypher.tools.tck.api.groups.Item
import org.opencypher.tools.tck.api.groups.Tag
import org.opencypher.tools.tck.api.groups.TckTree
import org.opencypher.tools.tck.api.groups.Total
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

trait ScenarioFormatChecker extends AnyFunSpecLike with Matchers with OptionValues with ValidateScenario {

  def create(scenarios: Seq[Scenario]): Unit = {

    implicit val tck: TckTree = TckTree(scenarios)

    def spawnTests(currentGroup: Group): Unit = {
      currentGroup match {
        case Total =>
          Total.children.foreach(spawnTests)
        case _: Tag => Unit
        case g: ContainerGroup =>
          describe(g.description) {
            g.children.foreach(spawnTests)
          }
        case i: Item =>
          describe(i.description) {
            validateScenario(i.scenario)
          }
          print(".")
          Console.flush()
        case _ => Unit
      }
    }

    spawnTests(Total)
  }
}
