package net.twisterrob.ghlint.rules

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

	private fun generateJobs(count: Int): String =
		"\n" + (1..count).joinToString(separator = "\n") {
			when (random().toInt() % 2) {
				0 -> """
					test${it}:
					  runs-on: ubuntu-latest
					  steps:${generateSteps(Random.nextInt(1, 5))}
				""".trimIndent()

				1 -> """
					test${it}:
					  uses: org/repo/.github/workflows/workflow.yml@v${it}
				""".trimIndent()

				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t  ")

	private fun generateSteps(count: Int): String =
		"\n" + (1..count).joinToString(separator = "\n") {
			when (Random.nextInt(1, 3)) {
				1 -> "- run: echo \"Test $it\""
				2 -> "- uses: some/action@v${it}"
				else -> error("Not possible")
			}
		}.prependIndent("\t\t\t\t\t    ")

	@Nested
	inner class TooManyJobsTest {

		@Test
		fun `passes single step job`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:${generateJobs(1)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step job`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:${generateJobs(5)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:${generateJobs(10)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:${generateJobs(11)}
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
					jobs:${generateJobs(20)}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"TooManyJobs",
				"Workflow[test] has 20 jobs, maximum recommended is 10."
			)
		}
	}

	@Nested
	inner class TooManyStepsTest {

		@Test
		fun `passes single step job`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:
					  test:
					    steps:${generateSteps(1)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes few step job`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:
					  test:
					    steps:${generateSteps(5)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `passes max steps count`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:
					  test:
					    steps:${generateSteps(20)}
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test
		fun `fails max steps count + 1`() {
			val results = check<ComponentCountRule>(
				"""
					jobs:
					  test:
					    steps:${generateSteps(21)}
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
					jobs:
					  test:
					    steps:${generateSteps(40)}
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"TooManySteps",
				"Job[test] has 40 steps, maximum recommended is 20."
			)
		}
	}
}
