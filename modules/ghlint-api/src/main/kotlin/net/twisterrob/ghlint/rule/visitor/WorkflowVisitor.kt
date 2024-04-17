package net.twisterrob.ghlint.rule.visitor

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Reporting
import javax.annotation.OverridingMethodsMustInvokeSuper

@Suppress("detekt.TooManyFunctions", "detekt.ComplexInterface")
public interface WorkflowVisitor {

	@OverridingMethodsMustInvokeSuper
	public fun visitWorkflowFile(reporting: Reporting, file: File) {
		visitWorkflow(reporting, file.content as Workflow)
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		workflow.jobs.values.forEach { job ->
			visitJob(reporting, job)
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitJob(reporting: Reporting, job: Job) {
		when (job) {
			is Job.NormalJob -> visitNormalJob(reporting, job)
			is Job.ReusableWorkflowCallJob -> visitReusableWorkflowCallJob(reporting, job)
			is Job.BaseJob -> error("Unknown job type: ${job}")
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		job.steps.forEach { step ->
			visitWorkflowStep(reporting, step)
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitReusableWorkflowCallJob(reporting: Reporting, job: Job.ReusableWorkflowCallJob) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitWorkflowStep(reporting: Reporting, step: WorkflowStep) {
		when (step) {
			is WorkflowStep.Run -> visitRunStep(reporting, step)
			is WorkflowStep.Uses -> visitUsesStep(reporting, step)
			is WorkflowStep.BaseStep -> error("Unknown step type: ${step}")
		}
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		// No children.
	}

	@OverridingMethodsMustInvokeSuper
	public fun visitRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		// No children.
	}
}
