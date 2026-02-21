package net.twisterrob.ghlint.build

import dev.detekt.gradle.Detekt
import net.twisterrob.ghlint.build.dsl.isCI
import net.twisterrob.ghlint.build.dsl.libs

plugins {
	id("dev.detekt")
}

detekt {
	ignoreFailures = isCI.get()
	allRules = true
	basePath = rootProject.projectDir

	parallel = true
	config.from(rootProject.file("config/detekt/detekt.yml"))

	tasks.withType<Detekt>().configureEach {
		// Target version of the generated JVM bytecode. It is used for type resolution.
		jvmTarget = libs.versions.java.target.get()
		reports {
			html.required = true // human
			checkstyle.required = true // checkstyle
			markdown.required = true // console
			// https://sarifweb.azurewebsites.net
			sarif.required = true // Github Code Scanning
		}
	}
}

dependencies {
	detektPlugins(libs.detekt.rules.libraries)
}
