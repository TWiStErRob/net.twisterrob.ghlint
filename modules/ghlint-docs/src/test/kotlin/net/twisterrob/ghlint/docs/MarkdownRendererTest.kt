package net.twisterrob.ghlint.docs

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.ruleset.RuleSet
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path

class MarkdownRendererTest {

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("testCases")
	fun `issue renders markdown`(
		@Suppress("UnusedParameter", "UNUSED_PARAMETER") issueId: String,
		issue: Issue,
		expected: String,
		@TempDir temp: Path
	) {
		val renderer = MarkdownRenderer(FileLocator(temp))

		val markdown = renderer.renderIssue(TestRuleSet(), TestRule(), issue, emptyList())

		markdown shouldBe expected
	}

	companion object {

		@Suppress("detekt.LongMethod")
		@JvmStatic
		fun testCases(): List<Arguments> {
			fun testCase(issue: Issue, @Language("markdown") expected: String) = Arguments.of(issue.id, issue, expected)
			return listOf(
				testCase(
					TestRule.IssueNameWithoutExamples,
					"""
						# `IssueNameWithoutExamples`
						Issue without examples.
						
						_Defined by `TestRule` in the "[Test RuleSet](..\index.md)" ruleset._
						
						## Description
						Description of issue without examples.
						
					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithOnlyCompliantExample,
					"""
						# `IssueNameWithOnlyCompliantExample`
						Issue with only compliant example.
						
						_Defined by `TestRule` in the "[Test RuleSet](..\index.md)" ruleset._
						
						## Description
						Description of issue with only compliant example.
						
						## Compliant example
						```yaml
						name: "IssueNameWithOnlyCompliantExample compliant"
						on: push
						jobs: {}
						```
						Compliant example description.
						
					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithOnlyNonCompliantExample,
					"""
						# `IssueNameWithOnlyNonCompliantExample`
						Issue with only compliant example.
						
						_Defined by `TestRule` in the "[Test RuleSet](..\index.md)" ruleset._
						
						## Description
						Description of issue with only compliant example.
						
						## Non-compliant example
						```yaml
						name: "IssueNameWithOnlyNonCompliantExample non-compliant"
						on: push
						jobs: {}
						```
						Non-compliant example description.

					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithOneExampleEach,
					"""
						# `IssueNameWithOneExampleEach`
						Issue with one example each.
						
						_Defined by `TestRule` in the "[Test RuleSet](..\index.md)" ruleset._
						
						## Description
						Description of issue with one example each.
						
						## Compliant example
						```yaml
						name: "IssueNameWithOneExampleEach compliant"
						on: push
						jobs: {}
						```
						Compliant example description.
						
						## Non-compliant example
						```yaml
						name: "IssueNameWithOneExampleEach non-compliant"
						on: push
						jobs: {}
						```
						Non-compliant example description.
						
					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithManyExamples,
					"""
						# `IssueNameWithManyExamples`
						Issue with many examples.
						
						_Defined by `TestRule` in the "[Test RuleSet](..\index.md)" ruleset._
						
						## Description
						Description of issue with many examples.
						
						## Compliant examples
						
						### Compliant example #1
						```yaml
						name: "IssueNameWithManyExamples compliant 1"
						on: push
						jobs: {}
						```
						Compliant example 1 description.
						
						### Compliant example #2
						```yaml
						name: "IssueNameWithManyExamples compliant 2"
						on: push
						jobs: {}
						```
						Compliant example 2 description.
						
						### Compliant example #3
						```yaml
						name: "IssueNameWithManyExamples compliant 3"
						on: push
						jobs: {}
						```
						Compliant example 3 description.
						
						## Non-compliant examples
						
						### Non-compliant example #1
						```yaml
						name: "IssueNameWithManyExamples non-compliant 1"
						on: push
						jobs: {}
						```
						Non-compliant example 1 description.
						
						### Non-compliant example #2
						```yaml
						name: "IssueNameWithManyExamples non-compliant 2"
						on: push
						jobs: {}
						```
						Non-compliant example 2 description.
						
					""".trimIndent(),
				),
			)
		}
	}
}

private class TestRuleSet(
	override val id: String = "test-ruleset",
	override val name: String = "Test RuleSet",
) : RuleSet by ReflectiveRuleSet(id, name, TestRule::class)

private class TestRule : Rule {

	override val issues: List<Issue> = listOf(IssueNameWithoutExamples)

	override fun check(workflow: Workflow): List<Finding> {
		TODO("Implement, or extend VisitorRule and override a function.")
	}

	companion object {

		val IssueNameWithoutExamples = Issue(
			id = "IssueNameWithoutExamples",
			title = "Issue without examples.",
			description = """
				Description of issue without examples.
			""".trimIndent(),
			compliant = emptyList(),
			nonCompliant = emptyList(),
		)

		val IssueNameWithOnlyCompliantExample = Issue(
			id = "IssueNameWithOnlyCompliantExample",
			title = "Issue with only compliant example.",
			description = """
				Description of issue with only compliant example.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Compliant example description.",
					content = """
						name: "IssueNameWithOnlyCompliantExample compliant"
						on: push
						jobs: {}
					""".trimIndent(),
				),
			),
			nonCompliant = emptyList(),
		)

		val IssueNameWithOnlyNonCompliantExample = Issue(
			id = "IssueNameWithOnlyNonCompliantExample",
			title = "Issue with only compliant example.",
			description = """
				Description of issue with only compliant example.
			""".trimIndent(),
			compliant = emptyList(),
			nonCompliant = listOf(
				Example(
					explanation = "Non-compliant example description.",
					content = """
						name: "IssueNameWithOnlyNonCompliantExample non-compliant"
						on: push
						jobs: {}
					""".trimIndent(),
				),
			),
		)

		val IssueNameWithOneExampleEach = Issue(
			id = "IssueNameWithOneExampleEach",
			title = "Issue with one example each.",
			description = """
				Description of issue with one example each.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Compliant example description.",
					content = """
						name: "IssueNameWithOneExampleEach compliant"
						on: push
						jobs: {}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Non-compliant example description.",
					content = """
						name: "IssueNameWithOneExampleEach non-compliant"
						on: push
						jobs: {}
					""".trimIndent(),
				),
			),
		)

		val IssueNameWithManyExamples = Issue(
			id = "IssueNameWithManyExamples",
			title = "Issue with many examples.",
			description = """
				Description of issue with many examples.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Compliant example 1 description.",
					content = """
						name: "IssueNameWithManyExamples compliant 1"
						on: push
						jobs: {}
					""".trimIndent(),
				),
				Example(
					explanation = "Compliant example 2 description.",
					content = """
						name: "IssueNameWithManyExamples compliant 2"
						on: push
						jobs: {}
					""".trimIndent(),
				),
				Example(
					explanation = "Compliant example 3 description.",
					content = """
						name: "IssueNameWithManyExamples compliant 3"
						on: push
						jobs: {}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Non-compliant example 1 description.",
					content = """
						name: "IssueNameWithManyExamples non-compliant 1"
						on: push
						jobs: {}
					""".trimIndent(),
				),
				Example(
					explanation = "Non-compliant example 2 description.",
					content = """
						name: "IssueNameWithManyExamples non-compliant 2"
						on: push
						jobs: {}
					""".trimIndent(),
				),
			),
		)
	}
}
