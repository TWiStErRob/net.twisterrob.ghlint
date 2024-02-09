package net.twisterrob.ghlint.results

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileName
import net.twisterrob.ghlint.model.InternalModel
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Model
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.nodes.Node
import java.util.Optional

context(Rule)
public fun Issue.problem(
	context: Model,
): Finding = Finding(
	rule = this@Rule,
	issue = this,
	location = Location.from(context),
)

private fun Location.Companion.from(context: Model): Location =
	when (context) {
		is InternalModel -> {
			context.node.toLocation(FileName.from(context))
		}
	}

internal fun Node.toLocation(file: File): Location =
	Location(
		file = file.file,
		start = startMark.toPosition(),
		end = endMark.toPosition(),
	)

private fun FileName.Companion.from(context: Model): File =
	when (context) {
		is Workflow -> context.parent
		is Job -> context.parent.parent
		is Step -> context.parent.parent.parent
		is InternalModel -> error("Location from ${context} is not implemented yet.")
	}

private fun Optional<Mark>.toPosition(): Position =
	this.get().toPosition()

private fun Mark.toPosition(): Position =
	Position(LineNumber(this.line), ColumnNumber(this.column))
