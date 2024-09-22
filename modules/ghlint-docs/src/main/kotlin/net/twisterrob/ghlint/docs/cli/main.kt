package net.twisterrob.ghlint.docs.cli

import net.twisterrob.ghlint.test.captureSystemStreams
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

public fun main(vararg args: String) {
	val output = Path.of(args[0])
	val target = output.resolve("cli.md").apply { createParentDirectories() }
	val cliHelp = captureSystemStreams { net.twisterrob.ghlint.cli.main("--help") }
	check(cliHelp.stderr == "") { "Error while running CLI: ${cliHelp.stderr}" }
	target.writeText(generateCliDocs(cliHelp.stdout))
}

private fun generateCliDocs(cliHelp: String): String {
	val cliFile = Resources::class.java.getResourceAsStream("/cli.md") ?: error("Cannot find `cli.md`")
	val cliMarkdown = cliFile.reader().use { it.readText() }
	return cliMarkdown
		.replace("{{ghlint --help}}", cliHelp)
}

/**
 * Placeholder for accessing Java resources.
 */
private object Resources
