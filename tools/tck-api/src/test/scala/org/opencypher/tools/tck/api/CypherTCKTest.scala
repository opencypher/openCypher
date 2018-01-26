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
package org.opencypher.tools.tck.api

import org.junit.jupiter.api.Test
import org.opencypher.tools.tck.IdeDetection
import org.opencypher.tools.tck.api.CypherTCK.{featuresPath, parseClasspathFeatures}

class CypherTCKTest {

  /**
    * This test does not work in IntelliJ, as the features are not loaded from a classpath when inside the IDE.
    */
  @Test
  def callParseClasspathFeaturesRepeatedly() {
    if (!IdeDetection.isRunningInsideIntelliJ) {
      parseClasspathFeatures(featuresPath)
      parseClasspathFeatures(featuresPath)
    }

  }

}
