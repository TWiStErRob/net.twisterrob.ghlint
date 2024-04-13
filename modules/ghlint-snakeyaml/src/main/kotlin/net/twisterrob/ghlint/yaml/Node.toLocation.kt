package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.nodes.Node
import java.util.Optional

public fun Node.toLocation(file: RawFile): Location =
	Location(
		file = file.location,
		start = startMark.toPosition(),
		end = endMark.toPosition(),
	)

private val EMPTY_MARK = Mark("NOTHING", 0, 0, 0, intArrayOf(), 0)

private fun Optional<Mark>.toPosition(): Position =
	this.orElse(EMPTY_MARK).toPosition()

private fun Mark.toPosition(): Position =
	Position(LineNumber(1 + this.line), ColumnNumber(1 + this.column))
