package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Model
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.problem
import net.twisterrob.ghlint.results.Finding

public interface VisitorRule : Rule, Visitor {

	override fun check(workflow: Workflow): List<Finding> {
		val reporting = object : Reporting {
			val findings: MutableList<Finding> = mutableListOf()
			override fun report(issue: Issue, context: Model) {
				findings.add(issue.problem(context))
			}
		}
		visitWorkflow(reporting, workflow)
		return reporting.findings
	}
}
