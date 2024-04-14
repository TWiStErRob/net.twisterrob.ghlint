package net.twisterrob.ghlint.results

import net.twisterrob.ghlint.model.FileLocation

/**
 * Represents a range of characters in a file.
 *
 * For example to represent (`startLine:startColumn-endLine:endColumn`):
 *  * the character "a" in "abc": `1:1-1:2`
 *  * the full text of "abc": `1:1-1:4`
 *  * the range of "bc" in "abcdef": `1:2-1:4`
 *  * the space between characters "b" and "c" in "abcd": `1:3-1:3`
 */
public class Location(
	public val file: FileLocation,

	/**
	 * The location of the first character.
	 * Line numbers are _inclusive_ and start with 1.
	 * Column numbers are _inclusive_ and start with 1.
	 */
	public val start: Position,

	/**
	 * The location **after** the last character.
	 * Line numbers are _inclusive_ and start with 1.
	 * Column numbers are _exclusive_ and start with 1.
	 */
	public val end: Position,
) {

	init {
		require(start.line.number <= end.line.number) {
			"Start line must be before or equal to end line: " +
					"${start.line.number} <= ${end.line.number}"
		}
		require(start.line.number != end.line.number || start.column.number <= end.column.number) {
			"Start column must be before or equal to end column on the same line (${start.line.number}): " +
					"${start.column.number} <= ${end.column.number}"
		}
	}

	public companion object
}
