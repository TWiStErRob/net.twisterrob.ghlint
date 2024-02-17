package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class DoubleCurlyIfRuleTest {

	@TestFactory fun metadata() = test(DoubleCurlyIfRule::class)

	@Nested
	inner class DoubleCurlyIfOnJobTest {

		@Test
		fun `passes even with lots of spacing`() {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    if:     ${'$'}{{     true     }}    
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should beEmpty()
		}

		@MethodSource(
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions",
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidButSyntaxErrorConditions"
		)
		@ParameterizedTest
		fun `passes wrapped condition`(condition: String) {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    if: ${'$'}{{ ${condition} }}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should beEmpty()

			val resultNoSpacing = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    if: ${'$'}{{${condition}}}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			resultNoSpacing should beEmpty()
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions")
		@ParameterizedTest
		fun `fails not fully wrapped condition`(condition: String) {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    if: ${condition}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should haveFinding(
				"DoubleCurlyIf",
				"Job[test] does not have double-curly-braces."
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getInvalidConditions")
		@ParameterizedTest
		fun `fails not strangely constructed condition`(condition: String) {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    if: ${condition}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should haveFinding(
				"DoubleCurlyIf",
				"Job[test] has nested or invalid double-curly-braces."
			)
		}
	}

	@Nested
	inner class DoubleCurlyIfOnStepTest {

		@Test
		fun `passes even with lots of spacing`() {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if:     ${'$'}{{     true     }}    
				""".trimIndent()
			)

			result should beEmpty()
		}

		@MethodSource(
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions",
			"net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidButSyntaxErrorConditions"
		)
		@ParameterizedTest
		fun `passes wrapped condition`(condition: String) {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    if: ${'$'}{{ ${condition} }}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should beEmpty()

			val resultNoSpacing = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: ${'$'}{{${condition}}}
				""".trimIndent()
			)

			resultNoSpacing should beEmpty()
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getValidConditions")
		@ParameterizedTest
		fun `fails not fully wrapped condition`(condition: String) {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: ${condition}
				""".trimIndent()
			)

			result should haveFinding(
				"DoubleCurlyIf",
				"Step[#0] in Job[test] does not have double-curly-braces."
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.DoubleCurlyIfRuleTest#getInvalidConditions")
		@ParameterizedTest
		fun `fails not strangely constructed condition`(condition: String) {
			val result = check<DoubleCurlyIfRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: ${condition}
				""".trimIndent()
			)

			result should haveFinding(
				"DoubleCurlyIf",
				"Step[#0] in Job[test] has nested or invalid double-curly-braces."
			)
		}
	}

	@MethodSource("getValidButSyntaxErrorConditions")
	@ParameterizedTest
	fun `test validation for syntax error`(condition: String) {
		val ex = assertThrows<RuntimeException> {
			check<DoubleCurlyIfRule>(
				"""
					if: ${condition}
				""".trimIndent()
			)
		}
		ex.printStackTrace()
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
