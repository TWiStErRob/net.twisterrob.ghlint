package net.twisterrob.ghlint.reporting

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.testing.testFinding
import org.junit.jupiter.api.Test

class TextReporterTest {

	@Test fun `single finding`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue),
		)
		report(findings) shouldBe """
			TestIssue at test.file:1:2-3:4: message
			
		""".trimIndent()
	}


	@Test fun `only file name is output`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue, file = "nested/test/path/to/file.name"),
		)
		report(findings) shouldBe """
			TestIssue at file.name:1:2-3:4: message
			
		""".trimIndent()
	}


	@Test fun `multiple findings`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue),
			testFinding(TestRule(), TestRule.TestIssue),
			testFinding(TestRule(), TestRule.TestIssue),
		)
		report(findings) shouldBe """
			TestIssue at test.file:1:2-3:4: message
			TestIssue at test.file:1:2-3:4: message
			TestIssue at test.file:1:2-3:4: message
			
		""".trimIndent()
	}

	@Test fun `multiline finding`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue),
			testFinding(TestRule(), TestRule.TestIssue, message = "foo\nbar\nbaz"),
			testFinding(TestRule(), TestRule.TestIssue),
		)
		report(findings) shouldBe """
			TestIssue at test.file:1:2-3:4: message
			TestIssue at test.file:1:2-3:4: foo
			bar
			baz
			TestIssue at test.file:1:2-3:4: message
			
		""".trimIndent()
	}

	private fun report(findings: List<Finding>): String {
		val output = StringBuilder()
		val subject = TextReporter(output)
		subject.report(findings)
		return output.toString()
	}

	private class TestRule : Rule {

		override val issues: List<Issue> = listOf(TestIssue)

		override fun check(file: File): List<Finding> =
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
}
