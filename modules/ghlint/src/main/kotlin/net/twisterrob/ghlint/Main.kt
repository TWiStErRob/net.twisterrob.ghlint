package net.twisterrob.ghlint

import net.twisterrob.ghlint.analysis.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.reporting.SarifReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.rules.DefaultRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet
import net.twisterrob.ghlint.yaml.Yaml
import java.nio.file.Path
import kotlin.io.path.readText

public fun main(vararg args: String) {
	val defaultRuleSet: RuleSet = DefaultRuleSet()
	val files = args.map { File(FileLocation(it), Path.of(it).readText()) }

	val validationResults = Validator().validateWorkflows(files)
	val analysisResults = Yaml.analyze(files, listOf(defaultRuleSet))
	val allFindings = validationResults + analysisResults

	TextReporter(System.out).report(allFindings)
	SarifReporter.report(allFindings, Path.of("report.sarif"), Path.of("."))
}
