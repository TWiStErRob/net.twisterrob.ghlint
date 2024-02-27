package net.twisterrob.ghlint.build.dsl

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputDirectory
import org.gradle.process.CommandLineArgumentProvider

class LazyArgumentProvider(
	@OutputDirectory
	val outputDir: Provider<Directory>
) : CommandLineArgumentProvider {

	override fun asArguments(): Iterable<String> =
		listOf(outputDir.get().asFile.absolutePath)
}
