/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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
 */
package org.opencypher.tools.tck.values

import org.opencypher.tools.tck.parsing.generated.{FeatureResultsBaseVisitor, FeatureResultsParser}

import scala.collection.JavaConverters._
import scala.util.Try

case class CypherValueParseException(msg: String) extends Exception(msg)

case class CypherValueVisitor(orderedLists: Boolean) extends FeatureResultsBaseVisitor[CypherValue] {

  import FeatureResultsParser._

  override def visitValue(ctx: ValueContext): CypherValue = {
    Option(visitChildren(ctx)).getOrElse(CypherNull)
  }

  override def visitString(ctx: StringContext) = {
    val s = Try(ctx.STRING_LITERAL.getText).getOrElse("''")
    CypherString(s.dropRight(1).drop(1))
  }

  override def visitNode(ctx: NodeContext) = {
    Try(visitNodeDesc(ctx.nodeDesc))
      .getOrElse(CypherNode(Set.empty, CypherPropertyMap()))
  }

  override def visitNodeDesc(ctx: NodeDescContext) = {
    val labels =
      Try(ctx.label.asScala.map(_.labelName.getText).toSet).getOrElse(Set.empty)
    val properties: CypherPropertyMap = Try(visitPropertyMap(ctx.propertyMap))
      .getOrElse(CypherPropertyMap())
    CypherNode(labels, properties)
  }

  override def visitRelationship(ctx: RelationshipContext) = {
    visitRelationshipDesc(ctx.relationshipDesc)
  }

  override def visitRelationshipDesc(ctx: RelationshipDescContext) = {
    val relType = ctx.relationshipType.relationshipTypeName.getText
    val properties: CypherPropertyMap =
      Try(visitPropertyMap(ctx.propertyMap))
        .getOrElse(CypherPropertyMap())
    CypherRelationship(relType, properties)
  }

  override def visitPath(ctx: PathContext) = {
    visitPathBody(ctx.pathBody)
  }

  override def visitPathBody(ctx: PathBodyContext) = {
    val initialNode = visitNodeDesc(ctx.nodeDesc)
    val (reversedPath, _) = ctx.pathLink.asScala.toList.foldLeft((List.empty[Connection], initialNode)) {
      case ((pathSoFar, currentNode), nextLink) =>
        val forward = Try(visitRelationshipDesc(nextLink.forwardsRelationship.relationshipDesc)).toOption
        val backward = Try(visitRelationshipDesc(nextLink.backwardsRelationship.relationshipDesc)).toOption
        val otherNode = visitNodeDesc(nextLink.nodeDesc)
        val updatedConnections = if (forward.isDefined) {
          Forward(forward.get, otherNode) :: pathSoFar
        } else {
          Backward(backward.get, otherNode) :: pathSoFar
        }
        (updatedConnections, otherNode)
    }
    CypherPath(initialNode, reversedPath.reverse)
  }

  override def visitInteger(ctx: IntegerContext): CypherInteger = {
    CypherInteger(ctx.INTEGER_LITERAL.getText.toLong)
  }

  override def visitFloatingPoint(ctx: FloatingPointContext) = {
    val d = Option(ctx.INFINITY) match {
      case Some(i) =>
        if (i.getText.startsWith("-")) Double.NegativeInfinity
        else Double.PositiveInfinity
      case None => ctx.FLOAT_LITERAL.getText.toDouble
    }
    CypherFloat(d)
  }

  override def visitBool(ctx: BoolContext) = {
    CypherBoolean(ctx.getText.toBoolean)
  }

  override def visitNullValue(ctx: NullValueContext) = {
    CypherNull
  }

  override def visitList(ctx: ListContext): CypherValue = {
    val contents = Try(
      ctx.listContents.listElement.asScala
        .map(element => visitValue(element.value))
        .toList).toOption.getOrElse(List.empty)
    if (orderedLists) {
      CypherOrderedList(contents)
    } else {
      CypherUnorderedList(contents.sorted(CypherValue.ordering))
    }
  }

  override def visitMap(ctx: MapContext): CypherValue = {
    visitPropertyMap(ctx.propertyMap)
  }

  override def visitPropertyMap(ctx: PropertyMapContext) = {
    val keyValuePairs =
      Try(ctx.mapContents.keyValuePair.asScala.toList.map(visitKeyValuePair(_))).getOrElse(List.empty)
    CypherPropertyMap(
      keyValuePairs.map(kvPair => kvPair.key -> kvPair.value).toMap)
  }

  override def visitKeyValuePair(ctx: KeyValuePairContext) = {
    CypherProperty(ctx.propertyKey.getText, visitValue(ctx.propertyValue.value))
  }

}
