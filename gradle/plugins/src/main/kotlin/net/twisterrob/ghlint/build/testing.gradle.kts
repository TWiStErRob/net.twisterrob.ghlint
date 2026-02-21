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
			// Hide (https://stackoverflow.com/a/79098701/253468)
			// > Java HotSpot(TM) 64-Bit Server VM warning:
			// > Sharing is only supported for boot loader classes because bootstrap classpath has been appended
			jvmArgs("-Xshare:off")
			if (javaVersion.isCompatibleWith(JavaVersion.VERSION_21)) {
				// https://github.com/mockito/mockito/issues/3037#issuecomment-1588199599
				jvmArgs("-XX:+EnableDynamicAgentLoading")
			}
		}
	}
}
