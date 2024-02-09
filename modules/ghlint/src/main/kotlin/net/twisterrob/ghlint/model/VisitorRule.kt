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
		job.steps.forEach { step ->
			visitStep(reporting, step)
		}
	}

	public fun visitStep(reporting: Reporting, step: Step) {
		// No children.
	}
}

public interface Reporting {

	public fun report(issue: Issue, context: Model)
}
