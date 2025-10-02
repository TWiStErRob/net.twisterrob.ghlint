package net.twisterrob.ghlint.build.dsl

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputDirectory
import org.gradle.process.CommandLineArgumentProvider

public class LazyArgumentProvider(
	@OutputDirectory
	public val outputDir: Provider<Directory>,
) : CommandLineArgumentProvider {

	override fun asArguments(): Iterable<String> =
		listOf(outputDir.get().asFile.absolutePath)
}
