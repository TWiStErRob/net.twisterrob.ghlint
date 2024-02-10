import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
	id("org.gradle.application")
	id("net.twisterrob.ghlint.build.java")
	id("net.twisterrob.ghlint.build.kotlin")
	id("net.twisterrob.ghlint.build.testing")
	id("net.twisterrob.ghlint.build.detekt")
}

kotlin {
	explicitApi = ExplicitApiMode.Strict
}
