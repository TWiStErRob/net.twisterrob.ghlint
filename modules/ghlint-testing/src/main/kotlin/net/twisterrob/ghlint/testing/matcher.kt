package net.twisterrob.ghlint.testing

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import net.twisterrob.ghlint.results.Finding

public fun beEmpty(): Matcher<List<Finding>> = object : Matcher<List<Finding>> {
	override fun test(value: List<Finding>): MatcherResult = MatcherResult(
		value.isEmpty(),
		{
			val values = value.joinToString(separator = "\n", transform = Finding::testString)
			"Findings should be empty but contained:\n$values"
		},
		{ "Collection should not be empty" }
	)
}

public fun Finding.testString(): String {
	val coordinates = with(location) {
		"${start.line.number}:${start.column.number}-${end.line.number}:${end.column.number}"
	}
	return """
		|Finding(
		|	rule=${rule},
		|	issue=${issue.id},
		|	location=${location.file.path}/${coordinates},
		|	message=${message}
		|)
	""".trimMargin()
}

public fun haveFinding(issue: String, message: String): Matcher<List<Finding>> = object : Matcher<List<Finding>> {
	override fun test(value: List<Finding>): MatcherResult = MatcherResult(
		value.singleOrNull { it.issue.id == issue && it.message == message } != null,
		{
			val values = value.joinToString(separator = "\n", transform = Finding::testString)
			"Could not find ${issue}: ${message} among findings:\n${values}"
		},
		{ "Collection should not have ${issue}: ${message}." }
	)
}
