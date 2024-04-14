package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position

public sealed interface Content {

	public val parent: File

	public val location: Location

	public companion object
}

public interface InvalidContent : Content {

	public val error: Throwable

	public override val location: Location
		get() = wholeFile

	public companion object
}

private val Content.wholeFile: Location
	get() {
		val lines = parent.origin.content.lineSequence()
		return Location(
			file = parent.location,
			start = Position(LineNumber(1), ColumnNumber(1)),
			end = Position(
				LineNumber(lines.count()),
				ColumnNumber(lines.last().count())
			),
		)
	}
