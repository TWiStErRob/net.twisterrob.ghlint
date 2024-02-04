package net.twisterrob.ghlint.build

import gradle.kotlin.dsl.accessors._285dcef16d8875fee0ec91e18e07daf9.kotlin
import net.twisterrob.ghlint.build.dsl.libs
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.jetbrains.kotlin.jvm")
}

kotlin {
	jvmToolchain(libs.versions.java.toolchain.get().toInt())
}

dependencies {
	implementation(libs.kotlin.stdlib)
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		allWarningsAsErrors.set(true)
		verbose.set(true)
		freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
	}
}
