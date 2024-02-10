package net.twisterrob.ghlint.results

import net.twisterrob.ghlint.model.FileName

public class Location(
	public val file: FileName,
	public val start: Position,
	public val end: Position,
) {

	public companion object
}
