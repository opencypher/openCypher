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
package org.opencypher.tools.tck.inspection.diff

import org.opencypher.tools.tck.inspection.diff.ElementaryDiffTag.Changed
import org.opencypher.tools.tck.inspection.diff.ElementaryDiffTag.Different
import org.opencypher.tools.tck.inspection.diff.ElementaryDiffTag.Unchanged

import scala.language.implicitConversions

sealed trait ElementaryDiffTag

object ElementaryDiffTag {
  case object Added extends ElementaryDiffTag
  case object Removed extends ElementaryDiffTag
  case object Unchanged extends ElementaryDiffTag
  case object Changed extends ElementaryDiffTag
  case object Different extends ElementaryDiffTag

  def apply(changed: Boolean): ElementaryDiffTag = if(changed) Changed else Unchanged

  def apply[E](pair: (E, E)): ElementaryDiffTag = pair match {
    case (Some(_), None) => Removed
    case (None, Some(_)) => Added
    case (b, a) if b == a => Unchanged
    case (b, a) if b != a => Changed
  }
}

trait Diff[A] {
  def before: A
  def after: A
  def tag: ElementaryDiffTag
  def changed: Boolean = tag != Unchanged
  def different: Boolean = tag == Different
}

case class ElementDiff[A](before: A, after: A) extends Diff[A] {
  lazy val tag: ElementaryDiffTag = ElementaryDiffTag(before, after)
}

case class Tuple2Diff[A1, A2](diff1: Diff[A1], diff2: Diff[A2]) extends Diff[(A1, A2)] {
  override def before: (A1, A2) = (diff1.before, diff2.before)
  override def after: (A1, A2) = (diff1.after, diff2.after)

  lazy val tag: ElementaryDiffTag = (diff1.tag, diff2.tag) match {
    case (tag1, tag2) if tag1 == tag2 => tag1
    case (tag1, Unchanged) => tag1
    case (Unchanged, tag2) => tag2
    case _ => Changed
  }
}

case class Tuple3Diff[A1, A2, A3](diff1: Diff[A1], diff2: Diff[A2], diff3: Diff[A3]) extends Diff[(A1, A2, A3)] {
  override def before: (A1, A2, A3) = (diff1.before, diff2.before, diff3.before)
  override def after: (A1, A2, A3) = (diff1.after, diff2.after, diff3.after)

  lazy val tag: ElementaryDiffTag = (diff1.tag, diff2.tag, diff3.tag) match {
    case (tag1, tag2, tag3) if tag1 == tag2 && tag2 == tag3 => tag1
    case (tag1, tag2, Unchanged) if tag1 == tag2 => tag1
    case (tag1, Unchanged, tag3) if tag1 == tag3 => tag1
    case (Unchanged, tag2, tag3) if tag2 == tag3 => tag2
    case (tag1, Unchanged, Unchanged) => tag1
    case (Unchanged, tag2, Unchanged) => tag2
    case (Unchanged, Unchanged, tag3) => tag3
    case _ => Changed
  }
}

case class SetDiff[A](before: Set[A], after: Set[A]) extends Diff[Set[A]] {
  lazy val unchangedElements: Set[A] = before intersect after
  lazy val removedElements: Set[A] = before diff unchangedElements
  lazy val addedElements: Set[A] = after diff unchangedElements

  lazy val elementTags: Map[A, ElementaryDiffTag] = (
      unchangedElements.map(_ -> ElementaryDiffTag.Unchanged).toMap ++
      removedElements.map(_ -> ElementaryDiffTag.Removed).toMap ++
      addedElements.map(_ -> ElementaryDiffTag.Added).toMap
    )
  lazy val tag = ElementaryDiffTag(removedElements.nonEmpty || addedElements.nonEmpty)
}

case class DeepSetDiff[A, B <: Diff[A]](before: Set[A], after: Set[A], elementDiff: (A, A) => B) extends Diff[Set[A]] {
  lazy val unchangedElements: Set[A] = before intersect after
  lazy val removedOrChangedElements: Set[A] = before diff unchangedElements
  lazy val addedOrChangedElements: Set[A] = after diff unchangedElements

  lazy val removedElements: Set[A] = removedOrChangedElements.filter(b => addedOrChangedElements.forall(a => elementDiff(b, a).different))
  lazy val addedElements: Set[A] = addedOrChangedElements.filter(b => removedOrChangedElements.forall(a => elementDiff(b, a).different))

  lazy val changedElementsInBefore: Set[A] = removedOrChangedElements -- removedElements
  lazy val changedElementsInAfter: Set[A] = addedOrChangedElements -- addedElements

  lazy val allChangedElements: Set[B] = changedElementsInBefore.flatMap(
    b => changedElementsInAfter.map(a => elementDiff(b, a)).filterNot(_.different)
  )

  lazy val elementTags: Map[A, ElementaryDiffTag] = (
    unchangedElements.map(_ -> ElementaryDiffTag.Unchanged).toMap ++
      removedElements.map(_ -> ElementaryDiffTag.Removed).toMap ++
      addedElements.map(_ -> ElementaryDiffTag.Added).toMap ++
      changedElementsInBefore.map(_ -> ElementaryDiffTag.Changed).toMap ++
      changedElementsInAfter.map(_ -> ElementaryDiffTag.Changed).toMap
    )
  lazy val tag =
    if(removedElements == before && addedElements == after)
      ElementaryDiffTag.Different
    else
      ElementaryDiffTag(removedElements.nonEmpty || addedElements.nonEmpty || changedElementsInBefore.nonEmpty || changedElementsInAfter.nonEmpty)
}

case class TreePathDiff[A](before: List[A], after: List[A]) extends Diff[List[A]] {
  lazy val changeHead: Option[Int] = before.zip(after).map(p => p._1 != p._2).
    indexWhere(b => b) match {
      case ix if ix < 0 => None
      case ix => Some(ix)
    }
  lazy val tag = ElementaryDiffTag(changeHead.nonEmpty)
}

trait ListDiff[A] extends Diff[List[A]] {
  def before: List[A]
  def after: List[A]
  def paired: List[ElementDiff[Option[A]]]
  lazy val tag = ElementaryDiffTag(before != after)
}

case class SimpleListTopDownDiff[A](before: List[A], after: List[A]) extends ListDiff[A] {
  private def beforeSome: List[Some[A]] = before.map(Some(_))
  private def afterSome: List[Some[A]] = after.map(Some(_))
  lazy val paired: List[ElementDiff[Option[A]]] = beforeSome.zipAll(afterSome, None, None).map(
    p => ElementDiff(p._1, p._2)
  )
}

case class LCSbasedListDiff[A](before: List[A], after: List[A]) extends ListDiff[A] {
  private def lcsSteps(before: List[A], after: List[A], eq: (A, A) => Boolean): List[(Int, Int)] = {
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

  lazy val paired: List[ElementDiff[Option[A]]] = {
    val lcsPairs = lcsSteps(before, after, (b, a) => b == a)
    val lcsPairWindows = ((-1, -1), lcsPairs.head) :: lcsPairs.zip(lcsPairs drop 1)
    lcsPairWindows.flatMap {
      case ((bIxPrec, aIxPrec), (bIx, aIx)) =>
        val paired = SimpleListTopDownDiff(before.slice(bIxPrec + 1, bIx), after.slice(aIxPrec + 1, aIx)).paired
        paired :+ ElementDiff[Option[A]](Some(before(bIx)), Some(after(aIx)))
    }
  }
}