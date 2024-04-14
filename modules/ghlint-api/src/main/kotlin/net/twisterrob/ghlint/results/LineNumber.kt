package net.twisterrob.ghlint.results

/**
 * 1-based line number.
 */
@JvmInline
public value class LineNumber(
	public val number: Int,
) {

	init {
		require(number > 0) { "Line number must be positive: ${number}" }
	}

	public companion object
}
