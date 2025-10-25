package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.isCI
import net.twisterrob.ghlint.build.dsl.libs
import java.util.Properties

plugins {
	id("org.gradle.java")
	id("org.gradle.java-test-fixtures")
}

@Suppress("UnstableApiUsage")
testing.suites.withType<JvmTestSuite>().configureEach {
	useJUnitJupiter(libs.versions.junit.jupiter)

	dependencies {
		implementation(project(":test-helpers"))
	}

	targets.configureEach {
		testTask.configure {
			javaLauncher = javaToolchains.launcherFor {
				languageVersion = libs.versions.java.toolchainTest.map(JavaLanguageVersion::of)
			}
			ignoreFailures = isCI.get()
			systemProperties(
				rootProject.file("config/junit/junit-platform.properties")
					.reader()
					.use { Properties().apply { load(it) } }
					.mapKeys { (k, _) -> k.toString() }
			)
			if (javaVersion.isCompatibleWith(JavaVersion.VERSION_21)) {
				// https://github.com/mockito/mockito/issues/3037#issuecomment-1588199599
				jvmArgs("-XX:+EnableDynamicAgentLoading")
			}
		}
	}
}
