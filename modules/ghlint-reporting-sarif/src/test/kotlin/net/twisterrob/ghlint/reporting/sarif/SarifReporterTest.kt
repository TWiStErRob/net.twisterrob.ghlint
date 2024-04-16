package net.twisterrob.ghlint.reporting.sarif

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.GHLINT_VERSION
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.testing.check
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.StringWriter
import java.nio.file.Path

class SarifReporterTest {

	@Test fun test(@TempDir temp: Path) {
		val writer = StringWriter()

		val testRuleSet = ReflectiveRuleSet(
			id = "test-ruleset",
			name = "Test RuleSet",
			IntegrationTestRule::class
		)
		val findings = check<IntegrationTestRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
			fileName = temp.resolve("test.yml").toString(),
		)
		SarifReporter(writer, temp, listOf(testRuleSet)).report(findings)

		writer.toString() shouldBe sarifReport(temp, "report.sarif.json")
	}
}

private fun testResource(path: String): String {
	val stream = SarifReporterTest::class.java.getResourceAsStream(path) ?: error("Cannot find ${path}")
	return stream.use { it.reader().readText() }
}

private fun sarifReport(root: Path, resourcePath: String): String =
	testResource(resourcePath)
		.replace("<version>", GHLINT_VERSION)
		.replace("<root>", root.toUri().toString())

internal class IntegrationTestRule : Rule {

	override val issues: List<Issue> = listOf(TestIssue)

	override fun check(file: File): List<Finding> {
		val workflow = file.content as Workflow
		val job = workflow.jobs["test"] ?: return emptyList()
		return listOf(
			Finding(
				rule = this,
				issue = TestIssue,
				message = "Test Finding message",
				location = job.location,
			)
		)
	}

	companion object {

		val TestIssue = Issue(
			id = "TestIssueId",
			title = "Test Issue Title",
			description = """
				Test issue description.
				
				Long explanation of the issue.
				This rule actually finds a job named test and flags it.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Compliant example explanation for `job`.",
					content = """
						on:
						jobs:
						  job:
						    steps: []
					""".trimIndent()
				),
				Example(
					explanation = "Compliant example explanation for `example`.",
					content = """
						on:
						jobs:
						  example:
						    steps: []
					""".trimIndent()
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Non-compliant example explanation.",
					content = """
						on:
						jobs:
						  test:
						    steps: []
					""".trimIndent(),
				),
			),
		)
	}
}
