import java.util.*

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
  id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer and runMojangMappedServer tasks for testing
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1" // Generates plugin.yml based on the Gradle config
  id("io.github.goooler.shadow") version "8.1.8"
  id("com.diffplug.spotless") version "6.25.0"
}

group = "de.terranova.nations"
version = "1.0.0-SNAPSHOT"
description = "Nations Plugin tailored & written by & for TerraNova."

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  maven {
    name = "papermc-repo"
    url = uri("https://repo.papermc.io/repository/maven-public/")
  }
  maven {
    name = "citizens-repo"
    url = uri("https://maven.citizensnpcs.co/repo")
  }
  maven {
    name = "WorldGuard"
    url = uri("https://maven.enginehub.org/repo/")
  }
  maven {
    name = "Nexo"
    url = uri("https://repo.nexomc.com/releases")
  }
  exclusiveContent {
    forRepository {
      maven("https://api.modrinth.com/maven")
    }
    filter { includeGroup("maven.modrinth") }
  }
  maven {
    name = "Hikari&Shadow"
    url = uri("https://jitpack.io")
  }

}

// using Mojang Mappins for NMS
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
  implementation("com.zaxxer:HikariCP:6.2.1")
  compileOnly("net.citizensnpcs:citizens-main:2.0.37-SNAPSHOT"){
    exclude(group = "*", module = "*")
  }
  compileOnly("maven.modrinth:pl3xmap:1.21.4-522")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
  compileOnly(fileTree(mapOf("dir" to "jars", "include" to listOf("*.jar"))))
  implementation("io.github.cdimascio:dotenv-java:3.0.0")
  compileOnly("com.nexomc:nexo:1.16.1")
  implementation("org.yaml:snakeyaml:2.2")
  compileOnly("de.mcterranova:terranova-lib:1.0.1")
  implementation ("org.locationtech.jts:jts-core:1.20.0")
}

tasks {
  compileJava {
    // Setgvb gb the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
  shadowJar{
    destinationDirectory.set(file("./testserver/plugins"))
    //relocate("kotlin.", "your.mod.package.kotlin.")
    relocate("org.yaml.snakeyaml", "de.terranova.nations.libs.yaml")
  }

}

bukkitPluginYaml {
  name = "Nations"
  version = project.version.toString()
  main = "${project.group}.NationsPlugin"
  apiVersion = "1.21"
  authors = listOf("gerryxn", "bastizeit")
  prefix = "Nations"
  website = "https://mcterranova.de"
  description = project.description.toString()
  depend = listOf("WorldGuard", "Citizens", "TerranovaLib", "Pl3xMap", "WorldGuardExtraFlags", "Oraxen")

  commands {
    register("terra") {
      description = "The main command for TerraNova Nations."
      usage = "/terra <region|bank> <subcommand>"
      aliases = listOf("t")
      permission = "nations.use"
      permissionMessage = "You do not have permission to use this command."
    }
  }
}

spotless {
  java {
    googleJavaFormat()
    removeUnusedImports()
    target("src/**/*.java")
  }
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
