import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
	id("org.gradle.java-library")
	id("net.twisterrob.ghlint.build.java")
	id("net.twisterrob.ghlint.build.kotlin")
	id("net.twisterrob.ghlint.build.testing")
	id("net.twisterrob.ghlint.build.detekt")
	id("net.twisterrob.ghlint.build.publishing")
}

kotlin {
	explicitApi = ExplicitApiMode.Strict
}
