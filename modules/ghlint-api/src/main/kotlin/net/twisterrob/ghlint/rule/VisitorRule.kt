package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Model
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding

// STOPSHIP
//import net.twisterrob.ghlint.results.problem
//import net.twisterrob.ghlint.results.toTarget

public interface VisitorRule : Rule, Visitor {

	override fun check(workflow: Workflow): List<Finding> {
		val reporting = object : Reporting {
			val findings: MutableList<Finding> = mutableListOf()
			override fun report(issue: Issue, context: Model, message: (String) -> String) {
//				val finalMessage = message(context.toTarget())
//				findings.add(issue.problem(context, finalMessage))
			}
		}
		visitWorkflow(reporting, workflow)
		return reporting.findings
	}
}
