package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MissingJobTimeoutRuleTest {

	@TestFactory fun metadata() = test(MissingJobTimeoutRule::class)

	@Test fun `passes when timeout is defined`() {
		val results = check<MissingJobTimeoutRule>(
			"""
				jobs:
				  test:
				    timeout-minutes: 5
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when timeout is defined as expression`() {
		val results = check<MissingJobTimeoutRule>(
			"""
				jobs:
				  test:
				    timeout-minutes: ${'$'}{{ inputs.timeout-minutes }}
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `fails when timeout is missing`() {
		val results = check<MissingJobTimeoutRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingJobTimeout",
			"Job[test] is missing `timeout-minutes`."
		)
	}

	@Test fun `fails when timeout is missing even when a step has timeout`() {
		val results = check<MissingJobTimeoutRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        timeout-minutes: 5
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingJobTimeout",
			"Job[test] is missing `timeout-minutes`."
		)
	}
}
