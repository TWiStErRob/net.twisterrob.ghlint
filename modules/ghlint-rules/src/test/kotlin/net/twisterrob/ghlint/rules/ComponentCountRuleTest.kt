package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
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
			val file = workflow(
				"""
					on: push
					jobs:${Random.generateJobs(1)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step job`() {
			val file = workflow(
				"""
					on: push
					jobs:${Random.generateJobs(5)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val file = workflow(
				"""
					on: push
					jobs:${Random.generateJobs(10)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val file = workflow(
				"""
					on: push
					jobs:${Random.generateJobs(11)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave singleFinding(
				issue = "TooManyJobs",
				message = "Workflow[test] has 11 jobs, maximum recommended is 10.",
				location = file("jobs"),
			)
		}

		@Test
		fun `fails double steps count`() {
			val file = workflow(
				"""
					on: push
					jobs:${Random.generateJobs(20)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave singleFinding(
				issue = "TooManyJobs",
				message = "Workflow[test] has 20 jobs, maximum recommended is 10.",
				location = file("jobs"),
			)
		}
	}

	@Nested
	inner class TooManyJobStepsTest {

		@Test
		fun `passes single step job`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(1)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step job`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(5)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(20)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(21)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave singleFinding(
				issue = "TooManySteps",
				message = "Job[test] has 21 steps, maximum recommended is 20.",
				location = file("test"),
			)
		}

		@Test
		fun `fails double steps count`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${Random.generateSteps(40)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave singleFinding(
				issue = "TooManySteps",
				message = "Job[test] has 40 steps, maximum recommended is 20.",
				location = file("test"),
			)
		}
	}

	@Nested
	inner class TooManyActionStepsTest {

		@Test
		fun `passes single step action`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(1)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step action`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(5)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(20)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(21)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave singleFinding(
				issue = "TooManySteps",
				message = """Action["Test"] has 21 steps, maximum recommended is 20.""",
				location = file("name"),
			)
		}

		@Test
		fun `fails double steps count`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${Random.generateActionSteps(40)}
				""".trimIndent(),
			)

			val results = check<ComponentCountRule>(file)

			results shouldHave singleFinding(
				issue = "TooManySteps",
				message = """Action["Test"] has 40 steps, maximum recommended is 20.""",
				location = file("name"),
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
		"\n" + (1..count).joinToString(separator = "\n") { number ->
			when (random().toInt() % 2) {
				0 -> """
					test${number}:
					  runs-on: test
					  steps:${generateSteps(nextInt(1, 5))}
				""".trimIndent()

				1 -> """
					test${number}:
					  uses: org/repo/.github/workflows/workflow.yml@v${number}
				""".trimIndent()

				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t  ")

	private fun Random.generateSteps(count: Int): String =
		"\n" + (1..count).joinToString(separator = "\n") { number ->
			when (nextInt(1, 3)) {
				1 -> "- run: echo \"Test ${number}\""
				2 -> "- uses: some/action@v${number}"
				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t    ")

	private fun Random.generateActionSteps(count: Int): String =
		"\n" + (1..count).joinToString(separator = "\n") { number ->
			@Suppress("detekt.StringShouldBeRawString") // Complex string building, be explicit.
			when (nextInt(1, 3)) {
				1 -> "- run: echo \"Test ${number}\"\n  shell: bash"
				2 -> "- uses: some/action@v${number}"
				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t    ")
}
