package net.twisterrob.ghlint.docs.cli

import net.twisterrob.ghlint.cli.CLI
import java.nio.file.Path
import kotlin.io.path.writeText

public fun main(vararg args: String) {
	val output = Path.of(args[0])
	val target = output.resolve("cli.md")
	val cliHelp = CLI().getFormattedHelp() ?: error("No help")
	target.writeText(buildString {
		appendLine("```text")
		appendLine(cliHelp)
		appendLine("```")
	})
}
