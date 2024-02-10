package net.twisterrob.ghlint.results

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileName
import net.twisterrob.ghlint.model.InternalModel
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Model
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
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
		is InternalModel -> error("Internal model: ${this}")
	}

private val Step.identifier: String
	get() = this.id
		?: this.name?.let { "\"${it}\"" }
		?: (this as? Step.Uses)?.uses
		?: this.index.toString()

private fun Location.Companion.from(context: Model): Location =
	@Suppress("detekt.ElseCaseInsteadOfExhaustiveWhen", "UseIfInsteadOfWhen") // STOPSHIP why?
	when (context) {
		is InternalModel -> context.node.toLocation(FileName.from(context))
		else -> error("Only ${InternalModel::class} is supported, got: ${context}")
	}

private fun FileName.Companion.from(context: Model): File =
	when (context) {
		is Workflow -> context.parent
		is Job -> context.parent.parent
		is Step -> context.parent.parent.parent
		is InternalModel -> error("Location from ${context} is not implemented yet.")
	}
