package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Issue
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Reporting
import net.twisterrob.ghlint.model.Rule
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow

public class IdNamingRule : Rule {

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		if (workflow.fileName.lowercase() != workflow.fileName) {
			reporting.report(WorkflowIdNaming, workflow)
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		if (job.id.lowercase() != job.id) {
			reporting.report(JobIdNaming, job)
		}
	}

	public override fun visitStep(reporting: Reporting, step: Step) {
		if (step.id?.lowercase() != step.id) {
			reporting.report(StepIdNaming, step)
		}
	}

	internal companion object {

		val WorkflowIdNaming =
			Issue("WorkflowIdNaming", "Job must have a name")

		val JobIdNaming =
			Issue("JobIdNaming", "Job must have a name")

		val StepIdNaming =
			Issue("StepIdNaming", "Step must have a name")
	}
}
