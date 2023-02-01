/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
package org.opencypher.tools.tck.api.groups

import org.opencypher.tools.tck.api.Scenario
import org.scalatest.Inside
import org.scalatest.Inspectors
import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class TckTreeTest extends AnyFunSpec with GroupTest with Inspectors with Inside with OptionValues {
  private val rand = Random
  rand.setSeed(0)

  describe("Total group") {
    it("has indent of zero") {
      Total.indent shouldBe 0
    }
    it("has no parent") {
      Total.parent shouldBe None
    }
  }

  def invariants(tckTree: TckTree, scenarios: Seq[Scenario]): Unit = {
    it("should provide a map that contains every of original scenarios at least once") {
      tckTree.groupedScenarios.values.flatten.toSet should equal(scenarios.toSet)
    }
    describe("should have consistent parent-child relationship, i.e.") {
      tckTree.groups foreach {
        p =>
          describe(s"of parent group $p") {
            tckTree.groupChildren(p).foreach(c =>
              it(s"child $c has $p as parent") {
                c.parent should equal(Some(p))
              }
            )
          }
      }
    }
    describe("should provide a map where the key set") {
      it("has one Feature group per distinct feature per category") {
        tckTree.groups.count {
          case _: Feature => true
          case _ => false
        } should equal(scenarios.map(s => (s.categories, s.featureName)).distinct.size)
      }
      it("has one ScenarioCategory group per distinct category") {
        def prepCategoryList(categories: List[String], parent: List[String] = List[String]()): List[List[String]] = categories match {
          case Nil => List[List[String]]()
          case c :: cs => (parent :+ c) +: prepCategoryList(cs, parent :+ c)
        }
        tckTree.groups.count {
          case _: ScenarioCategory => true
          case _ => false
        } should equal(scenarios.flatMap(s => prepCategoryList(s.categories)).distinct.size)
      }
      it("has one Tag group per distinct tag") {
        tckTree.groups.count {
          case _: Tag => true
          case _ => false
        } should equal(scenarios.flatMap(_.tags).distinct.size)
      }
      describe("has every group, except Total, with a parent group so that") {
        tckTree.groups foreach {
          case Total => false
          case g =>
            it(s"group $g has a parent") {
              g.parent.isDefined shouldBe true
            }
        }
      }
      describe("has every group, except Total, indented one more than their parent group so that") {
        tckTree.groups foreach {
          case Total => ()
          case g =>
            it(s"group $g is indented one more than ${g.parent}") {
              g.indent should equal(g.parent.get.indent + 1)
            }
        }
      }
      describe("has every ScenarioCategory group with a parent group which is a ScenarioCategory or Total so that") {
        tckTree.groups foreach {
          case sc:ScenarioCategory =>
            it(s"ScenarioCategory group $sc has a parent which is a ScenarioCategory or Total") {
              sc.parent.value should matchPattern {
                case _:ScenarioCategory | Total =>
              }
            }
          case _ => ()
        }
      }
      describe("has every Feature group with a parent group which is a ScenarioCategory or Total so that") {
        tckTree.groups foreach {
          case f:Feature => it(s"Feature group $f has a parent which is a ScenarioCategory or Total") {
            f.parent.value should matchPattern {
              case _:ScenarioCategory | Total =>
            }
          }
          case _ => ()
        }
      }
      describe("has every ScenarioItem group and ScenarioOutline group with a parent group which is a Feature or a Tag so that") {
        tckTree.groups foreach {
          case si:ScenarioItem =>
            it(s"ScenarioItem group $si has a parent which is a Feature or a Tag") {
              si.parent.value should matchPattern {
                case _:Feature | _:Tag =>
              }
            }
          case so:ScenarioOutline =>
            it(s"ScenarioOutline group $so has a parent which is a Feature or a Tag") {
              so.parent.value should matchPattern {
                case _:Feature | _:Tag =>
              }
            }
          case _ => ()
        }
      }
      describe("has every ExampleItem group with a parent group which is a ScenarioOutline so that") {
        tckTree.groups foreach {
          case e:ExampleItem =>
            it(s"ExampleItem group $e has a parent which is a ScenarioOutline") {
              e.parent.value should matchPattern {
                case _:ScenarioOutline =>
              }
            }
          case _ => ()
        }
      }
      describe("has every Tag group with a parent group which is Total so that") {
        tckTree.groups foreach {
          case t:Tag =>
            it(s"Tag group $t has a parent which is Total") {
              t.parent.value shouldBe Total
            }
          case _ => ()
        }
      }
      describe("has every Item group grouping exactly the single scenario referenced in the group object so that") {
        tckTree.groups foreach {
          case i:Item =>
            it(s"Item group $i groups its and only its scenario") {
              tckTree.groupedScenarios(i) should equal(Set(i.scenario))
            }
          case _ => ()
        }
      }
    }
    describe("should provide groups in depth first order that") {
      val groupSequence = tckTree.groupsOrderedDepthFirst

      it("contains all original groups in the same cardinality") {
        groupSequence.toSet should equal(tckTree.groups)
        groupSequence.size should equal(tckTree.groups.size)
      }
      it("has Total group as the first group") {
        groupSequence.head shouldBe Total
      }
      it("has Total group no where else") {
        forAll(groupSequence.tail) { _ should not be Total }
      }
      describe("has every group after their parent group so that") {
        val groupsWithIndex = groupSequence.zipWithIndex
        groupsWithIndex.tail.foreach {
          case (g, gi) =>
            it(s"group $g is after ${g.parent}") {
              forExactly(1, groupsWithIndex) {
                x => inside(x) { case (p, pi) =>
                  p shouldBe g.parent.value
                  pi should be < gi
                }
              }
            }
        }
      }
    }
  }

  describe("The given list of four scenarios in a top-level feature, the TckTree") {
    val scr1A = createScenario(List[String](), "ftr1", Some(1), "a", None, None, Set[String]())
    val scr2B = createScenario(List[String](), "ftr1", Some(2), "b", None, None, Set[String]())
    val scr3A = createScenario(List[String](), "ftr1", Some(3), "a", None, None, Set[String]())
    val scr3B = createScenario(List[String](), "ftr1", Some(3), "b", None, None, Set[String]())

    val scenarios = rand.shuffle(List(scr1A, scr2B, scr3A, scr3B))
    val tckTree = TckTree(scenarios)

    invariants(tckTree, scenarios)

    it("should provide the given map") {
      val expected = Map(
        Total -> Set(scr1A, scr2B, scr3A, scr3B),
        Feature("ftr1", Total) -> Set(scr1A, scr2B, scr3A, scr3B),
        ScenarioItem(scr1A, Feature("ftr1", Total)) -> Set(scr1A),
        ScenarioItem(scr2B, Feature("ftr1", Total)) -> Set(scr2B),
        ScenarioItem(scr3A, Feature("ftr1", Total)) -> Set(scr3A),
        ScenarioItem(scr3B, Feature("ftr1", Total)) -> Set(scr3B),
      )

      tckTree.groupedScenarios should equal(expected)
    }

    it("should provide the given group sequence") {
      val expected = Seq(
        Total,
        Feature("ftr1", Total),
        ScenarioItem(scr1A, Feature("ftr1", Total)),
        ScenarioItem(scr2B, Feature("ftr1", Total)),
        ScenarioItem(scr3A, Feature("ftr1", Total)),
        ScenarioItem(scr3B, Feature("ftr1", Total)),
      )

      tckTree.groupsOrderedDepthFirst should equal(expected)
    }
  }

  describe("The given list of two scenarios from a scenario outline in a top-level feature, the TckTree") {
    val scr1 = createScenario(List[String](), "ftr1", Some(1), "scr", Some(1), None, Set[String]())
    val scr2 = createScenario(List[String](), "ftr1", Some(1), "scr", Some(2), Some("two"), Set[String]())

    val scenarios = rand.shuffle(List(scr1, scr2))
    val tckTree = TckTree(scenarios)

    invariants(tckTree, scenarios)

    it("should provide the given map") {
      val expected = Map(
        Total -> Set(scr1, scr2),
        Feature("ftr1", Total) -> Set(scr1, scr2),
        ScenarioOutline(Some(1), "scr", Feature("ftr1", Total)) -> Set(scr1, scr2),
        ExampleItem(1, None, scr1, ScenarioOutline(Some(1), "scr", Feature("ftr1", Total))) -> Set(scr1),
        ExampleItem(2, Some("two"), scr2, ScenarioOutline(Some(1), "scr", Feature("ftr1", Total))) -> Set(scr2),
      )

      tckTree.groupedScenarios should equal(expected)
    }

    it("should provide the given group sequence") {
      val expected = Seq(
        Total,
        Feature("ftr1", Total),
        ScenarioOutline(Some(1), "scr", Feature("ftr1", Total)),
        ExampleItem(1, None, scr1, ScenarioOutline(Some(1), "scr", Feature("ftr1", Total))),
        ExampleItem(2, Some("two"), scr2, ScenarioOutline(Some(1), "scr", Feature("ftr1", Total))),
      )

      tckTree.groupsOrderedDepthFirst should equal(expected)
    }
  }

  describe("The given list of four scenarios, the TckTree") {
    val scrA = createScenario(List[String](), "ftr5", Some(1), "scrA", None, None, Set[String]())
    val scrB = createScenario(List[String](), "ftr1", Some(1), "scrB", None, None, Set[String]("A"))
    val scrC = createScenario(List[String]("b"), "ftr11", Some(1), "scrC", None, None, Set[String]("A"))
    val scrD = createScenario(List[String]("b"), "ftr3", Some(1), "scrD", None, None, Set[String]("B"))

    val scenarios = rand.shuffle(List(scrA, scrB, scrC, scrD))
    val tckTree = TckTree(scenarios)

    invariants(tckTree, scenarios)

    val scB = ScenarioCategory("b", Total)
    val ftr3 = Feature("ftr3", scB)
    val ftr11 = Feature("ftr11", scB)
    val ftr1 = Feature("ftr1", Total)
    val ftr5 = Feature("ftr5", Total)

    it("should provide the given map") {
      val expected = Map(
        Total -> Set(scrA, scrB, scrC, scrD),
        scB -> Set(scrC, scrD),
        ftr3 -> Set(scrD),
        ScenarioItem(scrD, ftr3) -> Set(scrD),
        ftr11 -> Set(scrC),
        ScenarioItem(scrC, ftr11) -> Set(scrC),
        ftr1 -> Set(scrB),
        ScenarioItem(scrB, ftr1) -> Set(scrB),
        ftr5 -> Set(scrA),
        ScenarioItem(scrA, ftr5) -> Set(scrA),
        Tag("A") -> Set(scrB, scrC),
        ScenarioItem(scrB, Tag("A")) -> Set(scrB),
        ScenarioItem(scrC, Tag("A")) -> Set(scrC),
        Tag("B") -> Set(scrD),
        ScenarioItem(scrD, Tag("B")) -> Set(scrD),
      )

      tckTree.groupedScenarios should equal(expected)
    }

    it("should provide the given group sequence") {
      val expected = Seq(
        Total,
        scB,
        ftr3,
        ScenarioItem(scrD, ftr3),
        ftr11,
        ScenarioItem(scrC, ftr11),
        ftr1,
        ScenarioItem(scrB, ftr1),
        ftr5,
        ScenarioItem(scrA, ftr5),
        Tag("A"),
        ScenarioItem(scrB, Tag("A")),
        ScenarioItem(scrC, Tag("A")),
        Tag("B"),
        ScenarioItem(scrD, Tag("B")),
      )

      tckTree.groupsOrderedDepthFirst should equal(expected)
    }

    describe("when filtered out a category should result in a TckTree that") {
      val tckTreeFiltered = tckTree filter {
        case ScenarioCategory(n, _) if n == "b" => false
        case _ => true
      }

      invariants(tckTreeFiltered, rand.shuffle(List(scrA, scrB)))

      it("should provide the given map") {
        val expected = Map(
          Total -> Set(scrA, scrB),
          ftr1 -> Set(scrB),
          ScenarioItem(scrB, ftr1) -> Set(scrB),
          ftr5 -> Set(scrA),
          ScenarioItem(scrA, ftr5) -> Set(scrA),
          Tag("A") -> Set(scrB),
          ScenarioItem(scrB, Tag("A")) -> Set(scrB),
        )

        tckTreeFiltered.groupedScenarios should equal(expected)
      }

      it("should provide the given group sequence") {
        val expected = Seq(
          Total,
          ftr1,
          ScenarioItem(scrB, ftr1),
          ftr5,
          ScenarioItem(scrA, ftr5),
          Tag("A"),
          ScenarioItem(scrB, Tag("A")),
        )

        tckTreeFiltered.groupsOrderedDepthFirst should equal(expected)
      }
    }
  }

  describe("The given list of ten scenarios, the TckTree") {
    val scrA = createScenario(List[String](), "ftr5 - a", Some(1), "a", None, None, Set[String]())
    val scrB = createScenario(List[String](), "ftr1 - b", Some(1), "b", None, None, Set[String]("A"))
    val scrC = createScenario(List[String](), "ftr1 - b", Some(2), "c", None, None, Set[String]("A"))
    val scrD = createScenario(List[String]("b"), "ftr11 - c", Some(1), "d", None, None, Set[String]("A", "C"))
    val scrE = createScenario(List[String]("b"), "ftr11", Some(1), "e", None, None, Set[String]("A", "C"))
    val scrF1 = createScenario(List[String]("a", "b"), "ftr2", Some(1), "f", Some(1), None, Set[String]("C"))
    val scrF2 = createScenario(List[String]("a", "b"), "ftr2", Some(1), "f", Some(2), Some("two"), Set[String]("C"))
    val scrG = createScenario(List[String]("a", "b"), "ftr", Some(1), "g", None, None, Set[String]("D"))
    val scrH = createScenario(List[String]("b"), "ftr11 - b", Some(1), "h", None, None, Set[String]("D", "2"))
    val scrI = createScenario(List[String]("b"), "ftr3", Some(1), "i", None, None, Set[String]("B"))
    val scrJ = createScenario(List[String]("a", "b"), "ftrX", Some(1), "j", None, None, Set[String]("11"))

    val scenarios = rand.shuffle(List(scrA, scrB, scrC, scrD, scrE, scrF1, scrF2, scrG, scrH, scrI, scrJ))
    val tckTree = TckTree(scenarios)

    invariants(tckTree, scenarios)

    val scB = ScenarioCategory("b", Total)
    val scA = ScenarioCategory("a", Total)
    val scAB = ScenarioCategory("b", scA)
    val ftr = Feature("ftr", scAB)
    val ftr2 = Feature("ftr2", scAB)
    val ftrX = Feature("ftrX", scAB)
    val ftr3 = Feature("ftr3", scB)
    val ftr11 = Feature("ftr11", scB)
    val ftr11b = Feature("ftr11 - b", scB)
    val ftr11c = Feature("ftr11 - c", scB)
    val ftr1b = Feature("ftr1 - b", Total)
    val ftr5a = Feature("ftr5 - a", Total)

    it("should provide the given map") {
      val expected = Map(
        Total -> Set(scrA, scrB, scrC, scrD, scrE, scrF1, scrF2, scrG, scrH, scrI, scrJ),
        scA -> Set(scrF1, scrF2, scrG, scrJ),
        scAB -> Set(scrF1, scrF2, scrG, scrJ),
        ftr -> Set(scrG),
        ScenarioItem(scrG, ftr) -> Set(scrG),
        ftr2 -> Set(scrF1, scrF2),
        ScenarioOutline(Some(1), "f", ftr2) -> Set(scrF1, scrF2),
        ExampleItem(1, None, scrF1, ScenarioOutline(Some(1), "f", ftr2)) -> Set(scrF1),
        ExampleItem(2, Some("two"), scrF2, ScenarioOutline(Some(1), "f", ftr2)) -> Set(scrF2),
        ftrX -> Set(scrJ),
        ScenarioItem(scrJ, ftrX) -> Set(scrJ),
        scB -> Set(scrD, scrE, scrH, scrI),
        ftr3 -> Set(scrI),
        ScenarioItem(scrI, ftr3) -> Set(scrI),
        ftr11 -> Set(scrE),
        ScenarioItem(scrE, ftr11) -> Set(scrE),
        ftr11b -> Set(scrH),
        ScenarioItem(scrH, ftr11b) -> Set(scrH),
        ftr11c -> Set(scrD),
        ScenarioItem(scrD, ftr11c) -> Set(scrD),
        ftr1b -> Set(scrB, scrC),
        ScenarioItem(scrB, ftr1b) -> Set(scrB),
        ScenarioItem(scrC, ftr1b) -> Set(scrC),
        ftr5a -> Set(scrA),
        ScenarioItem(scrA, ftr5a) -> Set(scrA),
        Tag("11") -> Set(scrJ),
        ScenarioItem(scrJ, Tag("11")) -> Set(scrJ),
        Tag("2") -> Set(scrH),
        ScenarioItem(scrH, Tag("2")) -> Set(scrH),
        Tag("A") -> Set(scrB, scrC, scrD, scrE),
        ScenarioItem(scrB, Tag("A")) -> Set(scrB),
        ScenarioItem(scrD, Tag("A")) -> Set(scrD),
        ScenarioItem(scrE, Tag("A")) -> Set(scrE),
        ScenarioItem(scrC, Tag("A")) -> Set(scrC),
        Tag("B") -> Set(scrI),
        ScenarioItem(scrI, Tag("B")) -> Set(scrI),
        Tag("C") -> Set(scrD, scrE, scrF1, scrF2),
        ScenarioItem(scrD, Tag("C")) -> Set(scrD),
        ScenarioItem(scrE, Tag("C")) -> Set(scrE),
        ScenarioOutline(Some(1), "f", Tag("C")) -> Set(scrF1, scrF2),
        ExampleItem(1, None, scrF1, ScenarioOutline(Some(1), "f", Tag("C"))) -> Set(scrF1),
        ExampleItem(2, Some("two"), scrF2, ScenarioOutline(Some(1), "f", Tag("C"))) -> Set(scrF2),
        Tag("D") -> Set(scrG, scrH),
        ScenarioItem(scrG, Tag("D")) -> Set(scrG),
        ScenarioItem(scrH, Tag("D")) -> Set(scrH),
      )

      tckTree.groupedScenarios should equal(expected)
    }


    it("should provide the given group sequence") {
      val expected = Seq(
        Total,
        scA,
        scAB,
        ftr,
        ScenarioItem(scrG, ftr),
        ftr2,
        ScenarioOutline(Some(1), "f", ftr2),
        ExampleItem(1, None, scrF1, ScenarioOutline(Some(1), "f", ftr2)),
        ExampleItem(2, Some("two"), scrF2, ScenarioOutline(Some(1), "f", ftr2)),
        ftrX,
        ScenarioItem(scrJ, ftrX),
        scB,
        ftr3,
        ScenarioItem(scrI, ftr3),
        ftr11,
        ScenarioItem(scrE, ftr11),
        ftr11b,
        ScenarioItem(scrH, ftr11b),
        ftr11c,
        ScenarioItem(scrD, ftr11c),
        ftr1b,
        ScenarioItem(scrB, ftr1b),
        ScenarioItem(scrC, ftr1b),
        ftr5a,
        ScenarioItem(scrA, ftr5a),
        Tag("11"),
        ScenarioItem(scrJ, Tag("11")),
        Tag("2"),
        ScenarioItem(scrH, Tag("2")),
        Tag("A"),
        ScenarioItem(scrB, Tag("A")),
        ScenarioItem(scrD, Tag("A")),
        ScenarioItem(scrE, Tag("A")),
        ScenarioItem(scrC, Tag("A")),
        Tag("B"),
        ScenarioItem(scrI, Tag("B")),
        Tag("C"),
        ScenarioItem(scrD, Tag("C")),
        ScenarioItem(scrE, Tag("C")),
        ScenarioOutline(Some(1), "f", Tag("C")),
        ExampleItem(1, None, scrF1, ScenarioOutline(Some(1), "f", Tag("C"))),
        ExampleItem(2, Some("two"), scrF2, ScenarioOutline(Some(1), "f", Tag("C"))),
        Tag("D"),
        ScenarioItem(scrG, Tag("D")),
        ScenarioItem(scrH, Tag("D")),
      )

      tckTree.groupsOrderedDepthFirst should equal(expected)
    }
  }
}
