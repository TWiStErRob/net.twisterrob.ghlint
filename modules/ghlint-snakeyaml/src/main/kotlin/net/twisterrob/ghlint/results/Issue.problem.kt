package net.twisterrob.ghlint.results

import net.twisterrob.ghlint.model.HasSnakeNode
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Model
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.file
import net.twisterrob.ghlint.model.id
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.toLocation

context(Rule)
public fun Issue.problem(
	context: Model,
	finalMessage: String,
): Finding = Finding(
	rule = this@Rule,
	issue = this,
	location = Location.from(context),
	message = finalMessage,
)

public fun Model.toTarget(): String =
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

private fun Location.Companion.from(context: Model): Location =
	if (context is HasSnakeNode) {
		context.node.toLocation(context.file)
	} else {
		error("Must implement ${HasSnakeNode::class}, got: ${context}")
	}
