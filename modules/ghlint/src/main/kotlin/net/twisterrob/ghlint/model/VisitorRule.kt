package net.twisterrob.ghlint.model

public interface VisitorRule : Rule, Visitor {

	override fun check(workflow: Workflow): List<Finding> {
		val reporting = object : Reporting {
			val findings: MutableList<Finding> = mutableListOf()
			private val state: MutableMap<Rule, MutableMap<String, Any?>> = mutableMapOf()
			override fun report(issue: Issue, context: Any) {
				findings.add(issue.problem(context))
			}

			override fun putState(rule: Rule, key: String, value: Any?) {
				val ruleState = state.getOrPut(rule) { mutableMapOf() }
				ruleState[key] = value
			}

			override fun getState(rule: Rule, key: String): Any? {
				val ruleState = state.getOrPut(rule) { mutableMapOf() }
				return ruleState[key]
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

	public fun report(issue: Issue, context: Any)

	public fun putState(rule: Rule, key: String, value: Any?)
	public fun getState(rule: Rule, key: String): Any?
}
