package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.Optional

context(Rule)
public fun Issue.problem(
	context: Any,
): Finding = Finding(
	rule = this@Rule,
	issue = this,
	location = Location.from(context),
)

public fun Location.Companion.from(context: Any): Location =
	when (context) {
		is Workflow -> Location(
			file = context.parent.file,
			start = context.node.startMark.toPosition(),
			end = context.node.endMark.toPosition(),
		)

		is Job -> Location(
			file = context.parent.parent.file,
			start = context.node.startMark.toPosition(),
			end = context.node.endMark.toPosition(),
		)

		is Step -> Location(
			file = context.parent.parent.parent.file,
			start = context.node.startMark.toPosition(),
			end = context.node.endMark.toPosition(),
		)

		else -> error("Location from ${context} is not implemented yet.")
	}

private fun Optional<Mark>.toPosition(): Position =
	this.get().toPosition()

private fun Mark.toPosition(): Position =
	Position(LineNumber(this.line), ColumnNumber(this.column))
