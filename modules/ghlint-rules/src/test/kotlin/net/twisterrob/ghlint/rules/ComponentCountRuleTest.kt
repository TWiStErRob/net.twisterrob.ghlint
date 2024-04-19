package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.lang.Math.random
import kotlin.random.Random

class ComponentCountRuleTest {

	@TestFactory fun metadata() = test(ComponentCountRule::class)

	@Nested
	inner class TooManyJobsTest {

		@Test
		fun `passes single step job`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:${Random.generateJobs(1)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step job`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:${Random.generateJobs(5)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:${Random.generateJobs(10)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:${Random.generateJobs(11)}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"TooManyJobs",
				"Workflow[test] has 11 jobs, maximum recommended is 10."
			)
		}

		@Test
		fun `fails double steps count`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:${Random.generateJobs(20)}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"TooManyJobs",
				"Workflow[test] has 20 jobs, maximum recommended is 10."
			)
		}
	}

	@Nested
	inner class TooManyJobStepsTest {

		@Test
		fun `passes single step job`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(1)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step job`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(5)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(20)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(21)}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"TooManySteps",
				"Job[test] has 21 steps, maximum recommended is 20."
			)
		}

		@Test
		fun `fails double steps count`() {
			val results = check<ComponentCountRule>(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(40)}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"TooManySteps",
				"Job[test] has 40 steps, maximum recommended is 20."
			)
		}
	}

	@Nested
	inner class TooManyActionStepsTest {

		@Test
		fun `passes single step action`() {
			val results = check<ComponentCountRule>(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(1)}
				""".trimIndent(),
				fileName = "action.yml",
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step action`() {
			val results = check<ComponentCountRule>(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(5)}
				""".trimIndent(),
				fileName = "action.yml",
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val results = check<ComponentCountRule>(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(20)}
				""".trimIndent(),
				fileName = "action.yml",
			)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val results = check<ComponentCountRule>(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(21)}
				""".trimIndent(),
				fileName = "action.yml",
			)

			results shouldHave singleFinding(
				"TooManySteps",
				"""Action["Test"] has 21 steps, maximum recommended is 20."""
			)
		}

		@Test
		fun `fails double steps count`() {
			val results = check<ComponentCountRule>(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(40)}
				""".trimIndent(),
				fileName = "action.yml",
			)

			results shouldHave singleFinding(
				"TooManySteps",
				"""Action["Test"] has 40 steps, maximum recommended is 20."""
			)
		}
	}

	@Nested
	@Suppress("detekt.TrimMultilineRawString") // Verifying exact indentations.
	inner class GenerateTest {

		@Test fun `generate single step`() {
			Random(0).generateSteps(1) shouldBe """
					    - uses: some/action@v1
			""".trimEnd()
		}

		@Test fun `generate multiple steps`() {
			Random(0).generateSteps(3) shouldBe """
					    - uses: some/action@v1
					    - run: echo "Test 2"
					    - uses: some/action@v3
			""".trimEnd()
		}

		@Test fun `generate single action step`() {
			Random(0).generateActionSteps(1) shouldBe """
					    - uses: some/action@v1
			""".trimEnd()
		}

		@Test fun `generate multiple action steps`() {
			Random(4).generateActionSteps(4) shouldBe """
					    - run: echo "Test 1"
					      shell: bash
					    - run: echo "Test 2"
					      shell: bash
					    - uses: some/action@v3
					    - run: echo "Test 4"
					      shell: bash
			""".trimEnd()
		}

		@Test fun `generate single job`() {
			Random(0).generateJobs(1) shouldBe """
					  test1:
					    runs-on: test
					    steps:
					      - run: echo "Test 1"
					      - uses: some/action@v2
					      - uses: some/action@v3
			""".trimEnd()
		}

		@Test fun `generate multiple jobs`() {
			Random(0).generateJobs(3) shouldBe """
					  test1:
					    runs-on: test
					    steps:
					      - run: echo "Test 1"
					      - uses: some/action@v2
					      - uses: some/action@v3
					  test2:
					    runs-on: test
					    steps:
					      - run: echo "Test 1"
					      - run: echo "Test 2"
					      - run: echo "Test 3"
					  test3:
					    runs-on: test
					    steps:
					      - uses: some/action@v1
					      - run: echo "Test 2"
			""".trimEnd()
		}
	}

	private fun Random.generateJobs(count: Int): String =
		"\n" + (1..count).joinToString(separator = "\n") {
			when (random().toInt() % 2) {
				0 -> """
					test${it}:
					  runs-on: test
					  steps:${generateSteps(nextInt(1, 5))}
				""".trimIndent()

				1 -> """
					test${it}:
					  uses: org/repo/.github/workflows/workflow.yml@v${it}
				""".trimIndent()

				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t  ")

	private fun Random.generateSteps(count: Int): String =
		"\n" + (1..count).joinToString(separator = "\n") {
			when (nextInt(1, 3)) {
				1 -> "- run: echo \"Test $it\""
				2 -> "- uses: some/action@v${it}"
				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t    ")

	private fun Random.generateActionSteps(count: Int): String =
		"\n" + (1..count).joinToString(separator = "\n") {
			@Suppress("detekt.StringShouldBeRawString") // Complex string building, be explicit.
			when (nextInt(1, 3)) {
				1 -> "- run: echo \"Test $it\"\n  shell: bash"
				2 -> "- uses: some/action@v${it}"
				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t    ")
}
