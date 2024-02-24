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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.nio.file.Path

@Suppress("detekt.NamedArguments")
class MarkdownRendererTest {

	@Nested
	inner class `RuleSet rendering` {

		private inner class NoIssuesTestRule : Rule {

			override val issues: List<Issue> = emptyList()
			override fun check(workflow: Workflow): List<Finding> = error("Should never be called.")
		}

		private inner class OneIssueTestRule : Rule {

			override val issues: List<Issue> = listOf(
				TestRule.IssueNameWithManyExamples,
			)

			override fun check(workflow: Workflow): List<Finding> = error("Should never be called.")
		}

		private inner class ManyIssuesTestRule : Rule {

			override val issues: List<Issue> = listOf(
				TestRule.IssueNameWithManyExamples,
				TestRule.IssueNameWithOneExampleEach,
				TestRule.IssueNameWithoutExamples
			)

			override fun check(workflow: Workflow): List<Finding> = error("Should never be called.")
		}

		@Test fun `rule set renders with no rules`(@TempDir temp: Path) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val ruleSet: RuleSet = mock()
			whenever(ruleSet.id).thenReturn("test-ruleset")
			whenever(ruleSet.name).thenReturn("Test RuleSet")
			whenever(ruleSet.createRules()).thenReturn(emptyList())

			val markdown = renderer.renderRuleSet(ruleSet)

			markdown shouldBe """
				# Rule set "Test RuleSet" (`test-ruleset`)
				
				No rules.
				
			""".trimIndent()
		}

		@Test fun `rule renders with no issues`(@TempDir temp: Path) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val ruleSet: RuleSet = mock()
			whenever(ruleSet.id).thenReturn("test-ruleset")
			whenever(ruleSet.name).thenReturn("Test RuleSet")

			whenever(ruleSet.createRules()).thenReturn(listOf(NoIssuesTestRule()))

			val markdown = renderer.renderRuleSet(ruleSet)

