package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.libs

plugins {
	id("org.jetbrains.kotlin.jvm")
}

kotlin {
	explicitApi()
	jvmToolchain(libs.versions.java.toolchain.map(String::toInt).get())
	compilerOptions {
		allWarningsAsErrors = true
		verbose = true
		freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
		freeCompilerArgs.add("-Xcontext-parameters")
		freeCompilerArgs.add("-Xjvm-default=all")
	}
}

dependencies {
	implementation(libs.kotlin.stdlib)
	plugins.withId("org.gradle.java-test-fixtures") {
		add("testFixturesImplementation", libs.kotlin.stdlib)
	}
}
