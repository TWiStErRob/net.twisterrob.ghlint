package net.twisterrob.ghlint.build.dsl

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register

fun Project.consumeGeneratedFolder(
	fromProject: ProjectDependency,
	fromConfiguration: String,
	intoFolder: String,
): TaskProvider<out Task> {
	val configurationName = "generatedFilesFrom${fromConfiguration.capitalized()}"
	val configurationDep = configurations.dependencyScope("${configurationName}Dependencies").get()
	val configuration = configurations.resolvable(configurationName) {
		extendsFrom(configurationDep)
	}

	dependencies {
		configurationDep(fromProject) {
			targetConfiguration = fromConfiguration
		}
	}

	return tasks.register<Copy>("copy${fromConfiguration.capitalized()}") {
		group = "Build"
		description = "Copy generated files from ${fromProject.name}:${fromConfiguration} into ${intoFolder}"
		from(configuration)
		into(layout.projectDirectory.dir(intoFolder))
	}
}
