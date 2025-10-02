plugins {
	`kotlin-dsl`
	id("org.gradle.idea")
}

dependencies {
	api(libs.plugins.kotlin.asDependency())
	api(libs.plugins.detekt.asDependency())

	// TODEL https://github.com/gradle/gradle/issues/15383
	implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}

kotlin {
	explicitApi()
}

fun Provider<PluginDependency>.asDependency(): Provider<String> =
	this.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

idea {
	module {
		excludeDirs = excludeDirs + listOf(
			//file("build/generated-sources/kotlin-dsl-accessors/kotlin/gradle/kotlin/dsl/accessors"),
			file("build/generated-sources/kotlin-dsl-external-plugin-spec-builders/kotlin/gradle/kotlin/dsl/plugins"),
			file("build/generated-sources/kotlin-dsl-plugins/kotlin"),
		)
	}
}
