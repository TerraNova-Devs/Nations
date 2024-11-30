import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import java.util.*

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.7.1"
  id("xyz.jpenilla.run-paper") version "2.3.0" // Adds runServer and runMojangMappedServer tasks for testing
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1" // Generates plugin.yml based on the Gradle config
  id("io.github.goooler.shadow") version "8.1.8"
}

group = "de.terranova.nations"
version = "1.0.0-SNAPSHOT"
description = "Nations Plugin tailored & written by & for TerraNova."

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

val envProperties = Properties()
val envPropertiesFile = file("private.env")
envPropertiesFile.bufferedReader(Charsets.UTF_8).use { reader ->
  envProperties.load(reader)
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
  maven {
    name = "Oraxen"
    url = uri("https://repo.oraxen.com/releases")
  }
  exclusiveContent {
    forRepository {
      maven("https://api.modrinth.com/maven")
    }
    filter { includeGroup("maven.modrinth") }
  }
  maven {
    name = "github"
    url = uri("https://maven.pkg.github.com/TerraNova-Devs/TerranovaLib")
    credentials {
      username = project.findProperty("githubUser") as String?
      password = project.findProperty("githubToken") as String?
    }
  }

}

// using Mojang Mappins for NMS
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
  paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
  implementation("com.zaxxer:HikariCP:5.1.0")
  compileOnly("net.citizensnpcs:citizens-main:2.0.35-SNAPSHOT"){
    exclude(group = "*", module = "*")
  }
  compileOnly("maven.modrinth:pl3xmap:1.21-500")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12")
  compileOnly(fileTree(mapOf("dir" to "jars", "include" to listOf("*.jar"))))
  implementation("io.github.cdimascio:dotenv-java:3.0.0")
  compileOnly("io.th0rgal:oraxen:1.184.0")
  implementation("de.mcterranova:terranova-lib:0.7.5")
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
