package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.reporting.SarifReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.rules.DefaultRuleSet
import net.twisterrob.ghlint.yaml.Yaml
import java.nio.file.Path
import kotlin.io.path.readText

public fun main(vararg args: String) {
	args.forEach {
		@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
		println("Received ${it} for analysis against JSON-schema and rules.")
	}
	val files = args.map { File(FileLocation(it), Path.of(it).readText()) }

	val validationResults = Validator().validateWorkflows(files)
	val ruleSets = listOf(DefaultRuleSet())
	val analysisResults = Yaml.analyze(files, ruleSets)
	val allFindings = validationResults + analysisResults

	TextReporter(System.out).report(allFindings)
	SarifReporter.report(
		ruleSets = ruleSets,
		findings = allFindings,
		target = Path.of("report.sarif"),
		rootDir = Path.of("."),
	)
}
