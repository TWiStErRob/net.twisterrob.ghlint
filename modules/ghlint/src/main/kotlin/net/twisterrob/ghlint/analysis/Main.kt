package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileName
import net.twisterrob.ghlint.reporting.IOReporter
import net.twisterrob.ghlint.rules.IdNamingRule
import net.twisterrob.ghlint.rules.MandatoryNameRule
import net.twisterrob.ghlint.rules.MandatoryShellRule

public fun main(vararg args: String) {
	val rules = listOf(
		MandatoryNameRule(),
		IdNamingRule(),
		MandatoryShellRule(),
	)
	val files = args.map { File(FileName(it)) }
	val findings = Analyzer().analyzeWorkflows(files, rules)

	IOReporter(System.out).report(findings)
}
