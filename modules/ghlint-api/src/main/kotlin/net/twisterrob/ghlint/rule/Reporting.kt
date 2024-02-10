package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Model
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.id
import net.twisterrob.ghlint.results.Finding

public interface Reporting {

	public fun report(finding: Finding)
}

context(Rule)
public fun Reporting.report(issue: Issue, context: Model, message: (String) -> String): Finding =
	Finding(
		rule = this@Rule,
		issue = issue,
		location = context.location,
		message = message(context.toTarget()),
	)

private fun Model.toTarget(): String =
	when (this) {
		is Workflow -> "workflow ${this.id}"
		is Job -> "job ${this.id}"
		is Step -> "step ${this.identifier} in ${this.parent.toTarget()}"
	}

private val Step.identifier: String
	get() = this.id
		?: this.name?.let { "\"${it}\"" }
		?: (this as? Step.Uses)?.uses
		?: this.index.toString()
