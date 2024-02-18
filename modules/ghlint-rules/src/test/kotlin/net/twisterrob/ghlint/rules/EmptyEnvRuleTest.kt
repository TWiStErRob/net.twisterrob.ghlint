package net.twisterrob.ghlint.rules

import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class EmptyEnvRuleTest {

	@TestFactory fun metadata() = test(EmptyEnvRule::class)

	@Test fun `passes when no env defined`() {
		val result = check<EmptyEnvRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `reports when workflow has empty env`() {
		val result = check<EmptyEnvRule>(
			"""
				env:
				jobs:
			""".trimIndent()
		)

		result shouldHave singleFinding(
			"EmptyWorkflowEnv",
			"Workflow[test] should not have empty env."
		)
	}

	@Test fun `reports when job has empty env`() {
		val result = check<EmptyEnvRule>(
			"""
				jobs:
				  test:
				    env:
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		result shouldHave singleFinding(
			"EmptyJobEnv",
			"Job[test] should not have empty env."
		)
	}

	@Test fun `reports when step has empty env`() {
		val result = check<EmptyEnvRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        env:
			""".trimIndent()
		)

		result shouldHave singleFinding(
			"EmptyStepEnv",
			"Step[#0] in Job[test] should not have empty env."
		)
	}
}
