package net.twisterrob.ghlint.results

/**
 * 1-based column number.
 */
@JvmInline
public value class ColumnNumber(
	public val number: Int,
) {

	init {
		require(number > 0) { "Column number must be positive: ${number}" }
	}

	public companion object
}
