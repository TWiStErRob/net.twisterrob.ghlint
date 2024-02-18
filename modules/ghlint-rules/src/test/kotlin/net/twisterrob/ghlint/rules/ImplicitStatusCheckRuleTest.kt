package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ImplicitStatusCheckRuleTest {

	@TestFactory fun metadata() = test(ImplicitStatusCheckRule::class)

	@Nested
	inner class NeverUseAlwaysStepTest {

		@Test fun `passes when always is not in the condition`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: success() || failure()
				""".trimIndent()
			)

			result shouldHave noFindings()
		}

		@Test fun `passes when always is explicitly expressed`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: success() || failure() || cancelled()
				""".trimIndent()
			)

			result shouldHave noFindings()
		}

		@Test fun `fails when always is used`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: always()
				""".trimIndent()
			)

			result should haveFinding(
				"NeverUseAlways",
				"Step[#0] in Job[test] uses the always() condition."
			)
		}

		@Test fun `fails when always is used as part of a condition`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: github.context.value && (always() || failure())
				""".trimIndent()
			)

			result should haveFinding(
				"NeverUseAlways",
				"Step[#0] in Job[test] uses the always() condition."
			)
		}
	}

	@Nested
	inner class NeverUseAlwaysJobTest {

		@Test fun `passes when always is not in the condition`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    if: success() || failure()
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result shouldHave noFindings()
		}

		@Test fun `passes when always is explicitly expressed`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    if: success() || failure() || cancelled()
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result shouldHave noFindings()
		}

		@Test fun `fails when always is used`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    if: always()
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should haveFinding(
				"NeverUseAlways",
				"Job[test] uses the always() condition."
			)
		}

		@Test fun `fails when always is used as part of a condition`() {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    if: github.context.value && (always() || failure())
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should haveFinding(
				"NeverUseAlways",
				"Job[test] uses the always() condition."
			)
		}
	}

	@Nested
	inner class NegativeStatusCheckTest {

		@ParameterizedTest
		@ValueSource(strings = ["success", "failure", "cancelled", "always"])
		fun `fails when negative status check condition is used`(function: String) {
			val result = check<ImplicitStatusCheckRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
					        if: ${'$'}{{ ! ${function}() }}
				""".trimIndent()
			)

			result should haveFinding(
				"NegativeStatusCheck",
				"Step[#0] in Job[test] uses a negative condition."
			)
		}
	}
}
