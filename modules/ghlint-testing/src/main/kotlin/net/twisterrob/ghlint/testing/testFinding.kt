package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule

/**
 * Creates a Finding object with the given parameters.
 * Useful to create a finding with only relevant properties set.
 *
 * Minimal example usage:
 * ```
 * testFinding(
 * 	TestRule(),
 * 	TestRule.TestIssue,
 * )
 *
 * private class TestRule : Rule {
 *
 * 	override val issues: List<Issue> = listOf(TestIssue)
 *
 * 	override fun check(file: File): List<Finding> =
 * 		error("Should never be called.")
 *
 * 	companion object {
 *
 * 		val TestIssue = Issue(
 * 			id = "TestIssue",
 * 			title = "title",
 * 			description = "description",
 * 			compliant = emptyList(),
 * 			nonCompliant = emptyList()
 * 		)
 * 	}
 * }
 * ```
 */
@Suppress("detekt.LongParameterList")
public fun testFinding(
	rule: Rule,
	issue: Issue,
	file: String = "test.file",
	message: String = "message",
	startLine: Int = 1,
	startCol: Int = 2,
	endLine: Int = 3,
	endCol: Int = 4,
): Finding = Finding(
	rule = rule,
	issue = issue,
	location = Location(
		file = FileLocation(file),
		start = Position(line = LineNumber(startLine), column = ColumnNumber(startCol)),
		end = Position(line = LineNumber(endLine), column = ColumnNumber(endCol)),
	),
	message = message
)
