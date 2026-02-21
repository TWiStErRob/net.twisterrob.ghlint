import net.twisterrob.gradle.settings.enableFeaturePreviewQuietly

// TODEL https://github.com/gradle/gradle/issues/18971
rootProject.name = "net-twisterrob-ghlint"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreviewQuietly("TYPESAFE_PROJECT_ACCESSORS", "Type-safe project accessors")

pluginManagement {
	includeBuild("gradle/plugins")
	repositories {
		gradlePluginPortal()
	}
}

plugins {
	id("net.twisterrob.gradle.plugin.settings") version "0.19"
	id("net.twisterrob.gradle.plugin.nagging") version "0.19"
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		mavenCentral()
		google()
	}
}

includeModule(":ghlint")
includeModule(":ghlint-api")
includeModule(":ghlint-cli")
includeModule(":ghlint-docs")
includeModule(":ghlint-extensions")
includeModule(":ghlint-jsonschema")
includeModule(":ghlint-reporting-sarif")
includeModule(":ghlint-rules")
includeModule(":ghlint-snakeyaml")
includeModule(":ghlint-testing")
includeModule(":test-helpers")
include(":website")

fun includeModule(path: String) {
	include(path)
	val module = project(path)
	module.projectDir = file("modules").resolve(module.projectDir.relativeTo(settings.rootDir))
}
