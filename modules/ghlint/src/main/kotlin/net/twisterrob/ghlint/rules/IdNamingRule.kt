package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

@Suppress("detekt.StringLiteralDuplication") // Inside lambda, only visually identical.
public class IdNamingRule : VisitorRule {

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		if (!isValid(workflow.parent.file.name)) {
			reporting.report(WorkflowIdNaming, workflow) { "${it} must have a lowercase id." }
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		if (!isValid(job.id)) {
			reporting.report(JobIdNaming, job) { "${it} must have a lowercase id." }
		}
	}

	public override fun visitStep(reporting: Reporting, step: Step) {
		if (step.id?.let(::isValid) == false) {
			reporting.report(StepIdNaming, step) { "${it} must have a lowercase id." }
		}
	}

	private fun isValid(fileName: String): Boolean =
		fileName.lowercase() == fileName

	internal companion object {

		val WorkflowIdNaming =
			Issue("WorkflowIdNaming", "Workflow must have lowercase id.")

		val JobIdNaming =
			Issue("JobIdNaming", "Job must have lowercase id.")

		val StepIdNaming =
			Issue("StepIdNaming", "Step must have lowercase id.")
	}
}
