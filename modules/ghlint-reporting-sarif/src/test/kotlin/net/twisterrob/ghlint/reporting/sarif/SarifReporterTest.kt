package net.twisterrob.ghlint.reporting.sarif

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.GHLINT_VERSION
import net.twisterrob.ghlint.test.readResourceText
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.LazyRuleSet
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

/**
 * @see SarifReporter
 */
class SarifReporterTest {

	@Test fun `test normal path`(@TempDir temp: Path) {
		val resolvedTemp = temp.toRealPath(LinkOption.NOFOLLOW_LINKS)
		test(resolvedTemp)
	}

	@Test fun `test symlinked path`(@TempDir temp: Path) {
		val resolvedTemp = temp.toRealPath(LinkOption.NOFOLLOW_LINKS)
		val real = resolvedTemp.resolve("real")
		val link = resolvedTemp.resolve("symlink")
		Files.createSymbolicLink(link, real.createDirectories())

		test(link)
	}

	private fun test(root: Path) {
		val writer = StringWriter()

		val testRuleSet = LazyRuleSet(
			id = "test-ruleset",
			name = "Test RuleSet",
			::IntegrationTestRule,
		)
		val file = root.resolve("test.yml").createFile()
		val workflow = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
			fileName = file.toString(),
		)
		val findings = check<IntegrationTestRule>(workflow)

		SarifReporter(writer, root, listOf(testRuleSet)).report(findings)

		writer.toString() shouldBe sarifReport(root, "report.sarif.json")
	}
}

private fun sarifReport(root: Path, resourcePath: String): String =
	SarifReporterTest::class.java.readResourceText(resourcePath)
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
