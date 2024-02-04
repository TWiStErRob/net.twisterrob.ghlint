package net.twisterrob.ghlint.build

import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.javaToolchains
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.testing
import net.twisterrob.ghlint.build.dsl.libs
import org.gradle.kotlin.dsl.withType

plugins {
	id("org.gradle.java")
}

@Suppress("UnstableApiUsage")
testing.suites.withType<JvmTestSuite>().configureEach {
	useJUnitJupiter(libs.versions.junit.jupiter)

	dependencies {
		implementation(project(":test-helpers"))
	}

	targets.configureEach {
		testTask.configure {
			javaLauncher.set(javaToolchains.launcherFor {
				languageVersion.set(JavaLanguageVersion.of(libs.versions.java.toolchainTest.get()))
			})
		}
	}
}
