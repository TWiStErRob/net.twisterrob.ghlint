package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position

internal class ErrorInvalidContent(
	override val raw: String,
	override val error: Throwable,
) : InvalidContent {

	@Suppress("LateinitUsage") // Can't figure out a better way.
	override lateinit var parent: File
		internal set

	override val location: Location
		get() = Location(
			file = parent.location,
			start = Position(LineNumber(0), ColumnNumber(0)),
			end = Position(LineNumber(0), ColumnNumber(0)),
		)

	companion object {

		fun create(file: RawFile, error: Throwable): File =
			ErrorInvalidContent(file.content, error).apply { parent = File(file.location, this) }.parent
	}
}
