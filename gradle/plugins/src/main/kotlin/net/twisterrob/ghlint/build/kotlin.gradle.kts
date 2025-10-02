package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
	id("org.jetbrains.kotlin.jvm")
}

kotlin {
	explicitApi()
	jvmToolchain(libs.versions.java.toolchain.map(String::toInt).get())
	compilerOptions {
		jvmDefault = JvmDefaultMode.ENABLE
		allWarningsAsErrors = true
		extraWarnings = true
		freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
		freeCompilerArgs.add("-Xcontext-parameters")
		// Kotlin 2.1: Make it easier to suppress warnings locally in the code by providing a name in logs.
		// See https://youtrack.jetbrains.com/issue/KT-8087
		freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
	}
}

dependencies {
	implementation(libs.kotlin.stdlib)
	plugins.withId("org.gradle.java-test-fixtures") {
		add("testFixturesImplementation", libs.kotlin.stdlib)
	}
}
