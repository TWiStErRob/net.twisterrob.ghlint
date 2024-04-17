package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
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
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if:     ${'$'}{{     true     }}    
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@MethodSource(
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions",
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidButSyntaxErrorConditions"
		)
		@ParameterizedTest
		fun `passes wrapped condition`(condition: String) {
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${'$'}{{ ${condition} }}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave noFindings()

			val resultsNoSpacing = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${'$'}{{${condition}}}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			resultsNoSpacing shouldHave noFindings()
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions")
		@ParameterizedTest
		fun `fails not fully wrapped condition`(condition: String) {
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${condition}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"DoubleCurlyIf",
				"Job[test] does not have double-curly-braces."
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getInvalidConditions")
		@ParameterizedTest
		fun `fails not strangely constructed condition`(condition: String) {
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    if: ${condition}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"DoubleCurlyIf",
				"Job[test] has nested or invalid double-curly-braces."
			)
		}
	}

	@Nested
	inner class DoubleCurlyIfOnStepTest {

		@Test
		fun `passes even with lots of spacing`() {
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if:     ${'$'}{{     true     }}    
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@MethodSource(
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions",
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidButSyntaxErrorConditions"
		)
		@ParameterizedTest
		fun `passes wrapped condition`(condition: String) {
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${'$'}{{ ${condition} }}
				""".trimIndent()
			)

			results shouldHave noFindings()

			val resultsNoSpacing = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${'$'}{{${condition}}}
				""".trimIndent()
			)

			resultsNoSpacing shouldHave noFindings()
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions")
		@ParameterizedTest
		fun `fails not fully wrapped condition`(condition: String) {
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${condition}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"DoubleCurlyIf",
				"Step[#0] in Job[test] does not have double-curly-braces."
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getInvalidConditions")
		@ParameterizedTest
		fun `fails not strangely constructed condition`(condition: String) {
			val results = check<DoubleCurlyIfRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        if: ${condition}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"DoubleCurlyIf",
				"Step[#0] in Job[test] has nested or invalid double-curly-braces."
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
