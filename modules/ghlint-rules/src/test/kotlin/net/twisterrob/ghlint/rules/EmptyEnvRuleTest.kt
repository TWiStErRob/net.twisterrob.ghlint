package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
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

		result should haveFinding(
			"EmptyWorkflowEnv",
			"Workflow[test.yml] should not have empty env."
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

		result should haveFinding(
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

		result should haveFinding(
			"EmptyStepEnv",
			"Step[#0] in Job[test] should not have empty env."
		)
	}
}
