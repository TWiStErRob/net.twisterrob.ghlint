package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileName
import net.twisterrob.ghlint.reporting.SarifReporter
import net.twisterrob.ghlint.reporting.TextReporter
import net.twisterrob.ghlint.rules.IdNamingRule
import net.twisterrob.ghlint.rules.MandatoryNameRule
import net.twisterrob.ghlint.rules.MandatoryShellRule
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.bufferedWriter

public fun main(vararg args: String) {
	val rules = listOf(
		MandatoryNameRule(),
		IdNamingRule(),
		MandatoryShellRule(),
	)
	val files = args.map { File(FileName(it)) }

	val validation = Validator().validateWorkflows(files)
	val findings = Analyzer().analyzeWorkflows(files, rules)
	val allFindings = validation + findings

	TextReporter(System.out).report(allFindings)
	Path.of("report.sarif").bufferedWriter().use {
		SarifReporter(it, Path.of(".").absolute()).report(allFindings)
	}
}
