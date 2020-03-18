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

import org.opencypher.tools.tck.api.Pickle
import org.opencypher.tools.tck.api.Scenario
import org.opencypher.tools.tck.api.Step

sealed trait ScenarioDiffTag
case object SourceUnchanged extends ScenarioDiffTag
case object Unchanged extends ScenarioDiffTag
case object Moved extends ScenarioDiffTag
case object Retagged extends ScenarioDiffTag
case object StepsChanged extends ScenarioDiffTag
case object SourceChanged extends ScenarioDiffTag
case object ExampleIndexChanged extends ScenarioDiffTag
case object PotentiallyRenamed extends ScenarioDiffTag
case object Different extends ScenarioDiffTag

case class ScenarioDiff(before: Scenario,
                        after: Scenario) {
  def diffSets[A](before: Set[A], after: Set[A]): (Boolean, Map[A, ElementaryDiff]) = {
    val unchanged: Map[A, ElementaryDiff] = (before intersect after).map(a => a -> ElementUnchanged).toMap
    val removed: Map[A, ElementaryDiff] = (before diff after).map(a => a -> ElementRemoved).toMap
    val added: Map[A, ElementaryDiff] = (after diff before).map(a => a -> ElementAdded).toMap
    (removed.nonEmpty || added.nonEmpty, unchanged ++ removed ++ added)
  }

  lazy val categories: Option[Int] = before.categories.zip(after.categories).
    map(p => p._1 != p._2).
    indexWhere(b => b) match {
    case ix if ix < 0 => None
    case ix => Some(ix)
  }

  def categoriesHaveChanged: Boolean = categories.nonEmpty

  lazy val featureNameHasChanged: Boolean = before.featureName != after.featureName

  lazy val nameHasChanged: Boolean = before.name != after.name

  lazy val exampleIndexHasChanged: Boolean = before.exampleIndex != after.exampleIndex

  lazy val (tagsHaveChanged: Boolean, tags: Map[String, ElementaryDiff]) = diffSets(before.tags, after.tags)

  lazy val steps: List[(Option[Step], ElementaryDiff, Option[Step])] = diffStepsAdvanced(before.steps, after.steps).map(
    p => (p._1, ElementaryDiff(p), p._2)
  )

  lazy val stepsHaveChanged: Boolean = after.steps != before.steps

  lazy val diffTags: Set[ScenarioDiffTag] = diffTags(before, after)

  lazy val potentialDuplicate: Boolean = diffTags subsetOf Set[ScenarioDiffTag](Moved, Retagged, ExampleIndexChanged, StepsChanged, PotentiallyRenamed)

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
    val lcsPairWindows = ((-1, -1), lcsPairs.head) :: lcsPairs.zip(lcsPairs drop 1)
    val paired = lcsPairWindows.flatMap {
      case ((bIxPrec, aIxPrec), (bIx, aIx)) =>
        val paired = diffStepsSimple(before.slice(bIxPrec + 1, bIx), after.slice(aIxPrec + 1, aIx))
        paired :+ (Some(before(bIx)), Some(after(aIx)))
    }
    paired
  }

  private def diffTags(before: Scenario, after: Scenario): Set[ScenarioDiffTag] = {
    val diff = Set[ScenarioDiffTag](Unchanged, SourceUnchanged, SourceChanged, Moved, Retagged, StepsChanged, ExampleIndexChanged, PotentiallyRenamed).filter {
      case Unchanged => equals(after)
      case SourceUnchanged =>
        before.equals(after) &&
          Pickle(after.source, withLocation = true) == Pickle(before.source, withLocation = true)
      case SourceChanged =>
        before.equals(after) &&
          Pickle(after.source, withLocation = true) != Pickle(before.source, withLocation = true)
      case Moved =>
        (categoriesHaveChanged || featureNameHasChanged) &&
          !nameHasChanged &&
          !exampleIndexHasChanged
      case Retagged =>
        !nameHasChanged &&
          !exampleIndexHasChanged &&
          tagsHaveChanged
      case StepsChanged =>
        !nameHasChanged &&
          !exampleIndexHasChanged &&
          stepsHaveChanged
      case ExampleIndexChanged =>
        !categoriesHaveChanged &&
          !featureNameHasChanged &&
          !nameHasChanged &&
          exampleIndexHasChanged &&
          !tagsHaveChanged &&
          !stepsHaveChanged &&
          Pickle(after.source) == Pickle(before.source)
      case PotentiallyRenamed =>
        nameHasChanged &&
          !stepsHaveChanged
      case _ => false
    }
    if(diff.isEmpty)
      Set[ScenarioDiffTag](Different)
    else
      diff
  }
}