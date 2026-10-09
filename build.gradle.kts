plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
  id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer and runMojangMappedServer tasks for testing
  id("com.gradleup.shadow") version "9.0.0"
  id("com.diffplug.spotless") version "6.25.0"
}

group = "de.terranova.nations"
version = "1.0.0-SNAPSHOT"
description = "Nations Plugin tailored & written by & for TerraNova."

val minecraftVersion = "26.3"
val nexoVersion = "1.27.0"
val worldGuardVersion = "7.0.18"
// HeroicMap-API über JitPack, nur zum Übersetzen; das Jar von HeroicMap bringt die Klassen mit.
// = v0.3.1 (Release-Commit auf main; JitPack baut Tags derzeit nicht)
val heroicMapApi = "com.github.VonNekyia:heroic-map-renderer-plugin:9f5a87a857"

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
  toolchain.languageVersion = JavaLanguageVersion.of(25)
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
  paperweight.paperDevBundle("$minecraftVersion.build.+")
  implementation("com.zaxxer:HikariCP:7.0.2")
  compileOnly("net.citizensnpcs:citizens-main:2.0.41-SNAPSHOT"){
    exclude(group = "*", module = "*")
  }
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:$worldGuardVersion") {
    exclude(group = "com.google.code.gson", module = "gson")
    exclude(group = "com.google.guava", module = "guava")
  }
  compileOnly(fileTree(mapOf("dir" to "jars", "include" to listOf("*.jar"))))
  implementation("io.github.cdimascio:dotenv-java:3.2.0")
  compileOnly("com.nexomc:nexo:$nexoVersion")
  implementation("org.yaml:snakeyaml:2.6")
  compileOnly("de.mcterranova:terranova-lib:1.0.1")
  implementation ("org.locationtech.jts:jts-core:1.20.0")
  compileOnly(heroicMapApi)
  testImplementation(heroicMapApi)
  testImplementation(platform("org.junit:junit-bom:6.1.3"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
  compileJava {
    // Setgvb gb the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release = 25
  }
  test {
    useJUnitPlatform()
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

spotless {
  java {
    googleJavaFormat()
    removeUnusedImports()
    target("src/**/*.java")
  }
}

tasks.processResources {
  val props = mapOf(
    "version" to version,
    "minecraftVersion" to minecraftVersion,
  )
  filteringCharset = "UTF-8"
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  inputs.properties(props)
  filteringCharset = "UTF-8"
  filesMatching(listOf("plugin.yml", "paper-plugin-x.yml")) {
    expand(props)
  }
}
