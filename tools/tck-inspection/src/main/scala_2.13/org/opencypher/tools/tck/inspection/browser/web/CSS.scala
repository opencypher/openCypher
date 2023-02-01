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
package org.opencypher.tools.tck.inspection.browser.web

import scalatags.Text.all._
import scalatags.stylesheet.CascadingStyleSheet
import scalatags.stylesheet.Cls

object CSS extends CascadingStyleSheet {
  initStyleSheet()

  val pageTitle: Cls = cls(
    fontSize:=2.em,
    fontFamily:="sans-serif",
  )

  val sectionTitle: Cls = cls(
    fontSize:=1.7.em,
    fontFamily:="sans-serif",
  )

  val subSectionTitle: Cls = cls(
    fontSize:=1.4.em,
    fontFamily:="sans-serif",
  )

  val hoverTable: Cls = cls(
    borderCollapse.collapse,
    tr.hover(
      backgroundColor := "#abd9f9",
    ),
    td(
      backgroundClip.`padding-box`,
    )
  )

  val locationLine: Cls = cls(
    backgroundColor:="#abd9f9",
  )

  val tckCollection: Cls = cls(
    backgroundColor:="#993300",
    color.white,
    fontFamily:="sans-serif",
    fontWeight.bold,
  )

  val categoryNameInLocationLine: Cls = cls(
    verticalAlign.baseline,
    fontFamily:="sans-serif",
    backgroundColor:="#abd9f9"
  )

  val categorySepInLocationLine: Cls = cls(
    paddingLeft:=0.5.em,
    paddingRight:=0.5.em,
    color:="#ffffff",
    verticalAlign.baseline,
    fontFamily:="sans-serif",
    fontWeight.bold,
    backgroundColor:="#abd9f9"
  )

  val featureIntroInLocationLine: Cls = cls(
    paddingLeft:=0.5.em,
    paddingRight:=0.5.em,
    color:="#ffffff",
    verticalAlign.baseline,
    fontFamily:="sans-serif",
    fontWeight.bold,
    backgroundColor:="#abd9f9"
  )

  val featureNameInLocationLine: Cls = cls(
    verticalAlign.baseline,
    fontFamily:="sans-serif",
    backgroundColor:="#abd9f9"
  )

  val scenarioLinkInLocationLine: Cls = cls(
    paddingLeft:=1.em,
    verticalAlign.baseline,
    fontFamily:="sans-serif",
    backgroundColor:="#abd9f9"
  )

  val scenarioTitleBox: Cls = cls(
    width:=100.pct,
  )

  val scenarioTitleSmall: Cls = cls(
    fontSize:=1.em,
    fontFamily:="sans-serif",
    fontWeight.bold
  )

  val scenarioTitleBig: Cls = cls(
    fontSize:=1.5.em,
    fontFamily:="sans-serif",
    fontWeight.bold
  )

  val exampleIndex: Cls = cls(
    paddingLeft:=1.em,
    fontSize:=1.5.em,
    fontFamily:="sans-serif",
    fontWeight.bold
  )

  val tagLine: Cls = cls(
    width:=100.pct,
    display.flex,
    flexDirection.row
  )

  val tag: Cls = cls(
    paddingLeft:=0.5.em,
    paddingRight:=0.5.em,
    fontFamily:="sans-serif",
  )

  val tagAdded: Cls = cls(
    paddingLeft:=0.5.em,
    paddingRight:=0.5.em,
    fontFamily:="sans-serif",
    color:="#00dd00",
  )

  val tagRemoved: Cls = cls(
    paddingLeft:=0.5.em,
    paddingRight:=0.5.em,
    fontFamily:="sans-serif",
    color:="#ee0000",
  )

  val fileLocation: Cls = cls(
    fontFamily:="Monospace",
    fontSize:=0.8.em,
    marginBottom:=0.5.ex
  )

  val movedScenariosList: Cls = cls(
    //listStyle:="disc",
  )

  val movedScenariosName: Cls = cls(
    marginBottom:=0.25.ex,
  )

  val movedScenariosMove: Cls = cls(
    display.flex,
    flexDirection.column,
    marginBottom:=0.8.ex,
  )

  val step: Cls = cls(
    width:=100.pct,
    display.flex,
    flexDirection.row,
    marginBottom:=0.25.ex,
  )

  val stepName: Cls = cls(
    backgroundColor:="#abd9f9",
    fontFamily:="sans-serif",
    fontWeight.bold,
    width:=10.em,
  )

  val emptyStepName: Cls = cls(
    backgroundColor:="#abd9f9",
    fontFamily:="sans-serif",
    fontWeight.bold,
    width:=100.pct,
  )

  val stepContent: Cls = cls(
    marginLeft:=0.5.em,
  )

  val scenarioDiffLine: Cls = cls(
    display.flex,
    flexDirection.row,
    marginBottom:=0.25.ex,
  )

  val scenarioDiffBefore: Cls = cls(
    flex:=1,
  )

  val scenarioDiffIndicatorChanged: Cls = cls(
    width:=15.px,
    backgroundColor:="#ee0000",
    fontSize:=1.5.em,
    fontWeight.bold,
    textAlign.center,
    marginRight:=0.25.ex,
    marginLeft:=0.25.ex,
    marginBottom:=0.25.ex,
  )

  val scenarioDiffIndicatorUnchanged: Cls = cls(
    width:=15.px,
    backgroundColor:="#88ee88",
    marginRight:=0.25.ex,
    marginLeft:=0.25.ex,
    marginBottom:=0.25.ex,
  )

  val scenarioDiffAfter: Cls = cls(
    flex:=1,
  )
}