package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class EmptyEnvRuleTest {

	@TestFactory fun metadata() = test(EmptyEnvRule::class)

	@Test fun `passes when no env defined`() {
		val results = check<EmptyEnvRule>(
			"""
				jobs:
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when workflow has empty env`() {
		val results = check<EmptyEnvRule>(
			"""
				env:
				jobs:
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"EmptyWorkflowEnv",
			"Workflow[test] should not have empty env."
		)
	}

	@Test fun `reports when job has empty env`() {
		val results = check<EmptyEnvRule>(
			"""
				jobs:
				  test:
				    env:
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"EmptyJobEnv",
			"Job[test] should not have empty env."
		)
	}

	@Test fun `reports when step has empty env`() {
		val results = check<EmptyEnvRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Test"
				        env:
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"EmptyStepEnv",
			"Step[#0] in Job[test] should not have empty env."
		)
	}
}
