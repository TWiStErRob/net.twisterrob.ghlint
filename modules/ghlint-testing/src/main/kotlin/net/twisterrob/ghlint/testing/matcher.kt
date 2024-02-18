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

/**
 * Matches findings of a specific issue.
 * It fails if there are any findings that are not of the specified issue.
 *
 * The negated version (`... shouldNotHave onlyFindings("...")`)
 * is not recommended as it does not behave as expected.
 * This is why this method is internal for now.
 * @see `MatcherKtTest.multiple different finding (including target) matches`
 */
internal fun onlyFindings(issue: String): Matcher<List<Finding>> = object : Matcher<List<Finding>> {
	override fun test(value: List<Finding>): MatcherResult = MatcherResult(
		value.isNotEmpty() && value.all { it.issue.id == issue },
		{ "Could not find ${issue} among findings:\n${value.testString()}" },
		{ "Collection should not have ${issue}, but contained:\n${value.testString()}" }
	)
}

public fun haveFindings(issue: String): Matcher<List<Finding>> = object : Matcher<List<Finding>> {
	override fun test(value: List<Finding>): MatcherResult = MatcherResult(
		value.any { it.issue.id == issue },
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
