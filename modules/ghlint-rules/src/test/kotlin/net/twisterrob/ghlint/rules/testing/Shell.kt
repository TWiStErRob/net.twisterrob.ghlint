package net.twisterrob.ghlint.rules.testing

object Shell {

	/**
	 * This method relies on being called from specific test methods. The indentation is tailored for them.
	 * The `nl` variable contains indentation for both Kotlin test code and yaml.
	 * To make it work for both actions and jobs, the tests needs to use the fact that arrays don't need to be indented:
	 * ```yaml
	 * steps:
	 *   - run: echo "Test"
	 * ```
	 * is the same as:
	 * ```yaml
	 * steps:
	 * - run: echo "Test"
	 * ```
	 * However, conventionally the first one is preferred.
	 */
	fun redirects(prefix: String): Map<String, String> {
		@Suppress("detekt.StringShouldBeRawString") // All whitespace is significant.
		val nl = "\n\t\t\t\t\t\t\t\t\t          "
		@Suppress("RemoveSingleExpressionStringTemplate", "REDUNDANT_SINGLE_EXPRESSION_STRING_TEMPLATE") // Consistency.
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