			markdown shouldBe """
				# Rule set "Test RuleSet" (`test-ruleset`)
				
				 - `NoIssuesTestRule`
				
			""".trimIndent()
		}

		@Test fun `rule renders with one issue`(@TempDir temp: Path) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val ruleSet: RuleSet = mock()
			whenever(ruleSet.id).thenReturn("test-ruleset")
			whenever(ruleSet.name).thenReturn("Test RuleSet")

			whenever(ruleSet.createRules()).thenReturn(listOf(OneIssueTestRule()))

			val markdown = renderer.renderRuleSet(ruleSet)

			markdown shouldBe """
				# Rule set "Test RuleSet" (`test-ruleset`)
				
				 - `OneIssueTestRule`
				    - [`IssueNameWithManyExamples`](IssueNameWithManyExamples.md): Issue with many examples.
				
			""".trimIndent()
		}

		@Test fun `rule renders with multiple issues`(@TempDir temp: Path) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val ruleSet: RuleSet = mock()
			whenever(ruleSet.id).thenReturn("test-ruleset")
			whenever(ruleSet.name).thenReturn("Test RuleSet")
			whenever(ruleSet.createRules()).thenReturn(listOf(ManyIssuesTestRule()))

			val markdown = renderer.renderRuleSet(ruleSet)

			markdown shouldBe """
				# Rule set "Test RuleSet" (`test-ruleset`)
				
				 - `ManyIssuesTestRule`
				    - [`IssueNameWithManyExamples`](IssueNameWithManyExamples.md): Issue with many examples.
				    - [`IssueNameWithOneExampleEach`](IssueNameWithOneExampleEach.md): Issue with one example each.
				    - [`IssueNameWithoutExamples`](IssueNameWithoutExamples.md): Issue without examples.
				
			""".trimIndent()
		}

		@Test fun `rule set renders with multiple rules`(@TempDir temp: Path) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val ruleSet: RuleSet = mock()
			whenever(ruleSet.id).thenReturn("test-ruleset")
			whenever(ruleSet.name).thenReturn("Test RuleSet")
			whenever(ruleSet.createRules()).thenReturn(
				listOf(
					OneIssueTestRule(),
					NoIssuesTestRule(),
					ManyIssuesTestRule()
				)
			)

			val markdown = renderer.renderRuleSet(ruleSet)

			markdown shouldBe """
				# Rule set "Test RuleSet" (`test-ruleset`)
				
				 - `ManyIssuesTestRule`
				    - [`IssueNameWithManyExamples`](IssueNameWithManyExamples.md): Issue with many examples.
				    - [`IssueNameWithOneExampleEach`](IssueNameWithOneExampleEach.md): Issue with one example each.
				    - [`IssueNameWithoutExamples`](IssueNameWithoutExamples.md): Issue without examples.
				 - `NoIssuesTestRule`
				 - `OneIssueTestRule`
				    - [`IssueNameWithManyExamples`](IssueNameWithManyExamples.md): Issue with many examples.
				
			""".trimIndent()
		}
	}

	@Nested
	inner class `Issue rendering` {

		@ParameterizedTest(name = "[{index}] {0}")
		@MethodSource("net.twisterrob.ghlint.docs.MarkdownRendererTest#testCases")
		fun `issue renders markdown`(
			@Suppress("detekt.UnusedParameter", "UNUSED_PARAMETER") issueId: String,
			issue: Issue,
			expected: String,
			@TempDir temp: Path
		) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val markdown = renderer.renderIssue(TestRuleSet(), TestRule(), issue, emptyList())

			markdown shouldBe expected
		}

		@Test fun `related issue is listed`(@TempDir temp: Path) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val relatedIssues = listOf(TestRule.IssueNameWithOneExampleEach)
			val markdown =
				renderer.renderIssue(TestRuleSet(), TestRule(), TestRule.IssueNameWithoutExamples, relatedIssues)

			markdown shouldBe """
				# `IssueNameWithoutExamples`
				Issue without examples.
				
				_Defined by `TestRule` in the "[Test RuleSet](index.md)" ruleset along with [`IssueNameWithOneExampleEach`](IssueNameWithOneExampleEach.md)._
				
				## Description
				Description of issue without examples.
				
			""".trimIndent()
		}

		@Test fun `related issues are listed`(@TempDir temp: Path) {
			val renderer = MarkdownRenderer(FileLocator(temp))

			val relatedIssues = listOf(
				TestRule.IssueNameWithOnlyCompliantExample,
				TestRule.IssueNameWithOnlyNonCompliantExample,
				TestRule.IssueNameWithOneExampleEach
			)
			val markdown =
				renderer.renderIssue(TestRuleSet(), TestRule(), TestRule.IssueNameWithoutExamples, relatedIssues)

			markdown shouldBe """
				# `IssueNameWithoutExamples`
				Issue without examples.
				
				_Defined by `TestRule` in the "[Test RuleSet](index.md)" ruleset along with [`IssueNameWithOnlyCompliantExample`](IssueNameWithOnlyCompliantExample.md), [`IssueNameWithOnlyNonCompliantExample`](IssueNameWithOnlyNonCompliantExample.md), [`IssueNameWithOneExampleEach`](IssueNameWithOneExampleEach.md)._
				
				## Description
				Description of issue without examples.
				
			""".trimIndent()
		}
	}

	companion object {

		@Suppress("detekt.LongMethod")
		@JvmStatic
		fun testCases(): List<Arguments> {
			fun testCase(issue: Issue, @Language("markdown") expected: String): Arguments =
				Arguments.of(issue.id, issue, expected)

			return listOf(
				testCase(
					TestRule.IssueNameWithoutExamples,
					"""
						# `IssueNameWithoutExamples`
						Issue without examples.
						
						_Defined by `TestRule` in the "[Test RuleSet](index.md)" ruleset._
						
						## Description
						Description of issue without examples.
						
					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithOnlyCompliantExample,
					"""
						# `IssueNameWithOnlyCompliantExample`
						Issue with only compliant example.
						
						_Defined by `TestRule` in the "[Test RuleSet](index.md)" ruleset._
						
						## Description
						Description of issue with only compliant example.
						
						## Compliant example
						Compliant example description.
						
						> ```yaml
						> name: "IssueNameWithOnlyCompliantExample compliant"
						> on: push
						> jobs: {}
						> ```
						
					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithOnlyNonCompliantExample,
					"""
						# `IssueNameWithOnlyNonCompliantExample`
						Issue with only compliant example.
						
						_Defined by `TestRule` in the "[Test RuleSet](index.md)" ruleset._
						
						## Description
						Description of issue with only compliant example.
						
						## Non-compliant example
						Non-compliant example description.
						
						> ```yaml
						> name: "IssueNameWithOnlyNonCompliantExample non-compliant"
						> on: push
						> jobs: {}
						> ```
						>
						> - **Line 3**: Non-compliant `workflow`.
						
					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithOneExampleEach,
					"""
						# `IssueNameWithOneExampleEach`
						Issue with one example each.
						
						_Defined by `TestRule` in the "[Test RuleSet](index.md)" ruleset._
						
						## Description
						Description of issue with one example each.
						
						## Compliant example
						Compliant example description.
						
						> ```yaml
						> name: "IssueNameWithOneExampleEach compliant"
						> on: push
						> jobs: {}
						> ```
						
						## Non-compliant example
						Non-compliant example description.
						
						> ```yaml
						> name: "IssueNameWithOneExampleEach non-compliant"
						> on: push
						> jobs: {}
						> ```
						>
						> - **Line 3**: Non-compliant `workflow`.
						
					""".trimIndent(),
				),
				testCase(
					TestRule.IssueNameWithManyExamples,
					"""
						# `IssueNameWithManyExamples`
						Issue with many examples.
						
						_Defined by `TestRule` in the "[Test RuleSet](index.md)" ruleset._
						
						## Description
						Description of issue with many examples.
						
						## Compliant examples
						
						### Compliant example #1
						Compliant example 1 description.
						
						> ```yaml
						> name: "IssueNameWithManyExamples compliant 1"
						> on: push
						> jobs: {}
						> ```
						
						### Compliant example #2
						Compliant example 2 description.
						
						> ```yaml
						> name: "IssueNameWithManyExamples compliant 2"
						> on: push
						> jobs: {}
						> ```
						
						### Compliant example #3
						Compliant example 3 description.
						
						> ```yaml
						> name: "IssueNameWithManyExamples compliant 3"
						> on: push
						> jobs: {}
						> ```
						
						## Non-compliant examples
						
						### Non-compliant example #1
						Non-compliant example 1 description.
						
						> ```yaml
						> name: "IssueNameWithManyExamples non-compliant 1"
						> on: push
						> jobs: {}
						> ```
						>
						> - **Line 3**: Non-compliant `workflow`.
						
						### Non-compliant example #2
						Non-compliant example 2 description.
						
						> ```yaml
						> name: "IssueNameWithManyExamples non-compliant 2"
						> on: push
						> jobs: {}
						> ```
						>
						> - **Line 3**: Non-compliant `workflow`.
						
					""".trimIndent(),
				),
			)
		}
	}
}

internal class TestRuleSet(
	override val id: String = "test-ruleset",
	override val name: String = "Test RuleSet",
) : RuleSet by ReflectiveRuleSet(id, name, TestRule::class)

internal class TestRule : Rule {

	override val issues: List<Issue> get() = error("Should never be called.")
	override fun check(workflow: Workflow): List<Finding> =
		if (workflow.name.orEmpty().contains("non-compliant")) {
			listOf(Finding(this, IssueNameWithoutExamples, workflow.location, "Non-compliant `workflow`."))
		} else {
			emptyList()
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
