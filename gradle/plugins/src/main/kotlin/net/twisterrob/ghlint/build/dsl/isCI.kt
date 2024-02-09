package net.twisterrob.ghlint.build.dsl

import org.gradle.api.Project
import org.gradle.api.provider.Provider

val Project.isCI: Provider<Boolean>
	get() = this
		.providers
		.environmentVariable("CI")
		.map(String::toBooleanStrict)
		.orElse(false)
