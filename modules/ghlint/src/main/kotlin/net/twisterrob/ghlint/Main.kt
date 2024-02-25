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
	Main().run(object : Configuration {
		override val files: List<Path> = args.map { Path.of(it) }
		override val verbose: Boolean = true
		override val reportConsole: Boolean = true
		override val reportSarif: Path? = Path.of("report.sarif")
		override val reportGitHubCommands: Boolean = System.getenv("GITHUB_ACTIONS") == "true"
	})
}

public class Main {

	public fun run(config: Configuration) {
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
	}
}
