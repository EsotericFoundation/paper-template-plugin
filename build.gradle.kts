import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
  java
  `java-library`

  `maven-publish`

  id("io.papermc.paperweight.userdev") version "1.7.3"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
  id("xyz.jpenilla.run-paper") version "2.3.1"
  id("io.github.goooler.shadow") version "8.1.8"
}

val groupStringSeparator = "."
val kebabcaseStringSeparator = "-"
val snakecaseStringSeparator = "_"

val startPath = "src${File.separator}main${File.separator}java"

fun capitalizeFirstLetter(string: String): String {
  return string.first().uppercase() + string.drop(1)
}

fun titlecase(kebabcaseString: String): String {
  return kebabcaseString.replace(kebabcaseStringSeparator, " ").split(" ").joinToString(" ") { string -> capitalizeFirstLetter(string) }
}

fun kebabcase(normalString: String): String {
  return normalString.lowercase().replace(" ", kebabcaseStringSeparator)
}

fun snakecase(string: String): String {
  return string.lowercase().replace(Regex("$kebabcaseStringSeparator| "), snakecaseStringSeparator)
}

fun pascalcase(string: String): String {
  return string.split(Regex("$kebabcaseStringSeparator| "))
    .joinToString("") { capitalizeFirstLetter(it) }
}

fun groupStringToPath(groupString: String): String {
  return groupString.replace(groupStringSeparator, File.separator)
}

fun replaceStringInFile(filePath: String, stringToReplace: String, replacementString: String) {
  val file = File(filePath)

  val content = file.readText()
  val updatedContent = content.replace(stringToReplace, replacementString)

  file.writeText(updatedContent)
}

fun replaceStringInDirectoryFiles(directory: File, stringToReplace: String, replacementString: String) {
  directory.walkTopDown().filter { it.isFile }.forEach { file ->
    replaceStringInFile(file.path, stringToReplace, replacementString)
  }
}

description = "A template repository for easily developing Minecraft Paper plugins."

val mainProjectAuthorName = "Esoteric Organisation"
val simplifiedMainProjectAuthorName = mainProjectAuthorName.split(" ").first()
val snakecaseMainProjectAuthorName = snakecase(mainProjectAuthorName)

val projectAuthors = listOfNotNull(mainProjectAuthorName, "Esoteric Enderman")

val topLevelDomain = "org"

val projectNameString = rootProject.name
val snakecaseProjectNameString = snakecase(projectNameString)

group = "$topLevelDomain$groupStringSeparator${simplifiedMainProjectAuthorName.lowercase()}"
version = "0.0.1"

val buildDirectoryString = layout.buildDirectory.toString()

val projectGroupString = group.toString()
val projectVersionString = version.toString()

val javaVersion = 21
val javaVersionEnumMember = JavaVersion.valueOf("VERSION_$javaVersion")

val paperApiMinecraftVersion = "1.21.1"
val paperApiVersion = "$paperApiMinecraftVersion-R0.1-SNAPSHOT"

java {
  sourceCompatibility = javaVersionEnumMember
  targetCompatibility = javaVersionEnumMember

  toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

repositories {
  mavenCentral()
  maven("https://www.jitpack.io")
}

dependencies {
  paperweight.paperDevBundle(paperApiVersion)

  implementation("dev.jorel", "commandapi-bukkit-shade-mojang-mapped", "9.5.3")
  implementation("net.lingala.zip4j", "zip4j", "2.11.5")
  implementation("com.github.EsotericFoundation:utility.kt:0.1.0")
  implementation("com.github.EsotericFoundation:plugin-library:0.1.0")
}

tasks {
  build {
    dependsOn(shadowJar)
  }

  compileJava {
    options.release.set(javaVersion)
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}

val projectName = providers.gradleProperty("projectName").get()
val pascalCaseProjectName = pascalcase(projectName)

bukkitPluginYaml {
  name = pascalCaseProjectName.replace(Regex(" Plugin$"), "")
  description = project.description

  authors = projectAuthors

  setVersion(project.version)

  apiVersion = paperApiMinecraftVersion
  main = "${project.group}.minecraft.plugins.template.${pascalCaseProjectName}"

  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      groupId = projectGroupString
      artifactId = projectNameString
      version = projectVersionString
    }
  }
}

tasks.named("publishMavenJavaPublicationToMavenLocal") {
  dependsOn(tasks.named("build"))
}
