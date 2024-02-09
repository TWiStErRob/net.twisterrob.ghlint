package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class IdNamingRule : VisitorRule {

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		if (!isValid(workflow.parent.file.name)) {
			reporting.report(WorkflowIdNaming, workflow)
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		if (!isValid(job.id)) {
			reporting.report(JobIdNaming, job)
		}
	}

	public override fun visitStep(reporting: Reporting, step: Step) {
		if (step.id?.let(::isValid) == false) {
			reporting.report(StepIdNaming, step)
		}
	}

	private fun isValid(fileName: String): Boolean =
		fileName.lowercase() == fileName

	internal companion object {

		val WorkflowIdNaming =
			Issue("WorkflowIdNaming", "Workflow must have a name")

		val JobIdNaming =
			Issue("JobIdNaming", "Job must have a name")

		val StepIdNaming =
			Issue("StepIdNaming", "Step must have a name")
	}
}
