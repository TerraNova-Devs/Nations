plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Nations"

// Point to your local checkout of the lib
val localLib = file("../TerranovaLib")
val requiredLocalLibBranch = "DEV-26.3"

fun currentGitBranch(repoDir: File): String? =
  runCatching {
    val process = ProcessBuilder(
      "git",
      "-C",
      repoDir.absolutePath,
      "rev-parse",
      "--abbrev-ref",
      "HEAD",
    )
      .redirectErrorStream(true)
      .start()
    val output = process.inputStream.bufferedReader().readText().trim()
    if (process.waitFor() == 0) output else null
  }.getOrNull()

if (localLib.isDirectory) {
  val localLibBranch = currentGitBranch(localLib)
  if (localLibBranch != requiredLocalLibBranch) {
    throw GradleException(
      "TerranovaLib must be checked out on $requiredLocalLibBranch, but was ${localLibBranch ?: "unknown"}."
    )
  }

  includeBuild(localLib) {
    name = "IMPORTED_LIB"
    dependencySubstitution {
      substitute(module("de.mcterranova:terranova-lib"))
        .using(project(":"))
    }
  }
}
