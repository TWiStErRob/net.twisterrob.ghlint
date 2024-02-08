package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Issue
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Reporting
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.VisitorRule
import net.twisterrob.ghlint.model.Workflow

public class MandatoryNameRule : VisitorRule {

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		if (workflow.name == null) {
			reporting.report(MandatoryWorkflowName, workflow)
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		if (job.name == null) {
			reporting.report(MandatoryJobName, job)
		}
	}

	public override fun visitStep(reporting: Reporting, step: Step) {
		if (step.name == null) {
			reporting.report(MandatoryStepName, step)
		}
	}

	internal companion object {

		val MandatoryWorkflowName =
			Issue("MandatoryWorkflowName", "Job must have a name")

		val MandatoryJobName =
			Issue("MandatoryJobName", "Job must have a name")

		val MandatoryStepName =
			Issue("MandatoryStepName", "Step must have a name")
	}
}
