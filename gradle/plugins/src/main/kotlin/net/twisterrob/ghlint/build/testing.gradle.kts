package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.isCI
import net.twisterrob.ghlint.build.dsl.libs

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
			ignoreFailures = isCI.get()
		}
	}
}
