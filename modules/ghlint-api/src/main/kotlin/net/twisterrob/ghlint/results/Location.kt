package net.twisterrob.ghlint.results

import net.twisterrob.ghlint.model.FileLocation

public class Location(
	public val file: FileLocation,
	public val start: Position,
	public val end: Position,
) {

	public companion object
}
