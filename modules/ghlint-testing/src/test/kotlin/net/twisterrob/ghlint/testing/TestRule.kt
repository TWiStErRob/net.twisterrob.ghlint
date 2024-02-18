package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule

internal class TestRule : Rule {

	override val issues: List<Issue> = listOf(TestIssue1, TestIssue2, TestIssue3, TestIssue4)

	override fun check(workflow: Workflow): List<Finding> =
		error("Should never be called.")

	override fun toString(): String =
		"toString of ${TestRule::class.simpleName ?: error("Cannot self-reflect!")}"

	@Suppress("detekt.NamedArguments")
	companion object {

		val TestIssue1 = Issue("TestIssue1", "title1", "description1", emptyList(), emptyList())
		val TestIssue2 = Issue("TestIssue2", "title2", "description2", emptyList(), emptyList())
		val TestIssue3 = Issue("TestIssue3", "title3", "description3", emptyList(), emptyList())
		val TestIssue4 = Issue("TestIssue4", "title4", "description4", emptyList(), emptyList())

		@Suppress("detekt.LongParameterList")
		fun testFinding(
			issue: Issue,
			file: String = "test.file",
			message: String = "message",
			startLine: Int = 1,
			startCol: Int = 2,
			endLine: Int = 3,
			endCol: Int = 4,
		) = Finding(
			rule = TestRule(),
			issue = issue,
			location = Location(
				file = FileLocation(file),
				start = Position(line = LineNumber(startLine), column = ColumnNumber(startCol)),
				end = Position(line = LineNumber(endLine), column = ColumnNumber(endCol)),
			),
			message = message
		)
	}
}
