package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.libs
import org.gradle.kotlin.dsl.withType

plugins {
	id("org.gradle.java")
}

tasks.withType<JavaCompile>().configureEach {
	sourceCompatibility = libs.versions.java.source.get()
	targetCompatibility = libs.versions.java.target.get()
}
