package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MissingJobTimeoutRuleTest {

	@TestFactory fun metadata() = test(MissingJobTimeoutRule::class)

	@Test fun `passes when timeout is defined`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    timeout-minutes: 5
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingJobTimeoutRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when timeout is defined as expression`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    timeout-minutes: ${'$'}{{ inputs.timeout-minutes }}
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingJobTimeoutRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `fails when timeout is missing`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingJobTimeoutRule>(file)

		results shouldHave singleFinding(
			issue = "MissingJobTimeout",
			message = "Job[test] is missing `timeout-minutes`.",
			location = file("test"),
		)
	}

	@Test fun `fails when timeout is missing even when a step has timeout`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        timeout-minutes: 5
			""".trimIndent(),
		)

		val results = check<MissingJobTimeoutRule>(file)

		results shouldHave singleFinding(
			issue = "MissingJobTimeout",
			message = "Job[test] is missing `timeout-minutes`.",
			location = file("test"),
		)
	}
}
