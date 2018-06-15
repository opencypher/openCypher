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
// Generated from FeatureResults.g4 by ANTLR 4.7
package org.opencypher.tools.tck.parsing.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FeatureResultsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FeatureResultsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(FeatureResultsParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#node}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNode(FeatureResultsParser.NodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#nodeDesc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodeDesc(FeatureResultsParser.NodeDescContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#relationship}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationship(FeatureResultsParser.RelationshipContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#relationshipDesc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationshipDesc(FeatureResultsParser.RelationshipDescContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath(FeatureResultsParser.PathContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#pathBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathBody(FeatureResultsParser.PathBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#pathLink}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathLink(FeatureResultsParser.PathLinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#forwardsRelationship}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForwardsRelationship(FeatureResultsParser.ForwardsRelationshipContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#backwardsRelationship}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackwardsRelationship(FeatureResultsParser.BackwardsRelationshipContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#integer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger(FeatureResultsParser.IntegerContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#floatingPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloatingPoint(FeatureResultsParser.FloatingPointContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#bool}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool(FeatureResultsParser.BoolContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#nullValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullValue(FeatureResultsParser.NullValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(FeatureResultsParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#listContents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListContents(FeatureResultsParser.ListContentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#listElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListElement(FeatureResultsParser.ListElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#map}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap(FeatureResultsParser.MapContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#propertyMap}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyMap(FeatureResultsParser.PropertyMapContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#mapContents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapContents(FeatureResultsParser.MapContentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#keyValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyValuePair(FeatureResultsParser.KeyValuePairContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#propertyKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyKey(FeatureResultsParser.PropertyKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#propertyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyValue(FeatureResultsParser.PropertyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#relationshipType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationshipType(FeatureResultsParser.RelationshipTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#relationshipTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationshipTypeName(FeatureResultsParser.RelationshipTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel(FeatureResultsParser.LabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#labelName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabelName(FeatureResultsParser.LabelNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link FeatureResultsParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(FeatureResultsParser.StringContext ctx);
}