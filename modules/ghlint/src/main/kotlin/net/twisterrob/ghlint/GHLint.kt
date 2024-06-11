package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.Analyzer
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.reporting.GitHubCommandReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.reporting.sarif.SarifReporter
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rules.DefaultRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.jetbrains.annotations.TestOnly
import kotlin.io.path.readText
import kotlin.time.measureTimedValue

public class GHLint {

	@Suppress(
		"detekt.ForbiddenMethodCall", // TODO logging.
		// TODO feels like an abstraction is missing in this long method, but it's not clear what it is.
		"detekt.CognitiveComplexMethod",
	)
	public fun run(config: Configuration): Int {
		if (config.isVerbose) {
			println("Received the following files for analysis against JSON-schema and rules:")
			config.files.forEach { println(" * ${it}") }
			if (config.files.isEmpty()) {
				println("No files.")
			}
		}

		val files = config.files.map { RawFile(FileLocation(it.toString()), it.readText()) }
		val ruleSets = listOf(BuiltInRuleSet(), DefaultRuleSet())
		val result = measureTimedValue { analyze(files, ruleSets, config.isVerbose) }
		val findings = result.value

		if (config.isVerbose) {
			println("There are ${findings.size} findings in ${result.duration}.")
		}

		if (config.isReportConsole) {
			if (config.isVerbose) {
				println("Reporting findings to console.")
			}
			TextReporter(
				output = System.out,
			).report(findings)
		}
		if (config.isReportGitHubCommands) {
			if (config.isVerbose) {
				println("Reporting findings via GitHub Commands.")
			}
			GitHubCommandReporter(
				repositoryRoot = config.root,
				output = System.out,
			).report(findings)
		}
		config.sarifReportLocation?.run {
			if (config.isVerbose) {
				println("Writing findings to SARIF file: ${this}.")
			}
			SarifReporter.report(
				ruleSets = ruleSets,
				findings = findings,
				target = this,
				rootDir = config.root,
			)
		}
		val code = if (config.isReportExitCode && findings.isNotEmpty()) 1 else 0
		if (config.isVerbose) {
			println("Exiting with code ${code}.")
		}
		return code
	}

	@TestOnly
	internal fun analyze(files: List<RawFile>, ruleSets: List<RuleSet>, verbose: Boolean): List<Finding> {
		val loadedFiles = files.map(SnakeYaml::load)
		return Analyzer().analyze(loadedFiles, ruleSets, verbose)
	}
}
