package net.twisterrob.ghlint

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path

internal class CLI : CliktCommand(
	name = "ghlint",
	printHelpOnEmptyArgs = true,
	help = """
		GitHub Actions Linter (GH-Lint).
		A tool to lint GitHub Actions workflows.
		See https://ghlint.twisterrob.net for more.
	""".trimIndent(),
), Configuration {

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

	private val inputs: InputOptions by InputOptions()

	override val verbose: Boolean by option("-v", "--verbose")
		.flag(default = false, defaultForHelp = "off")
		.help("Prints more information.")

	override val reportExitCode: Boolean get() = inputs.reportExitCode
	override val reportConsole: Boolean get() = inputs.reportConsole
	override val reportSarif: Path? get() = inputs.reportSarif
	override val reportGitHubCommands: Boolean get() = inputs.reportGitHubCommands

	private class InputOptions : OptionGroup("Reporting") {

		val reportExitCode: Boolean by option("--exit")
			.flag("--ignore-failures", default = false)
			.help("Exit with non-zero code if there are findings. Default: --ignore-failures.")

		val reportConsole: Boolean by option("--console")
			.flag("--silent", default = true)
			.help("Output to console. Default: --console. --silent does not affect --verbose, only findings.")

		val reportSarif: Path? by option("--sarif")
			// Theoretically mustBeWritable = true, but Java requires the file to exists, which is silly.
			.path(canBeDir = false)
			.help("Output a SARIF file.")

		val reportGitHubCommands: Boolean by option("--ghcommands")
			.flag()
			.help("Output GitHub Commands (warnings).")
	}
}