package net.twisterrob.ghlint

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path

internal class CLI : CliktCommand(), Configuration {

	override fun run() {
		val code = Main().run(this)
		throw ProgramResult(code)
	}

	init {
		eagerOption("--version", help = "Prints the version and exits.") {
			throw PrintMessage("GH-Lint (${commandName}) version ${BuildConfig.APP_VERSION}")
		}
	}

	// IMPORTANT: The order of properties is important, it'll define how "--help" is printed.

	override val files: List<Path> by argument()
		.path(mustExist = true, canBeDir = false)
		.multiple(required = true)
		.help("File to scan.")

	override val reportExitCode: Boolean by option("--exit")
		.flag("--ignore-failures", default = true, defaultForHelp = "on")
		.help("Exit with non-zero code if there are findings. Default: --exit.")

	override val reportConsole: Boolean by option("--console")
		.flag("--silent", default = true, defaultForHelp = "on")
		.help("Output to console. --silent does not affect --verbose, only findings. Default: --console.")

	override val reportSarif: Path? by option("--sarif")
		// Theoretically mustBeWritable = true, but Java requires the file to exists, which is silly.
		.path(canBeDir = false)
		.help("Output a SARIF file.")

	override val reportGitHubCommands: Boolean by option("--ghcommands")
		.boolean()
		.default(false)
		.help("Output GitHub Commands (warnings).")

	override val verbose: Boolean by option("-v", "--verbose")
		.flag(default = false, defaultForHelp = "off")
		.help("Prints more information.")
}
