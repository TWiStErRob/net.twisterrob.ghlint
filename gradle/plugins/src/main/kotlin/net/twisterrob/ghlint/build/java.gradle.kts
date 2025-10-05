package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.libs

plugins {
	id("org.gradle.java")
}

tasks.withType<JavaCompile>().configureEach {
	sourceCompatibility = libs.versions.java.target.get()
	targetCompatibility = libs.versions.java.target.get()
	options.compilerArgs.add("-Xlint:all")
	options.compilerArgs.add("-Werror")
}
