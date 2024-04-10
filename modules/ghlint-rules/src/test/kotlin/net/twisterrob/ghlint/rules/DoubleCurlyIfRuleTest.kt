package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import io.kotest.matchers.throwable.shouldHaveMessage
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

class DoubleCurlyIfRuleTest {

	@TestFactory fun metadata() = test(DoubleCurlyIfRule::class)

	@Nested
	inner class DoubleCurlyIfOnJobTest {

		@Test
		fun `passes even with lots of spacing`() {
			val results = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
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
					jobs:
					  test:
					    if: ${'$'}{{ ${condition} }}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave noFindings()

			val resultsNoSpacing = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
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
					jobs:
					  test:
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
					jobs:
					  test:
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
					jobs:
					  test:
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
					jobs:
					  test:
					    if: ${'$'}{{ ${condition} }}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave noFindings()

			val resultsNoSpacing = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
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
					jobs:
					  test:
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
					jobs:
					  test:
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

	@MethodSource("getValidButSyntaxErrorConditions")
	@ParameterizedTest
	fun `test validation for syntax error`(condition: String) {
		val rule = spy<DoubleCurlyIfRule>()
		doAnswer { throw it.getArgument(1, InvalidContent::class.java).error }
			.whenever(rule)
			.visitInvalidContent(any(), any())
		val ex = assertThrows<RuntimeException> {
			rule.check(
				"""
					if: ${condition}
				""".trimIndent()
			)
		}
		ex shouldHaveMessage """(?s)^Failed to parse YAML: .*\Q${condition}\E$""".toRegex()
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
			"null",
			"github.context.variable == 'bbb'",
			"github.context.variable == \"bbb\"",
			"123 == github.context.variable",
			"true == github.context.variable",
			"! github.invalid.yaml",
			"!!github.invalid.yaml",
		)

		@JvmStatic
		val validButSyntaxErrorConditions = listOf(
			"'aaa' == 'bbb'",
			@Suppress("detekt.StringShouldBeRawString")
			"\"aaa\" == \"bbb\"",
			"'aaa' == github.context.variable",
			"\"aaa\" == github.context.variable",
		)
	}
}
