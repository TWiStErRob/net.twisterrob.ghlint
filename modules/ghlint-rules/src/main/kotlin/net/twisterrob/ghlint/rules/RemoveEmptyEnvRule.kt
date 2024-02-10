package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.report
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule

@Suppress("detekt.StringLiteralDuplication")
public class RemoveEmptyEnvRule : VisitorRule {

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		val env = workflow.env
		if (env != null && env.isEmpty()) {
			reporting.report(RedundantEmptyWorkflowEnv, workflow) { "${it} should not have empty env." }
		}
	}

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		val env = job.env
		if (env != null && env.isEmpty()) {
			reporting.report(RedundantEmptyJobEnv, job) { "${it} should not have empty env." }
		}
	}

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		val env = step.env
		if (env != null && env.isEmpty()) {
			reporting.report(RedundantEmptyStepEnv, step) { "${it} should not have empty env." }
		}
	}

	internal companion object {

		val RedundantEmptyWorkflowEnv =
			Issue("RedundantEmptyWorkflowEnv", "Workflow must have lowercase id.")

		val RedundantEmptyJobEnv =
			Issue("RedundantEmptyJobEnv", "Job must have lowercase id.")

		val RedundantEmptyStepEnv =
			Issue("RedundantEmptyStepEnv", "Step must have lowercase id.")
	}
}
