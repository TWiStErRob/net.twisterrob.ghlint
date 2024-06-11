package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.random.Random

class JobDependenciesRuleTest {

	@TestFactory fun metadata() = test(JobDependenciesRule::class)

	@Test fun `passes when single job has no dependency`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		val results = check<JobDependenciesRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when parallel jobs have no dependency`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test1:
				    uses: reusable/workflow.yml
				  test2:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		val results = check<JobDependenciesRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when single job references unknown job`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    needs: missing
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		val results = check<JobDependenciesRule>(file)

		results shouldHave singleFinding(
			"MissingNeedsJob",
			"Job[test] references Job[missing], which does not exist.",
		)
	}

	@Test fun `reports when there are multiple unknown job references in a diamond`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test0:
				    uses: reusable/workflow.yml
				  test1:
				    needs: [test0, missing1]
				    uses: reusable/workflow.yml
				  test2:
				    needs: [test0, missing2]
				    uses: reusable/workflow.yml
				  test3:
				    needs: [test1, test2]
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		val results = check<JobDependenciesRule>(file)

		results shouldHave exactFindings(
			aFinding(
				"MissingNeedsJob",
				"Job[test1] references Job[missing1], which does not exist.",
			),
			aFinding(
				"MissingNeedsJob",
				"Job[test2] references Job[missing2], which does not exist.",
			)
		)
	}

	@Test fun `passes on long chain of jobs`() {
		val file = yaml(
			"""
				on: push
				jobs:${Random.generate(1000) { if (it == 1) emptyList() else listOf("test${it - 1}") }}
			""".trimIndent(),
		)

		val results = check<JobDependenciesRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes on large fan of jobs`() {
		val file = yaml(
			"""
				on: push
				jobs:${Random.generate(100) { n -> (1..<n).map { "test${it}" } }}
			""".trimIndent(),
		)

		val results = check<JobDependenciesRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes on large parallel flow of jobs`() {
		val file = yaml(
			"""
				on: push
				jobs:${Random.generate(100) { listOf("start") }}
				  start:
				    uses: org/repo/.github/workflows/workflow.yml@v0
				  end:
				    needs: [ ${(1..100).joinToString(separator = ", ") { "test$it" }} ]
				    uses: org/repo/.github/workflows/workflow.yml@all
			""".trimIndent(),
		)

		val results = check<JobDependenciesRule>(file)

		results shouldHave noFindings()
	}

	@Nested
	inner class CyclesTest {

		@Test fun `reports when single job self-references`() {
			val file = yaml(
				"""
					on: push
					jobs:
					  test:
					    needs: test
					    uses: reusable/workflow.yml
				""".trimIndent(),
			)

			val results = check<JobDependenciesRule>(file)

			results shouldHave singleFinding(
				"JobDependencyCycle",
				"Job[test] forms a dependency cycle: [test].",
			)
		}

		@Test fun `reports when two jobs reference each other`() {
			val file = yaml(
				"""
					on: push
					jobs:
					  test1:
					    needs: test2
					    uses: reusable/workflow.yml
					  test2:
					    needs: test1
					    uses: reusable/workflow.yml
				""".trimIndent(),
			)

			val results = check<JobDependenciesRule>(file)

			results shouldHave singleFinding(
				"JobDependencyCycle",
				"Job[test1] forms a dependency cycle: [test1, test2].",
			)
		}

		@Test fun `reports when for jobs reference each other`() {
			val file = yaml(
				"""
					on: push
					jobs:
					  test1:
					    needs: test2
					    uses: reusable/workflow.yml
					  test2:
					    needs: test1
					    uses: reusable/workflow.yml
					  test3:
					    needs: test4
					    uses: reusable/workflow.yml
					  test4:
					    needs: test3
					    uses: reusable/workflow.yml
				""".trimIndent(),
			)

			val results = check<JobDependenciesRule>(file)

			results shouldHave singleFinding(
				"JobDependencyCycle",
				"Job[test1] forms a dependency cycle: [test1, test2].",
			)
		}

		@Test fun `reports when three jobs form a cycle`() {
			val file = yaml(
				"""
					on: push
					jobs:
					  test1:
					    needs: test3
					    uses: reusable/workflow.yml
					  test2:
					    needs: test1
					    uses: reusable/workflow.yml
					  test3:
					    needs: test2
					    uses: reusable/workflow.yml
				""".trimIndent(),
			)

			val results = check<JobDependenciesRule>(file)

			results shouldHave singleFinding(
				"JobDependencyCycle",
				"Job[test1] forms a dependency cycle: [test1, test2, test3].",
			)
		}

		@Test fun `reports when chain ends in self-reference cycle`() {
			val file = yaml(
				"""
					on: push
					jobs:
					  test1:
					    needs: test2
					    uses: reusable/workflow.yml
					  test2:
					    needs: test3
					    uses: reusable/workflow.yml
					  test3:
					    needs: test3
					    uses: reusable/workflow.yml
				""".trimIndent(),
			)

			val results = check<JobDependenciesRule>(file)

			results shouldHave singleFinding(
				"JobDependencyCycle",
				"Job[test3] forms a dependency cycle: [test3].",
			)
		}
	}

	companion object {

		@Suppress("UnusedReceiverParameter") // Keep for future.
		private fun Random.generate(count: Int, needs: (Int) -> List<String>): String {
			val others = (1..count).joinToString(separator = "\n") { n ->
				val needsIds = needs(n)
				val needsLine = if (needsIds.isEmpty()) {
					"#needs: []"
				} else {
					"needs: [${needsIds.joinToString(separator = ", ")}]"
				}
				"""
					test${n}:
					  ${needsLine}
					  uses: org/repo/.github/workflows/workflow.yml@v${n}
				""".trimIndent()
			}
			return "\n" + others.prependIndent("\t\t\t\t  ")
		}
	}
}
