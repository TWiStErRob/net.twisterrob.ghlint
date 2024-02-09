package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Finding
import net.twisterrob.ghlint.model.Issue
import net.twisterrob.ghlint.model.Rule
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.problem

public class JobNamingRule : Rule {

	override fun check(workflow: Workflow): List<Finding> =
		workflow.jobs.mapNotNull { (id, job) ->
			if (!isValid(id)) {
				JobIdNaming.problem(job)
			} else {
				null
			}
		}

	private fun isValid(fileName: String): Boolean =
		fileName.lowercase() == fileName

	internal companion object {

		val JobIdNaming =
			Issue("JobIdName", "Job must have a name")
	}
}
