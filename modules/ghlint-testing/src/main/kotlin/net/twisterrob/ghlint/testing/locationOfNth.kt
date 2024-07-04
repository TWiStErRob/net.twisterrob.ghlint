package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position

/**
 * Find the location of the [occurrence]th [string] in [this] [RawFile].
 *
 * @param string String to find.
 * @param occurrence 1-based index of the occurrence to find.
 *
 * @see aLocation
 * @see singleFinding
 * @see aFinding
 */
public operator fun RawFile.invoke(string: String, occurrence: Int = 1): String =
	this.locationOfNth(string, occurrence).testString()

/**
 * Find the [Location] of the [occurrence]th [string] in [this] [RawFile].
 *
 * @param string String to find.
 * @param occurrence 1-based index of the occurrence to find.
 */
public fun RawFile.locationOfNth(string: String, occurrence: Int = 1): Location =
	this.content.locationOfNth(this.location, string, occurrence)

internal fun String.locationOfNth(location: FileLocation, string: String, occurrence: Int = 1): Location {
	require('\r' !in this) { "Only \\n line endings are supported." }
	require(occurrence > 0) { "Occurrence must be positive, but was ${occurrence}." }
	val found = this.indexOfNth(string, occurrence)
	require(found != -1) { "Cannot find occurrence #${occurrence} of '${string}' in:\n$this" }
	return Location(
		location,
		this.positionOf(found),
		this.positionOf(found + string.length),
	)
}

private fun String.indexOfNth(string: String, occurrence: Int): Int =
	generateSequence(-1) { last -> indexOf(string, last + 1).takeIf { it != -1 } }
		.drop(occurrence)
		.firstOrNull()
		?: -1

private fun String.positionOf(index: Int): Position {
	val line = this.substring(0, index).count { '\n' == it }
	val before = this.lines().take(line).sumOf { it.length + 1 }
	return Position(
		LineNumber(line + 1),
		ColumnNumber(index - before + 1),
	)
}
