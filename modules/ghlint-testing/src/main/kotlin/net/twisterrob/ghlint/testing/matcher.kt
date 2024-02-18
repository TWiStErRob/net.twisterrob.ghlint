package net.twisterrob.ghlint.testing

import io.kotest.assertions.print.Print
import io.kotest.assertions.print.Printed
import io.kotest.assertions.print.Printers
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.and
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.results.Finding

@Suppress("unused") // Initialize static framework when these assertions are used.
private val init = run {
	Printers.add(Finding::class, object : Print<Finding> {
		override fun print(a: Finding, level: Int): Printed = Printed(a.testString())

		@Suppress("OVERRIDE_DEPRECATION")
		override fun print(a: Finding): Printed = error("Unused")
	})
}

/**
 * Matches findings that have no findings.
 * It fails if there are any findings.
 *
 * Prefer to use this instead of [beEmpty] for better error reporting.
 * In case of failure this will print the finding details, while the other may or may not.
 *
 * Recommended usage:
 * ```
 * results shouldHave noFindings()
 * ```
 */
public fun noFindings(): Matcher<List<Finding>> = beEmpty()

/**
 * Matches findings containing exactly one specific issue.
 * It fails if there are no or more findings, or if there's a mismatch.
 *
 * Recommended usage:
 * ```
 * results shouldHave singleFinding(
 *     "IssueName1",
 *     "Finding message line 1."
 * )
 * ```
 *
 * Tip: if there's an extra finding that you don't care about, filter them first:
 * ```
 * results.filterNot { it.issue.id == "DontCareAboutThis" } shouldHave singleFinding(
 *     "NegativeStatusCheck",
 *     "Step[#0] in Job[test] uses a negative condition."
 * )
 * ```
 * but it's recommended to use [exactFindings] wherever possible.
 */
public fun singleFinding(issue: String, message: String): Matcher<List<Finding>> =
	haveSize<Finding>(1) and aFinding(issue, message)

/**
 * Matches a specific finding in the list.
 * It fails if there is no finding with matching attributes.
 *
 * Recommended usage: do not use directly with [shouldHave].
 *  * If you're matching one finding, use [singleFinding] instead.
 *  * If you're matching multiple findings, use [exactFindings] instead.
 */
public fun aFinding(issue: String, message: String): Matcher<List<Finding>> =
	object : Matcher<List<Finding>> {
		override fun test(value: List<Finding>): MatcherResult = MatcherResult(
			value.singleOrNull { it.issue.id == issue && it.message == message } != null,
			@Suppress("StringShouldBeRawString")
			{ "Could not find \"${issue}: ${message}\" among findings:\n${value.testString()}" },
			@Suppress("StringShouldBeRawString")
			{ "Collection should not have \"${issue}: ${message}\", but contained:\n${value.testString()}" }
		)
	}

/**
 * Matches findings containing multiple specific issues.
 * It fails if there are more or less findings, or if there's a mismatch.
 *
 * Recommended usage:
 * ```
 * results shouldHave exactFindings(
 *     aFinding(
 *         "IssueName1",
 *         "Finding message line 1."
 *     ),
 *     aFinding(
 *         "IssueName2",
 *         "Finding message line 2."
 *     ),
 * )
 * ```
 */
public fun exactFindings(vararg findings: Matcher<List<Finding>>): Matcher<List<Finding>> =
	haveSize<Finding>(findings.size) and findings.reduce(Matcher<List<Finding>>::and)

/**
 * Matches findings of a specific issue.
 * It fails if there are any findings that are not of the specified issue.
 *
 * The negated version (`... shouldNotHave onlyFindings("...")`)
 * is not recommended as it does not behave as expected.
 * This is why this method is internal for now.
 * @see `MatcherKtTest.multiple different finding (including target) matches`
 */
internal fun onlyFindings(issue: String): Matcher<List<Finding>> =
	object : Matcher<List<Finding>> {
		override fun test(value: List<Finding>): MatcherResult = MatcherResult(
			value.isNotEmpty() && value.all { it.issue.id == issue },
			{ "Could not find ${issue} among findings:\n${value.testString()}" },
			{ "Collection should not have ${issue}, but contained:\n${value.testString()}" }
		)
	}
