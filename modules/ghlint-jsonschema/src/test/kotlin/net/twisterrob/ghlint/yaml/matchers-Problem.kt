package net.twisterrob.ghlint.yaml

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.and
import io.kotest.matchers.collections.haveSize

internal fun singleProblem(instanceLocation: String, error: String): Matcher<List<YamlValidationProblem>> =
	haveSize<YamlValidationProblem>(1) and aProblem(instanceLocation, error)

internal fun aProblem(instanceLocation: String, error: String): Matcher<List<YamlValidationProblem>> =
	object : Matcher<List<YamlValidationProblem>> {
		override fun test(value: List<YamlValidationProblem>): MatcherResult = MatcherResult(
			value.singleOrNull { it.error == error && it.instanceLocation == instanceLocation } != null,
			@Suppress("detekt.StringShouldBeRawString")
			{ "Could not find \"${instanceLocation}: ${error}\" among problems:\n${value.testString()}" },
			@Suppress("detekt.StringShouldBeRawString")
			{ "Collection should not have \"${instanceLocation}: ${error}\", but contained:\n${value.testString()}" }
		)
	}

private fun YamlValidationProblem.testString(): String =
	"""YamlValidationProblem(instanceLocation=${this.instanceLocation}, error=${this.error})"""

private fun List<YamlValidationProblem>.testString(): String =
	if (this.isEmpty()) {
		"No problems."
	} else {
		this.joinToString(separator = "\n", transform = YamlValidationProblem::testString)
	}
