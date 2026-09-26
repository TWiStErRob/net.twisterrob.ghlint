package net.twisterrob.ghlint.reporting

import net.twisterrob.ghlint.results.Finding

public class TextReporter(
	private val output: Appendable,
) : Reporter {

	override fun report(findings: List<Finding>) {
		findings.forEach {
			output.appendLine(it.render())
		}
		if (findings.isNotEmpty()) {
			output.appendLine()
			output.appendLine(buildHelpHint(findings))
		}
	}
}

private fun Finding.render(): String {
	val loc = with(location) {
		when {
			start == end -> "${start.line.number}:${start.column.number}"
			start.line == end.line -> "${start.line.number}:${start.column.number}-${end.column.number}"
			else -> "${start.line.number}:${start.column.number}-${end.line.number}:${end.column.number}"
		}
	}
	return "${issue.id} at ${location.file.path}:${loc}: ${message}"
}

private fun buildHelpHint(findings: List<Finding>): String {
	val uniqueIssueIds = findings.map { it.issue.id }.distinct()
	return if (uniqueIssueIds.size == 1) {
		"For more information about this rule, run: ghlint --help ${uniqueIssueIds.single()}"
	} else {
		"For more information about these rules, run: ghlint --help <RuleId>"
	}
}
