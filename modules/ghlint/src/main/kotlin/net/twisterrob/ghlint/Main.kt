package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.reporting.GitHubCommandReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.reporting.sarif.SarifReporter
import net.twisterrob.ghlint.rules.DefaultRuleSet
import net.twisterrob.ghlint.yaml.Yaml
import java.nio.file.Path
import kotlin.io.path.readText

public fun main(vararg args: String) {
	CLI().main(args)
}

public class Main {

	public fun run(config: Configuration): Int {
		if (config.verbose) {
			config.files.forEach {
				@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
				println("Received ${it} for analysis against JSON-schema and rules.")
			}
		}
		val files = config.files.map { File(FileLocation(it.toString()), it.readText()) }
		val validationResults = Validator().validateWorkflows(files)
		val ruleSets = listOf(DefaultRuleSet())
		val analysisResults = Yaml.analyze(files, ruleSets)
		val allFindings = validationResults + analysisResults
		if (config.verbose) {
			println("There are ${allFindings.size} findings.")
		}

		if (config.reportConsole) {
			if (config.verbose) {
				println("Reporting findings to console.")
			}
			TextReporter(System.out).report(allFindings)
		}
		if (config.reportGitHubCommands) {
			if (config.verbose) {
				println("Reporting findings via GitHub Commands.")
			}
			GitHubCommandReporter(
				repositoryRoot = Path.of("."),
				output = System.out,
			).report(allFindings)
		}
		config.reportSarif?.run {
			if (config.verbose) {
				println("Writing findings to SARIF file: ${this}.")
			}
			SarifReporter.report(
				ruleSets = ruleSets,
				findings = allFindings,
				target = this,
				rootDir = Path.of("."),
			)
		}
		val code = if (config.reportExitCode && allFindings.isNotEmpty()) 1 else 0
		if (config.verbose) {
			println("Exiting with code ${code}.")
		}
		return code
	}
}
