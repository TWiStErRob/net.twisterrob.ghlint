package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding

public interface VisitorRule : Rule, Visitor {

	override fun check(file: File): List<Finding> {
		val reporting = object : Reporting {
			val findings: MutableList<Finding> = mutableListOf()
			override fun report(finding: Finding) {
				findings.add(finding)
			}
		}
		visitFile(reporting, file)
		return reporting.findings
	}
}
