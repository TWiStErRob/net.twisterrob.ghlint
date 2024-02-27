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
		appendLine("```text")
		appendLine(cliHelp)
		appendLine("```")
	})
}
