package org.opencypher.tools.tck
import org.opencypher.tools.tck.api.{CypherTCK, Scenario}

/**
  * Detect if a program is run from inside of an IDE or on Mac OS X. This is important for tests that try to access files from
  * the classpath/filesystem, because running from inside of the IDE behaves differently from running with Maven.
  */
object RuntimeDetection {

  def isRunningInsideIntelliJ: Boolean = System.getProperty("java.class.path").contains("idea_rt.jar")
  def isRunningOnMacOS: Boolean = System.getProperty("os.name").contains("Mac OS X")

  /*
   * Adaptation for unit testing the API;
   * when consuming this artifact or running mvn verify we need to resolve via classpath
   * when running inside IntelliJ or on Mac OS X we need to resolve via filesystem
   *
   * this is annoying like this because we need to initialise a new filesystem in the JVM when loading from the JAR
   */
  def allTckScenarios: Seq[Scenario] =
    if (isRunningInsideIntelliJ || isRunningOnMacOS)
      CypherTCK.allTckScenariosFromFilesystem
    else
      CypherTCK.allTckScenarios
}
