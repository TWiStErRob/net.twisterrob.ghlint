package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("org.jetbrains.kotlin.jvm")
}

kotlin {
	explicitApi()
	jvmToolchain(libs.versions.java.toolchain.map(String::toInt).get())
	compilerOptions {
		jvmTarget = libs.versions.java.target.map(JvmTarget::fromTarget)
		freeCompilerArgs.add(jvmTarget.map { "-Xjdk-release=${it.target}" })
		jvmDefault = JvmDefaultMode.ENABLE
		allWarningsAsErrors = true
		extraWarnings = true
		progressiveMode = true
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
