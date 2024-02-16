package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MissingJobTimeoutRuleTest {

	@TestFactory fun metadata() = test(MissingJobTimeoutRule::class)

	@Test fun `passes when timeout is defined`() {
		val result = check<MissingJobTimeoutRule>(
			"""
				jobs:
				  test:
				    timeout-minutes: 5
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `fails when timeout is missing`() {
		val result = check<MissingJobTimeoutRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		result should haveFinding(
			"MissingJobTimeout",
			"Job[test] is missing `timeout-minutes`."
		)
	}

	@Test fun `fails when timeout is missing even when a step has timeout`() {
		val result = check<MissingJobTimeoutRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        timeout-minutes: 5
			""".trimIndent()
		)

		result should haveFinding(
			"MissingJobTimeout",
			"Job[test] is missing `timeout-minutes`."
		)
	}
}
