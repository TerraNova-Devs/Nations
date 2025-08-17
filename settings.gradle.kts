plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Nations"

// Point to your local checkout of the lib
val localLib = file("../TerranovaLib")
if (localLib.isDirectory) {
  includeBuild(localLib) {
    dependencySubstitution {
      // substitute published module with the local project (root project of the lib)
      substitute(module("de.mcterranova:terranova-lib"))
        .using(project(":"))
    }
  }
}