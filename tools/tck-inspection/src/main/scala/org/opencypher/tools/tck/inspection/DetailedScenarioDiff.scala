/*
 * Copyright (c) 2015-2020 "Neo Technology,"
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
package org.opencypher.tools.tck.inspection

import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step

case class DetailedScenarioDiff(categories: Option[Int],
                                featureName: Boolean,
                                name: Boolean,
                                exampleIndex: Boolean,
                                tags: Map[String, ElementaryDiff],
                                steps: List[(Option[Step], ElementaryDiff, Option[Step])])

case object DetailedScenarioDiff {
  def apply(before: Scenario, after: Scenario): DetailedScenarioDiff = {
    def diffSets[A](before: Set[A], after: Set[A]): Map[A, ElementaryDiff] = {
      val unchanged: Map[A, ElementaryDiff] = (before intersect after).map(a => a -> Unchanged).toMap
      val removed: Map[A, ElementaryDiff] = (before diff after).map(a => a -> Removed).toMap
      val added: Map[A, ElementaryDiff] = (after diff before).map(a => a -> Added).toMap
      unchanged ++ removed ++ added
    }

    val categories = before.categories.zip(after.categories).
      map(p => p._1 != p._2).
      indexWhere(b => b) match {
      case ix if ix < 0 => None
      case ix => Some(ix)
    }
    val featureName = before.featureName != after.featureName
    val name = before.name != after.name
    val exampleIndex = before.exampleIndex != after.exampleIndex
    val tags = diffSets(before.tags, after.tags)
    val steps = diffStepsAdvanced(before.steps, after.steps).map(
      p => (p._1, ElementaryDiff(p), p._2)
    )
    DetailedScenarioDiff(categories, featureName, name, exampleIndex, tags, steps)
  }

  private def diffStepsSimple(before: List[Step], after: List[Step]): List[(Option[Step], Option[Step])] = {
    val beforeSome = before.map(Some(_))
    val afterSome = after.map(Some(_))
    val paired = beforeSome.zipAll(afterSome, None, None)
    paired
  }

  private def diffStepsAdvanced(before: List[Step], after: List[Step]): List[(Option[Step], Option[Step])] = {
    def lcsSteps(before: List[Step], after: List[Step], eq: (Step, Step) => Boolean): List[(Int, Int)] = {
      /**
       * Generic way to create memoized functions
       * @see https://stackoverflow.com/questions/25129721
       */
      case class Memo[I, K, O](f: I => O)(implicit ev: I => K) extends (I => O) {
        import scala.collection.mutable
        val cache: mutable.Map[K, O] = mutable.Map.empty[K, O]
        override def apply(x: I): O = cache.getOrElseUpdate(x, f(x))
      }

      def lcs[A](a: List[(A, Int)], b: List[(A, Int)], eq: (A, A) => Boolean): List[(Int, Int)] = {
        type DP = Memo[(List[(A, Int)], List[(A, Int)]), (Int, Int), List[(Int, Int)]]
        implicit def encode(key: (List[(A, Int)], List[(A, Int)])): (Int, Int) = (key._1.length, key._2.length)

        implicit val o: Ordering[List[(Int, Int)]] = Ordering.by(_.length)

        lazy val f: DP = Memo {
          case (Nil, _) | (_, Nil) => Nil
          case (x :: xs, y :: ys) if eq(x._1, y._1) => (x._2, y._2) :: f(xs, ys)
          case (x, y) => o.max(f(x.tail, y), f(x, y.tail))
        }

        f(a, b)
      }
      lcs(before.zipWithIndex, after.zipWithIndex, eq)
    }

    val lcsPairs = lcsSteps(before, after, (b, a) => b == a)
    val lcsPairWindows = ((0, 0), lcsPairs.head) :: lcsPairs.zip(lcsPairs drop 1)
    val paired = lcsPairWindows.flatMap {
      case ((bIxPrec, aIxPrec), (bIx, aIx)) =>
        val paired = diffStepsSimple(before.slice(bIxPrec + 1, bIx), after.slice(aIxPrec + 1, aIx))
        paired :+ (Some(before(bIx)), Some(after(aIx)))
    }
    paired
  }
}