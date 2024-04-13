package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.reporting.GitHubCommandReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.reporting.sarif.SarifReporter
import net.twisterrob.ghlint.rules.DefaultRuleSet
import net.twisterrob.ghlint.yaml.SnakeYaml
import kotlin.io.path.readText

public class GHLint {

	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	public fun run(config: Configuration): Int {
		if (config.isVerbose) {
			config.files.forEach {
				println("Received ${it} for analysis against JSON-schema and rules.")
			}
		}
		val files = config.files.map { File(FileLocation(it.toString()), it.readText()) }
		val validationResults = Validator().validateWorkflows(files)
		val ruleSets = listOf(DefaultRuleSet())
		val analysisResults = SnakeYaml.analyze(files, ruleSets)
		val allFindings = validationResults + analysisResults
		if (config.isVerbose) {
			println("There are ${allFindings.size} findings.")
		}

		if (config.isReportConsole) {
			if (config.isVerbose) {
				println("Reporting findings to console.")
			}
			TextReporter(System.out).report(allFindings)
		}
		if (config.isReportGitHubCommands) {
			if (config.isVerbose) {
				println("Reporting findings via GitHub Commands.")
			}
			GitHubCommandReporter(
				repositoryRoot = config.root,
				output = System.out,
			).report(allFindings)
		}
		config.sarifReportLocation?.run {
			if (config.isVerbose) {
				println("Writing findings to SARIF file: ${this}.")
			}
			SarifReporter.report(
				ruleSets = ruleSets,
				findings = allFindings,
				target = this,
				rootDir = config.root,
			)
		}
		val code = if (config.isReportExitCode && allFindings.isNotEmpty()) 1 else 0
		if (config.isVerbose) {
			println("Exiting with code ${code}.")
		}
		return code
	}
}
