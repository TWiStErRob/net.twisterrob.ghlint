package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Finding
import net.twisterrob.ghlint.model.Issue
import net.twisterrob.ghlint.model.Rule
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.problem

public class WorkflowNamingRule : Rule {

	override fun check(workflow: Workflow): List<Finding> =
		if (!isValid(workflow.parent.file.name)) {
			listOf(WorkflowIdNaming.problem(workflow))
		} else {
			emptyList()
		}

	private fun isValid(fileName: String): Boolean =
		fileName.lowercase() == fileName

	internal companion object {

		val WorkflowIdNaming =
			Issue("WorkflowIdNaming", "Workflow must have a name")
	}
}
