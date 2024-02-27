package net.twisterrob.ghlint.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import net.twisterrob.ghlint.Configuration
import net.twisterrob.ghlint.GHLINT_VERSION
import net.twisterrob.ghlint.GHLint
import java.nio.file.Path

public class CLI : CliktCommand(
	name = "ghlint",
	printHelpOnEmptyArgs = true,
	help = """
		GitHub Actions Linter (GH-Lint).
		A tool to lint GitHub Actions workflows.  
		See https://ghlint.twisterrob.net for more information.
	""".trimIndent(),
	epilog = """
		Example usages:
		
		**Lint a single workflow**:  
		`$ ghlint .github/workflows/main.yml`
		
		**Lint all workflows in GitHub Actions CI**:  
		`$ ghlint --verbose --sarif=report.sarif.json .github/workflows/*.yml`
		
		**Lint a single action in repository root.**:  
		`$ ghlint action.yml`
		
		**Lint for CI actions in a directory**:  
		`$ ghlint actions/my-action/action.yml`
	""".trimIndent(),
), Configuration {

	@Suppress("detekt.ClassOrdering")
	override fun run() {
		val code = GHLint().run(this)
		throw ProgramResult(code)
	}

	init {
		context {
			helpFormatter = {
				MordantHelpFormatter(
					context = it,
					showRequiredTag = true,
					showDefaultValues = true,
					requiredOptionMarker = "*",
				)
			}
		}
	}

	// IMPORTANT: The order of properties is important, it'll define how "--help" is printed.

	override val files: List<Path> by argument()
		.path(mustExist = true, canBeDir = false)
		.multiple(required = true)
		.help("Workflow YML files to check for problems.")

	private val inputs: InputOptions by InputOptions()

	override val root: Path by option("--root")
		.path(mustExist = true, canBeDir = true, canBeFile = false)
		.default(Path.of("."))
		.help("Root directory of the repository.")

	override val isVerbose: Boolean by option("--verbose")
		.flag(default = false, defaultForHelp = "off")
		.help("Prints more information.")

	init {
		eagerOption("-v", "--version", help = "Prints the version and exits.") {
			throw PrintMessage("GH-Lint (${commandName}) version ${GHLINT_VERSION}")
		}
	}

	override val isReportExitCode: Boolean get() = inputs.isReportExitCode
	override val isReportConsole: Boolean get() = inputs.isReportConsole
	override val isReportGitHubCommands: Boolean get() = inputs.isReportGitHubCommands
	override val sarifReportLocation: Path? get() = inputs.sarifReportLocation

	private class InputOptions : OptionGroup("Reporting") {

		val isReportExitCode: Boolean by option("--exit")
			.flag("--ignore-failures", default = false, defaultForHelp = "--ignore-failures")
			.help("Exit with non-zero code if there are findings.")

		val isReportConsole: Boolean by option("--console")
			.flag("--silent", default = true, defaultForHelp = "--console")
			.help("Output to console. --silent does not affect --verbose, only findings.")

		val isReportGitHubCommands: Boolean by option("--ghcommands")
			.flag(defaultForHelp = "off")
			.help("Output GitHub Commands (warnings).")

		val sarifReportLocation: Path? by option("--sarif")
			// Theoretically mustBeWritable = true, but Java requires the file to exists, which is silly.
			.path(canBeDir = false)
			.help("Output a SARIF file.")
	}
}
