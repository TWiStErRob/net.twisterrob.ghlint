package net.twisterrob.ghlint.testing

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import net.twisterrob.ghlint.results.Finding

public fun beEmpty(): Matcher<List<Finding>> = object : Matcher<List<Finding>> {
	override fun test(value: List<Finding>): MatcherResult = MatcherResult(
		value.isEmpty(),
		{ "Findings should be empty but contained:\n${value.testString()}" },
		{ "Collection should not be empty." }
	)
}

public fun haveFinding(issue: String): Matcher<List<Finding>> = object : Matcher<List<Finding>> {
	override fun test(value: List<Finding>): MatcherResult = MatcherResult(
		value.singleOrNull { it.issue.id == issue } != null,
		{ "Could not find ${issue} among findings:\n${value.testString()}" },
		{ "Collection should not have ${issue}, but contained:\n${value.testString()}" }
	)
}

public fun haveFinding(issue: String, message: String): Matcher<List<Finding>> = object : Matcher<List<Finding>> {
	override fun test(value: List<Finding>): MatcherResult = MatcherResult(
		value.singleOrNull { it.issue.id == issue && it.message == message } != null,
		{ "Could not find ${issue}: ${message} among findings:\n${value.testString()}" },
		{ "Collection should not have ${issue}: ${message}, but contained:\n${value.testString()}" }
	)
}
