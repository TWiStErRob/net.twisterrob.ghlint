package net.twisterrob.ghlint.reporting

import net.twisterrob.ghlint.model.name
import net.twisterrob.ghlint.results.Finding

public class TextReporter(
	private val output: Appendable
) : Reporter {

	override fun report(findings: List<Finding>) {
		findings.forEach {
			output.appendLine(it.render())
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
	return "${issue.id} at ${location.file.name}:${loc}: ${message}"
}
