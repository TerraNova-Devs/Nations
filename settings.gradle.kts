plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Nations"

// Point to your local checkout of the lib
val localLib = file("../TerranovaLib")
if (localLib.isDirectory) {
  includeBuild(localLib) {
    name = "IMPORTED_LIB"
    dependencySubstitution {
      substitute(module("de.mcterranova:terranova-lib"))
        .using(project(":"))
    }
  }
}