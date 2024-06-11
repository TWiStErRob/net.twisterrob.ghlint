package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class IdNamingRuleTest {

	@TestFactory fun metadata() = test(IdNamingRule::class)

	@Test fun `passes when no step id`() {
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

		val results = check<IdNamingRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when no step id in action`() {
		val file = action(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: echo "Test"
				      shell: bash
			""".trimIndent(),
		)

		val results = check<IdNamingRule>(file)

		results shouldHave noFindings()
	}

	@ParameterizedTest
	@MethodSource("getLowerKebabIds")
	fun `passes when workflow id is lower kebab`(id: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
			fileName = "${id}.yml",
		)

		val results = check<IdNamingRule>(file)

		results shouldHave noFindings()
	}

	@ParameterizedTest
	@MethodSource("getLowerKebabIds")
	fun `passes when job id is lower kebab`(id: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  ${id}:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<IdNamingRule>(file)

		results shouldHave noFindings()
	}

	@ParameterizedTest
	@MethodSource("getLowerKebabIds")
	fun `passes when step id is lower kebab`(id: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - id: ${id}
				        run: echo "Test"
			""".trimIndent(),
			fileName = "${id}.yml",
		)

		val results = check<IdNamingRule>(file)

		results shouldHave noFindings()
	}

	@ParameterizedTest
	@MethodSource("getLowerKebabIds")
	fun `passes when step id is lower kebab in action`(id: String) {
		val file = action(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - id: ${id}
				      run: echo "Test"
				      shell: bash
			""".trimIndent(),
		)

		val results = check<IdNamingRule>(file)

		results shouldHave noFindings()
	}

	@ParameterizedTest
	@MethodSource("getNonLowerKebabIds")
	fun `reports when workflow id is not lower kebab`(id: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
			fileName = "${id}.yml",
		)

		val results = check<IdNamingRule>(file)

		results shouldHave singleFinding(
			"WorkflowIdNaming",
			"""Workflow[${id}] should have a lower-case kebab ID."""
		)
	}

	@ParameterizedTest
	@MethodSource("getNonLowerKebabIds")
	fun `reports when job id is not lower kebab`(id: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  ${id}:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<IdNamingRule>(file)

		results shouldHave singleFinding(
			"JobIdNaming",
			"""Job[${id}] should have a lower-case kebab ID."""
		)
	}

	@ParameterizedTest
	@MethodSource("getNonLowerKebabIds")
	fun `reports when step id is not lower kebab`(id: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - id: ${id}
				        run: echo "Test"
			""".trimIndent(),
		)

		val results = check<IdNamingRule>(file)

		results shouldHave singleFinding(
			"StepIdNaming",
			"""Step[${id}] in Job[test] should have a lower-case kebab ID."""
		)
	}

	@ParameterizedTest
	@MethodSource("getNonLowerKebabIds")
	fun `reports when step id is not lower kebab in action`(id: String) {
		val file = action(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - id: ${id}
				      run: echo "Test"
				      shell: bash
			""".trimIndent(),
		)

		val results = check<IdNamingRule>(file)

		results shouldHave singleFinding(
			"StepIdNaming",
			"""Step[${id}] in Action["Test"] should have a lower-case kebab ID."""
		)
	}

	companion object {

		@JvmStatic
		val lowerKebabIds = listOf(
			"test", "te-st", "t-e-s-t", "-test", "test-", "test1", "test-1"
		)

		@JvmStatic
		val nonLowerKebabIds = listOf(
			/*test-job,*/ "Test-job", "test-Job", "Test-Job", "TEST-JOB",
			/*testjob,*/ "Testjob", "testJob", "TestJob", "TESTJOB",
			"test_job", "Test_job", "test_Job", "Test_Job", "TEST_JOB",
			"test job", "Test job", "test Job", "Test Job", "TEST JOB",
			"teST", "tEST",
		)
	}
}
