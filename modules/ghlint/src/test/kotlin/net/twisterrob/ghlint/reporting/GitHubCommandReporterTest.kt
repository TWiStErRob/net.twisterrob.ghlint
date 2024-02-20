package net.twisterrob.ghlint.reporting

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.testing.testFinding
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path

private const val DOCS =
	"%0AFor more information, see the online documentation: https://ghlint.twisterrob.net/issues/default/TestIssue/"

class GitHubCommandReporterTest {

	@Test fun `reporting a single finding`() {
		val output = StringBuilder()
		val reporter = GitHubCommandReporter(Path.of("test"), output)

		val findings = listOf(
			testFinding(
				rule = TestRule(),
				issue = TestRule.TestIssue,
				file = "test/path/to/test.file",
			)
		)

		reporter.report(findings)

		output.toString() shouldBe """
			::warning file=path${File.separator}to${File.separator}test.file,line=1,endLine=3,title=TestIssue::message${DOCS}
			
		""".trimIndent()
	}

	@Test fun `reporting a single-line finding`() {
		val output = StringBuilder()
		val reporter = GitHubCommandReporter(Path.of("."), output)

		val findings = listOf(
			testFinding(
				rule = TestRule(),
				issue = TestRule.TestIssue,
				startLine = 42,
				endLine = 42,
			)
		)

		reporter.report(findings)

		output.toString() shouldBe """
			::warning file=test.file,line=42,endLine=42,title=TestIssue::message${DOCS}
			
		""".trimIndent()
	}

	@Test fun `reporting a multiline finding`() {
		val output = StringBuilder()
		val reporter = GitHubCommandReporter(Path.of("."), output)

		val findings = listOf(
			testFinding(
				rule = TestRule(),
				issue = TestRule.TestIssue,
				message = "multiline\nmessage",
			)
		)

		reporter.report(findings)

		output.toString() shouldBe """
			::warning file=test.file,line=1,endLine=3,title=TestIssue::multiline%0Amessage${DOCS}
			
		""".trimIndent()
	}

	@Test fun `reporting multiple findings`() {
		val output = StringBuilder()
		val reporter = GitHubCommandReporter(Path.of("."), output)

		val findings = listOf(
			testFinding(
				rule = TestRule(),
				issue = TestRule.TestIssue,
				file = "test1.file",
				startLine = 11,
				endLine = 12,
				message = "message1"
			),
			testFinding(
				rule = TestRule(),
				issue = TestRule.TestIssue,
				file = "test2.file",
				startLine = 21,
				endLine = 22,
				message = "message2"
			),
		)

		reporter.report(findings)

		output.toString() shouldBe """
			::warning file=test1.file,line=11,endLine=12,title=TestIssue::message1${DOCS}
			::warning file=test2.file,line=21,endLine=22,title=TestIssue::message2${DOCS}
			
		""".trimIndent()
	}
}

private class TestRule : Rule {

	override val issues: List<Issue> = listOf(TestIssue)

	override fun check(workflow: Workflow): List<Finding> =
		error("Should never be called.")

	companion object {

		val TestIssue = Issue(
			id = "TestIssue",
			title = "title",
			description = "description",
			compliant = emptyList(),
			nonCompliant = emptyList()
		)
	}
}
