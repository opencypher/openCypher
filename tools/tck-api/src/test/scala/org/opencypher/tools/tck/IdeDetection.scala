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
package org.opencypher.tools.tck

/**
  * Detect if a program is run from inside of an IDE. This is important for tests that try to access files from
  * the classpath/filesystem, because running from inside of the IDE behaves differently from running with Maven.
  */
object IdeDetection {

  def isRunningInsideIntelliJ = System.getProperty("java.class.path").contains("idea_rt.jar")

}
