package net.twisterrob.ghlint.reporting

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.testing.testFinding
import org.junit.jupiter.api.Test

class TextReporterTest {

	private fun report(findings: List<Finding>): String {
		val output = StringBuilder()
		val subject = TextReporter(output)
		subject.report(findings)
		return output.toString()
	}

	@Test fun `single finding`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue),
		)
		report(findings) shouldBe """
			TestIssue at test.file:1:2-3:4: message
			
			For more information about this rule, run: ghlint --help TestIssue
			
		""".trimIndent()
	}

	@Test fun `full path is output`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue, file = "nested/test/path/to/file.name"),
		)
		report(findings) shouldBe """
			TestIssue at nested/test/path/to/file.name:1:2-3:4: message
			
			For more information about this rule, run: ghlint --help TestIssue
			
		""".trimIndent()
	}

	@Test fun `multiple findings same issue`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue),
			testFinding(TestRule(), TestRule.TestIssue),
			testFinding(TestRule(), TestRule.TestIssue),
		)
		report(findings) shouldBe """
			TestIssue at test.file:1:2-3:4: message
			TestIssue at test.file:1:2-3:4: message
			TestIssue at test.file:1:2-3:4: message
			
			For more information about this rule, run: ghlint --help TestIssue
			
		""".trimIndent()
	}

	@Test fun `multiple findings different issues`() {
		val findings = listOf(
			testFinding(TestRule(), TestRule.TestIssue),
			testFinding(TestRule(), TestRule.OtherIssue),
		)
		report(findings) shouldBe """
			TestIssue at test.file:1:2-3:4: message
			OtherIssue at test.file:1:2-3:4: message
			
			For more information about these rules, run: ghlint --help <RuleId>
			
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
			
			For more information about this rule, run: ghlint --help TestIssue
			
		""".trimIndent()
	}

	@Test fun `no findings`() {
		val findings = emptyList<Finding>()
		report(findings) shouldBe ""
	}

	private class TestRule : Rule {

		override val issues: List<Issue> = listOf(TestIssue, OtherIssue)

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

			val OtherIssue = Issue(
				id = "OtherIssue",
				title = "other title",
				description = "other description",
				compliant = emptyList(),
				nonCompliant = emptyList()
			)
		}
	}
}
