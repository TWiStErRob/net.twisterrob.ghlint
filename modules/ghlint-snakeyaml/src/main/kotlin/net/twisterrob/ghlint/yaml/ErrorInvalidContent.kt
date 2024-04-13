package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position

internal class ErrorInvalidContent(
	override var parent: File,
	override val raw: String,
	override val error: Throwable,
) : InvalidContent {

	override val location: Location
		get() = Location(
			file = parent.location,
			start = Position(LineNumber(1), ColumnNumber(1)),
			end = Position(
				LineNumber(raw.lineSequence().count()),
				ColumnNumber(raw.lineSequence().last().count())
			),
		)
}
