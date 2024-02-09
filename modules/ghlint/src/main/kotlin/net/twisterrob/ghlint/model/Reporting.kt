package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.Optional

context(Rule)
public fun Issue.problem(
	context: Model,
): Finding = Finding(
	rule = this@Rule,
	issue = this,
	location = Location.from(context),
)

public fun Location.Companion.from(context: Model): Location =
	when (context) {
		is InternalModel -> Location(
			file = FileName.from(context),
			start = context.node.startMark.toPosition(),
			end = context.node.endMark.toPosition(),
		)
	}

public fun FileName.Companion.from(context: Model): FileName =
	when (context) {
		is Workflow -> context.parent.file
		is Job -> context.parent.parent.file
		is Step -> context.parent.parent.parent.file
		is InternalModel -> error("Location from ${context} is not implemented yet.")
	}

private fun Optional<Mark>.toPosition(): Position =
	this.get().toPosition()

private fun Mark.toPosition(): Position =
	Position(LineNumber(this.line), ColumnNumber(this.column))
