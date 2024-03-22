package net.twisterrob.ghlint.docs.cli

import net.twisterrob.ghlint.cli.CLI
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

public fun main(vararg args: String) {
	val output = Path.of(args[0])
	val target = output.resolve("cli.md").apply { createParentDirectories() }
	val cliHelp = CLI().getFormattedHelp() ?: error("No help")
	target.writeText(buildString {
		appendLine("# Command Line Interface")
		appendLine(
			"""
				GH-Lint comes as an executable JAR file that can be run from the command line.
			""".trimIndent()
		)

		appendLine("## Installation")
		appendLine("### GitHub Releases")
		appendLine(
			"""
				Download the latest "CLI executable" from the [GitHub Releases][releases] page.
				
				[releases]: https://github.com/TWiStErRob/net.twisterrob.ghlint/releases
			""".trimIndent()
		)

		appendLine("### GitHub pre-releases")
		appendLine(
			"""
				https://github.com/TWiStErRob/net.twisterrob.ghlint/actions/workflows/ci.yml
			""".trimIndent()
		)

		appendLine("## Execution")
		appendLine(
			"""
				Regardless of where you downloaded the JAR file, you can run it from the command line.
				
				This JAR file can be run with Java on all operating systems:
				```shell
				java -jar ghlint.jar
				```
				
				On Unix systems (Linux, Mac), you can make the JAR executable and run it directly:
				```shell
				mv ghlint.jar ghlint
				chmod +x ghlint
				./ghlint --version
				```
				
				If you put the executable on the `PATH`, you can run it from anywhere:
				```shell
				ghlint my-workflow.yml
				```
			""".trimIndent()
		)
		
		appendLine("## Usage")
		appendLine("```text")
		appendLine(cliHelp)
		appendLine("```")
	})
}
