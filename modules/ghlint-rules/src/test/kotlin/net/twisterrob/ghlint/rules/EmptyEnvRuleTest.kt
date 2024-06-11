package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class EmptyEnvRuleTest {

	@TestFactory fun metadata() = test(EmptyEnvRule::class)

	@Test fun `passes when no env defined`() {
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

		val results = check<EmptyEnvRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when workflow has dynamic env`() {
		val file = workflow(
			"""
				on: push
				env: ${'$'}{{ {} }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when workflow has empty env`() {
		val file = workflow(
			"""
				on: push
				env: {}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave singleFinding(
			issue = "EmptyWorkflowEnv",
			message = "Workflow[test] should not have empty env.",
			location = file("jobs"),
		)
	}

	@Test fun `passes when job has dynamic env`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env: ${'$'}{{ {} }}
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when job has empty env`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env: {}
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave singleFinding(
			issue = "EmptyJobEnv",
			message = "Job[test] should not have empty env.",
			location = file("test"),
		)
	}

	@Test fun `passes when step has dynamic env`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        env: ${'$'}{{ {} }}
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when step has empty env`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: some/action@v1
				        env: {}
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave singleFinding(
			issue = "EmptyStepEnv",
			message = "Step[some/action@v1] in Job[test] should not have empty env.",
			location = file("-", 2),
		)
	}

	@Test fun `reports when run step has empty env`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        env: {}
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave singleFinding(
			issue = "EmptyStepEnv",
			message = "Step[#0] in Job[test] should not have empty env.",
			location = file("-", 2),
		)
	}

	@Test fun `passes when step has dynamic env in action`() {
		val file = action(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: some/action@v1
				      env: ${'$'}{{ {} }}
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when step has empty env in action`() {
		val file = action(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: some/action@v1
				      env: {}
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave singleFinding(
			issue = "EmptyStepEnv",
			message = """Step[some/action@v1] in Action["Test"] should not have empty env.""",
			location = file("-"),
		)
	}

	@Test fun `reports when run step has empty env in action`() {
		val file = action(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: echo "Test"
				      shell: bash 
				      env: {}
			""".trimIndent(),
		)

		val results = check<EmptyEnvRule>(file)

		results shouldHave singleFinding(
			issue = "EmptyStepEnv",
			message = """Step[#0] in Action["Test"] should not have empty env.""",
			location = file("-"),
		)
	}
}
