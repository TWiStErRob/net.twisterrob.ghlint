package net.twisterrob.ghlint.rules.testing

object Shell {

	fun redirects(prefix: String): Map<String, String> {
		@Suppress("detekt.StringShouldBeRawString") // All whitespace is significant.
		val nl = "\n\t\t\t\t\t\t\t\t          "
		@Suppress("RemoveSingleExpressionStringTemplate")
		return mapOf(
			"immediate" to """${prefix}""",
			"separated" to """${prefix} """,
			"distant" to """${prefix}  """,
			"immediate new-line" to """${prefix}\${nl}""",
			"separated new-line" to """${prefix} \${nl}""",
			"distant new-line" to """${prefix}   \${nl}""",
			"after new-line immediate" to """\${nl}${prefix}""",
			"after new-line separated" to """\${nl}${prefix} """,
			"after new-line distant" to """\${nl}${prefix}   """,
			"after new-line indented" to """\${nl}    ${prefix}""",
			"after new-line indented separated" to """\${nl}  ${prefix}  """,
		)
	}

	/**
	 * Cartesian product of two maps, combining keys with a slash separator.
	 */
	@Suppress("detekt.FunctionMinLength") // It's an operator.
	infix fun Map<String, String>.x(other: Map<String, String>): Map<String, String> =
		flatMap { (oneKey, oneValue) ->
			other.map { (otherKey, otherValue) ->
				"${oneKey}/${otherKey}" to oneValue + otherValue
			}
		}.toMap()
}
