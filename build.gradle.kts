import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.7.1"
  id("xyz.jpenilla.run-paper") version "2.3.0" // Adds runServer and runMojangMappedServer tasks for testing
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1" // Generates plugin.yml based on the Gradle config
  id("io.github.goooler.shadow") version "8.1.8"
}

group = "de.terranova.nations"
version = "1.0.0-SNAPSHOT"
description = "Test plugin for paperweight-userdev"

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {

  maven {
    name = "citizens-repo"
    url = uri("https://maven.citizensnpcs.co/repo")
  }

  maven {
    name = "DecentHolograms&GUIs&Hikari&Shadow"
    url = uri("https://jitpack.io")
  }

  maven {
    name = "WorldGuard"
    url = uri("https://maven.enginehub.org/repo/")
  }
  exclusiveContent {
    forRepository {
      maven("https://api.modrinth.com/maven")
    }
    filter { includeGroup("maven.modrinth") }
  }


}

// 1)
// For >=1.20.5 when you don't care about supporting spigot
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

// 2)
// For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5
// Configure reobfJar to run when invoking the build task
/*
tasks.assemble {
  dependsOn(tasks.reobfJar)
}
 */

dependencies {
  paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
  implementation("com.zaxxer:HikariCP:5.0.1")
  compileOnly("net.citizensnpcs:citizens-main:2.0.35-SNAPSHOT"){
    exclude(group = "*", module = "*")
  }
  //compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.9")
  compileOnly("maven.modrinth:pl3xmap:1.21-500")
  implementation("com.github.hamza-cskn.obliviate-invs:core:4.3.0")
  implementation("com.github.hamza-cskn.obliviate-invs:pagination:4.3.0")
  implementation("com.github.hamza-cskn.obliviate-invs:configurablegui:4.3.0")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.11-SNAPSHOT")
  compileOnly(fileTree(mapOf("dir" to "jars", "include" to listOf("*.jar"))))
  implementation("io.github.cdimascio:dotenv-java:3.0.0")
}



tasks {
  compileJava {
    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
  shadowJar{
    destinationDirectory.set(file("./testserver/plugins"))
    //relocate("kotlin.", "your.mod.package.kotlin.")
  }

}

bukkitPluginYaml {
  name = "Nations"
  version = "1.0.0-SNAPSHOT"
  main = "io.papermc.paperweight.testplugin.TestPlugin"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("gerryxn")
  apiVersion = "1.21"
  prefix = "TerraNova"
  website = "mcterranova.de"
  description = "Nations Plugin tailored & written by & for TerraNova."
  //commands:
  //settle:
  //description: Command facilitates settlements creation.
  //depend: [WorldGuard, Citizens]
}

tasks.processResources {
  val props = mapOf("version" to version)
  filteringCharset = "UTF-8"
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  inputs.properties(props)
  filteringCharset = "UTF-8"
  filesMatching("plugin.yml") {
    expand(props)
  }
}
