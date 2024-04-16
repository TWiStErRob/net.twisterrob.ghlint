package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position

public interface File {

	public val origin: RawFile

	public val location: FileLocation
		get() = origin.location

	public val content: Content

	public companion object
}

public val File.wholeFile: Location
	get() {
		val lines = origin.content.lineSequence()
		return Location(
			file = location,
			start = Position(LineNumber(1), ColumnNumber(1)),
			end = Position(
				LineNumber(lines.count()),
				ColumnNumber(lines.last().count() + 1)
			),
		)
	}
