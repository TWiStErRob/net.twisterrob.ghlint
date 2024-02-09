package net.twisterrob.ghlint.model

public interface VisitorRule : Rule, Visitor {

	override fun check(workflow: Workflow): List<Finding> {
		val reporting = object : Reporting {
			val findings: MutableList<Finding> = mutableListOf()
			override fun report(issue: Issue, context: Model) {
				findings.add(issue.problem(context))
			}
		}
		visitWorkflow(reporting, workflow)
		return reporting.findings
	}
}

public interface Visitor {

	public fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		workflow.jobs.values.forEach { job ->
			visitJob(reporting, job)
		}
	}

	public fun visitJob(reporting: Reporting, job: Job) {
		when (job) {
			is Job.NormalJob -> visitNormalJob(reporting, job)
			is Job.ReusableWorkflowCallJob -> visitReusableWorkflowCallJob(reporting, job)
		}
	}

	public fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		job.steps.forEach { step ->
			visitStep(reporting, step)
		}
	}

	public fun visitReusableWorkflowCallJob(reporting: Reporting, job: Job.ReusableWorkflowCallJob) {
		// No children.
	}

	public fun visitStep(reporting: Reporting, step: Step) {
		when (step) {
			is Step.Run -> visitRunStep(reporting, step)
			is Step.Uses -> visitUsesStep(reporting, step)
		}
	}

	public fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		// No children.
	}

	public fun visitRunStep(reporting: Reporting, step: Step.Run) {
		// No children.
	}
}

public interface Reporting {

	public fun report(issue: Issue, context: Model)
}
