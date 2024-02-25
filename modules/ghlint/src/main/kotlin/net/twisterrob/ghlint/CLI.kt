package net.twisterrob.ghlint

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path

internal class CLI : CliktCommand(), Configuration {

	override val files: List<Path> by argument()
		.path(mustExist = true, canBeDir = false)
		.multiple(required = true)
		.help("File to scan.")

	override val verbose: Boolean by option("-v", "--verbose")
		.boolean()
		.default(false)
		.help("Prints more information.")

	override val reportConsole: Boolean by option("--console")
		.boolean()
		.default(true)
		.help("Output to console.")

	override val reportSarif: Path? by option("--sarif")
		.path(canBeDir = false, mustBeWritable = true)
		.help("Output a SARIF file.")

	override val reportGitHubCommands: Boolean by option("--ghcommands")
		.boolean()
		.default(false)
		.help("Output GitHub Commands (warnings).")

	override fun run() {
		Main().run(this)
	}
}
