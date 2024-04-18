package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.id
import net.twisterrob.ghlint.results.Finding

public interface Reporting {

	public fun report(finding: Finding)
}

context(Rule)
public fun Reporting.report(issue: Issue, context: Component, message: (String) -> String) {
	report(
		Finding(
			rule = this@Rule,
			issue = issue,
			location = context.location,
			message = message(context.toTarget()),
		)
	)
}

public fun Component.toTarget(): String =
	when (this) {
		is Workflow -> "Workflow[${this.id}]"
		is Job -> "Job[${this.id}]"
		is WorkflowStep -> "Step[${this.identifier}] in ${this.parent.toTarget()}"
		is Action -> "Action[${this.id ?: "\"${this.name}\""}]"
		is ActionStep -> "Step[${this.identifier}] in ${this.parent.parent.toTarget()}"
	}

private val Step.identifier: String
	get() = this.id
		?: this.name?.let { "\"${it}\"" }
		?: (this as? Step.Uses)?.run { uses.uses }
		?: "#${this.index.value}"

private val ActionStep.identifier: String
	get() = this.id
		?: this.name?.let { "\"${it}\"" }
		?: (this as? ActionStep.Uses)?.run { uses.uses }
		?: "#${this.index.value}"
