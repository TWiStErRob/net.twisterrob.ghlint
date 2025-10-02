package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.jetbrains.kotlin.jvm")
}

kotlin {
	jvmToolchain(libs.versions.java.toolchain.get().toInt())
	compilerOptions {
		jvmTarget = libs.versions.java.target.map(JvmTarget::fromTarget)
	}
}

dependencies {
	implementation(libs.kotlin.stdlib)
	plugins.withId("org.gradle.java-test-fixtures") {
		add("testFixturesImplementation", libs.kotlin.stdlib)
	}
}

// TODO kotlin.target.compilerOptions { ... }
tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		allWarningsAsErrors.set(true)
		verbose.set(true)
		freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
		freeCompilerArgs.add("-Xcontext-parameters")
		freeCompilerArgs.add("-Xjvm-default=all")
	}
}
