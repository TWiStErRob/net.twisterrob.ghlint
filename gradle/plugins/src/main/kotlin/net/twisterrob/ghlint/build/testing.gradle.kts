package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.isCI
import net.twisterrob.ghlint.build.dsl.libs
import java.util.Properties

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
			systemProperties(
				rootProject.file("config/junit/junit-platform.properties")
					.reader()
					.use { Properties().apply { load(it) } }
					.mapKeys { (k, _) -> k.toString() }
			)
			if (javaVersion.isCompatibleWith(JavaVersion.VERSION_21)) {
				jvmArgs("-XX:+EnableDynamicAgentLoading")
			}
		}
	}
}
