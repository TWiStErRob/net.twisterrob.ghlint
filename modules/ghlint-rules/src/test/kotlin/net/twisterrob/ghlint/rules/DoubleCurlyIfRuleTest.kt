package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class DoubleCurlyIfRuleTest {

	@TestFactory fun metadata() = test(DoubleCurlyIfRule::class)

	@Nested
	inner class DoubleCurlyIfOnJobTest {

		@Test
		fun `passes even with lots of spacing`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if:     ${'$'}{{     true     }}    
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave noFindings()
		}

		@MethodSource(
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions",
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidButSyntaxErrorConditions"
		)
		@ParameterizedTest
		fun `passes wrapped condition`(condition: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${'$'}{{ ${condition} }}
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave noFindings()

			val fileNoSpacing = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${'$'}{{${condition}}}
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val resultsNoSpacing = check<DoubleCurlyIfRule>(fileNoSpacing)

			resultsNoSpacing shouldHave noFindings()
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions")
		@ParameterizedTest
		fun `fails not fully wrapped condition`(condition: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${condition}
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave singleFinding(
				issue = "DoubleCurlyIf",
				message = "Job[test] does not have double-curly-braces.",
				location = file("test"),
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getInvalidConditions")
		@ParameterizedTest
		fun `fails not strangely constructed condition`(condition: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${condition}
					    steps:
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave singleFinding(
				issue = "DoubleCurlyIf",
				message = "Job[test] has nested or invalid double-curly-braces.",
				location = file("test"),
			)
		}
	}

	@Nested
	inner class DoubleCurlyIfOnStepTest {

		@Test
		fun `passes even with lots of spacing`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if:     ${'$'}{{     true     }}    
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave noFindings()
		}

		@MethodSource(
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions",
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidButSyntaxErrorConditions"
		)
		@ParameterizedTest
		fun `passes wrapped condition`(condition: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${'$'}{{ ${condition} }}
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave noFindings()

			val fileNoSpacing = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${'$'}{{${condition}}}
				""".trimIndent(),
			)

			val resultsNoSpacing = check<DoubleCurlyIfRule>(fileNoSpacing)

			resultsNoSpacing shouldHave noFindings()
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions")
		@ParameterizedTest
		fun `fails not fully wrapped condition`(condition: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${condition}
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave singleFinding(
				issue = "DoubleCurlyIf",
				message = "Step[#0] in Job[test] does not have double-curly-braces.",
				location = file("-", 2),
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getInvalidConditions")
		@ParameterizedTest
		fun `fails not strangely constructed condition`(condition: String) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${condition}
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave singleFinding(
				issue = "DoubleCurlyIf",
				message = "Step[#0] in Job[test] has nested or invalid double-curly-braces.",
				location = file("-", 2),
			)
		}
	}

	@Nested
	inner class DoubleCurlyIfOnActionStepTest {

		@Test
		fun `passes even with lots of spacing`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if:     ${'$'}{{     true     }}    
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave noFindings()
		}

		@MethodSource(
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions",
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidButSyntaxErrorConditions"
		)
		@ParameterizedTest
		fun `passes wrapped condition`(condition: String) {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: ${'$'}{{ ${condition} }}
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave noFindings()

			val fileNoSpacing = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					      - run: echo "Test"
					        shell: bash
					        if: ${'$'}{{${condition}}}
				""".trimIndent(),
			)

			val resultsNoSpacing = check<DoubleCurlyIfRule>(fileNoSpacing)

			resultsNoSpacing shouldHave noFindings()
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions")
		@ParameterizedTest
		fun `fails not fully wrapped condition`(condition: String) {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: >
					        ${condition}
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave singleFinding(
				issue = "DoubleCurlyIf",
				message = """Step[#0] in Action["Test"] does not have double-curly-braces.""",
				location = file("-"),
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getInvalidConditions")
		@ParameterizedTest
		fun `fails not strangely constructed condition`(condition: String) {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      if: ${condition}
				""".trimIndent(),
			)

			val results = check<DoubleCurlyIfRule>(file)

			results shouldHave singleFinding(
				issue = "DoubleCurlyIf",
				message = """Step[#0] in Action["Test"] has nested or invalid double-curly-braces.""",
				location = file("-"),
			)
		}
	}

	companion object {

		@JvmStatic
		val invalidConditions = listOf(
			"${'$'}{{ github.context.variable }} == 'test'",
			"123 == ${'$'}{{ github.context.variable }}",
			"null == ${'$'}{{ github.context.variable }}",
			"${'$'}{{ github.context.variable }} == ${'$'}{{ github.other.var }}",
			"${'$'}{{ github.context.variable }} && ${'$'}{{ github.other.var }} || ${'$'}{{ github.yet.other.var }}",
		)

		@JvmStatic
		val validConditions = listOf(
			"true",
			"true || false",
			"github.context.variable == 'bbb'",
			"github.context.variable == \"bbb\"",
			"123 == github.context.variable",
			"true == github.context.variable",
			"! github.invalid.yaml",
		)

		@JvmStatic
		val validButSyntaxErrorConditions = listOf(
			"'aaa' == 'bbb'",
			@Suppress("detekt.StringShouldBeRawString")
			"\"aaa\" == \"bbb\"",
			"'aaa' == github.context.variable",
			"\"aaa\" == github.context.variable",
			"!! github.invalid.yaml",
		)
	}
}
