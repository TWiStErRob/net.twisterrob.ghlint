package net.twisterrob.ghlint.rules

import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import io.kotest.matchers.string.shouldMatch
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.aLocation
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.locationOfNth
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

class DuplicateStepIdRuleTest {

	@TestFactory fun metadata() = test(DuplicateStepIdRule::class)

	@Nested
	inner class Workflows {

		@Test fun `passes when no ids are defined`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					      - run: echo "Test"
					      - run: echo "Test"
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when same or similar ids are in different jobs`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test1:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        id: step-id
					      - run: echo "Test"
					        id: mystep1
					  test2:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        id: step-id
					      - run: echo "Test"
					        id: mystep2
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when ids are similar`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        id: test1
					      - run: echo "Test"
					        id: step
					      - run: echo "Test"
					        id: test2
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave singleFinding(
				issue = "SimilarStepId",
				message = "Job[test] has similar step identifiers: `test1` and `test2`.",
				location = file("test"),
			)
		}

		// Regression for https://github.com/TWiStErRob/net.twisterrob.ghlint/issues/166
		@Test fun `passes when ids are close, but different`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  repro:
					    runs-on: test
					    steps:
					      - id: params
					        run: 'true'
					      - id: pages
					        run: 'true'
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when multiple ids are similar`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        id: test1
					      - run: echo "Test"
					        id: test2
					      - run: echo "Test"
					        id: test3
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave exactFindings(
				aFinding(
					issue = "SimilarStepId",
					message = "Job[test] has similar step identifiers: `test1` and `test2`.",
					location = file("test"),
				),
				aFinding(
					issue = "SimilarStepId",
					message = "Job[test] has similar step identifiers: `test1` and `test3`.",
					location = file("test"),
				),
				aFinding(
					issue = "SimilarStepId",
					message = "Job[test] has similar step identifiers: `test2` and `test3`.",
					location = file("test"),
				),
			)
		}

		@Test fun `reports when ids are the same`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        id: test
					      - run: echo "Test"
					        id: test
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave singleFinding(
				issue = "DuplicateStepId",
				message = "Job[test] has the `test` step identifier multiple times.",
				location = file("test"),
			)
		}

		@Test fun `reports when multiple ids are the same`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:
					      - run: echo "Test"
					        shell: bash
					        id: hello
					      - run: echo "Test"
					        shell: bash
					        id: world
					      - run: echo "Test"
					        shell: bash
					        id: hello
					      - run: echo "Test"
					        shell: bash
					        id: world
					      - run: echo "Test"
					        shell: bash
					        id: hello
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave exactFindings(
				aFinding(
					issue = "DuplicateStepId",
					message = """Job[test] has the `hello` step identifier multiple times.""",
					location = file("test"),
				),
				aFinding(
					issue = "DuplicateStepId",
					message = """Job[test] has the `world` step identifier multiple times.""",
					location = file("test"),
				),
			)
		}

		@Timeout(
			5,
			unit = TimeUnit.SECONDS,
			threadMode = Timeout.ThreadMode.SEPARATE_THREAD // separate == preemptive.
		)
		@Test fun `reports when myriad of ids are similar`() {
			val steps = (0..100).joinToString(separator = "\n") {
				"""
					|      - run: echo "Test"
					|        id: step-id-${it}
				""".trimMargin()
			}
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${"\n" + steps.prependIndent("\t\t\t\t\t")}
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results should haveSize(4970)
			val messageRegex = """Job\[test] has similar step identifiers: `step-id-\d+` and `step-id-\d+`.""".toRegex()
			results.forEach { finding ->
				finding.issue.id shouldBe "SimilarStepId"
				finding.message shouldMatch messageRegex
				finding.location shouldBe aLocation(file.locationOfNth("test"))
			}
		}

		@Timeout(
			5,
			unit = TimeUnit.SECONDS,
			threadMode = Timeout.ThreadMode.SEPARATE_THREAD // separate == preemptive.
		)
		@Test fun `reports when myriad of ids are the same`() {
			val steps = (0..100).joinToString(separator = "\n") {
				"""
					|      - run: echo "Test"
					|        id: step-id
				""".trimMargin()
			}
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: test
					    steps:${"\n" + steps.prependIndent("\t\t\t\t\t")}
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave singleFinding(
				issue = "DuplicateStepId",
				message = """Job[test] has the `step-id` step identifier multiple times.""",
				location = file("test"),
			)
		}
	}

	@Nested
	inner class Actions {

		@Test fun `passes when no ids are defined`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					    - run: echo "Test"
					      shell: bash
					    - run: echo "Test"
					      shell: bash
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when ids are similar`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      id: test1
					    - run: echo "Test"
					      shell: bash
					      id: step
					    - run: echo "Test"
					      shell: bash
					      id: test2
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave singleFinding(
				issue = "SimilarStepId",
				message = """Action["Test"] has similar step identifiers: `test1` and `test2`.""",
				location = file("name"),
			)
		}

		// Regression for https://github.com/TWiStErRob/net.twisterrob.ghlint/issues/166
		@Test fun `passes when ids are close, but different`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - id: params
					      shell: bash
					      run: 'true'
					    - id: pages
					      shell: bash
					      run: 'true'
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when multiple ids are similar`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      id: test1
					    - run: echo "Test"
					      shell: bash
					      id: test2
					    - run: echo "Test"
					      shell: bash
					      id: test3
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave exactFindings(
				aFinding(
					issue = "SimilarStepId",
					message = """Action["Test"] has similar step identifiers: `test1` and `test2`.""",
					location = file("name"),
				),
				aFinding(
					issue = "SimilarStepId",
					message = """Action["Test"] has similar step identifiers: `test1` and `test3`.""",
					location = file("name"),
				),
				aFinding(
					issue = "SimilarStepId",
					message = """Action["Test"] has similar step identifiers: `test2` and `test3`.""",
					location = file("name"),
				),
			)
		}

		@Test fun `reports when ids are the same`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      id: test
					    - run: echo "Test"
					      shell: bash
					      id: test
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave singleFinding(
				issue = "DuplicateStepId",
				message = """Action["Test"] has the `test` step identifier multiple times.""",
				location = file("name"),
			)
		}

		@Test fun `reports when multiple ids are the same`() {
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:
					    - run: echo "Test"
					      shell: bash
					      id: hello
					    - run: echo "Test"
					      shell: bash
					      id: world
					    - run: echo "Test"
					      shell: bash
					      id: hello
					    - run: echo "Test"
					      shell: bash
					      id: world
					    - run: echo "Test"
					      shell: bash
					      id: hello
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave exactFindings(
				aFinding(
					issue = "DuplicateStepId",
					message = """Action["Test"] has the `hello` step identifier multiple times.""",
					location = file("name"),
				),
				aFinding(
					issue = "DuplicateStepId",
					message = """Action["Test"] has the `world` step identifier multiple times.""",
					location = file("name"),
				),
			)
		}

		@Timeout(
			10,
			unit = TimeUnit.SECONDS,
			threadMode = Timeout.ThreadMode.SEPARATE_THREAD // separate == preemptive.
		)
		@Test fun `reports when myriad of ids are similar`() {
			val steps = (0..1000).joinToString(separator = "\n") {
				"""
					|    - run: echo "Test"
					|      shell: bash
					|      id: step-id-${it}
				""".trimMargin()
			}
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${"\n" + steps.prependIndent("\t\t\t\t\t")}
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results should haveSize(159_931)
			val messageRegex = Regex(
				"""Action\["Test"] has similar step identifiers: `step-id-\d+` and `step-id-\d+`."""
			)
			results.forEach { finding ->
				finding.issue.id shouldBe "SimilarStepId"
				finding.message shouldMatch messageRegex
			}
		}

		@Timeout(
			2,
			unit = TimeUnit.SECONDS,
			threadMode = Timeout.ThreadMode.SEPARATE_THREAD // separate == preemptive.
		)
		@Test fun `reports when myriad of ids are the same`() {
			val steps = (0..100).joinToString(separator = "\n") {
				"""
					|    - run: echo "Test"
					|      shell: bash
					|      id: step-id
				""".trimMargin()
			}
			val file = action(
				"""
					name: Test
					description: Test
					runs:
					  using: composite
					  steps:${"\n" + steps.prependIndent("\t\t\t\t\t")}
				""".trimIndent(),
			)

			val results = check<DuplicateStepIdRule>(file)

			results shouldHave singleFinding(
				issue = "DuplicateStepId",
				message = """Action["Test"] has the `step-id` step identifier multiple times.""",
				location = file("name"),
			)
		}
	}
}
