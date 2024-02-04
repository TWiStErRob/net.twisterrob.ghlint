plugins {
	`kotlin-dsl`
}

dependencies {
	api(libs.plugins.kotlin.asDependency())
	api(libs.plugins.detekt.asDependency())

	// TODEL https://github.com/gradle/gradle/issues/15383
	implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}

fun Provider<PluginDependency>.asDependency(): Provider<String> =
	this.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
