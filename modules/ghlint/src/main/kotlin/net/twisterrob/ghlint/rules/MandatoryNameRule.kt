package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

public class MandatoryNameRule : VisitorRule {

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		if (workflow.name == null) {
			reporting.report(MandatoryWorkflowName, workflow) { "${it} must have a name." }
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		if (job.name == null) {
			reporting.report(MandatoryJobName, job) { "${it} must have a name." }
		}
	}

	public override fun visitStep(reporting: Reporting, step: Step) {
		if (step.name == null) {
			reporting.report(MandatoryStepName, step) { "${it} must have a name." }
		}
	}

	internal companion object {

		val MandatoryWorkflowName =
			Issue("MandatoryWorkflowName", "Workflow must have a name.")

		val MandatoryJobName =
			Issue("MandatoryJobName", "Job must have a name.")

		val MandatoryStepName =
			Issue("MandatoryStepName", "Step must have a name.")
	}
}
