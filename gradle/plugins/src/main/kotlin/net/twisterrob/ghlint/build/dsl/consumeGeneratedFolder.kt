package net.twisterrob.ghlint.build.dsl

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register

public fun Project.consumeGeneratedFolder(
	fromProject: ProjectDependency,
	fromConfiguration: String,
	intoFolder: String,
): TaskProvider<out Task> {
	val configurationName = "generatedFilesFrom${fromConfiguration.replaceFirstChar(Char::uppercaseChar)}"
	val configuration = configurations.dependencyScope("${configurationName}Dependencies").get()
	val resolvableConfiguration = configurations.resolvable(configurationName) {
		extendsFrom(configuration)
	}

	dependencies {
		configuration(fromProject) {
			targetConfiguration = fromConfiguration
		}
	}

	return tasks.register<Copy>("copy${fromConfiguration.replaceFirstChar(Char::uppercaseChar)}") {
		group = "Build"
		description = "Copy generated files from ${fromProject.name}:${fromConfiguration} into ${intoFolder}"
		from(resolvableConfiguration)
		into(layout.projectDirectory.dir(intoFolder))
	}
}
