package net.twisterrob.ghlint.build

import net.twisterrob.ghlint.build.dsl.libs

plugins {
	id("org.gradle.java")
}

java {
	sourceCompatibility = libs.versions.java.target.map(JavaVersion::toVersion).get()
	targetCompatibility = libs.versions.java.target.map(JavaVersion::toVersion).get()
}

tasks.withType<JavaCompile>().configureEach {
	options.release = libs.versions.java.target.map(String::toInt)
	options.compilerArgs.add("-Xlint:all")
	options.compilerArgs.add("-Werror")
}
