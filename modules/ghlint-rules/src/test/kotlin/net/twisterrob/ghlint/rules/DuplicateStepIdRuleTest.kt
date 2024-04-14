package net.twisterrob.ghlint.rules

import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import io.kotest.matchers.string.shouldMatch
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

class DuplicateStepIdRuleTest {

	@TestFactory fun metadata() = test(DuplicateStepIdRule::class)

	@Test fun `passes when no ids are defined`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Example"
				      - run: echo "Example"
				      - run: echo "Example"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when same or similar ids are in different jobs`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  test1:
				    steps:
				      - run: echo "Example"
				        id: step-id
				      - run: echo "Example"
				        id: mystep1
				  test2:
				    steps:
				      - run: echo "Example"
				        id: step-id
				      - run: echo "Example"
				        id: mystep2
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when ids are similar`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Example"
				        id: test1
				      - run: echo "Example"
				        id: step
				      - run: echo "Example"
				        id: test2
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"SimilarStepId",
			"Job[test] has similar step identifiers: `test1` and `test2`.",
		)
	}

	// Regression for https://github.com/TWiStErRob/net.twisterrob.ghlint/issues/166
	@Test fun `passes when ids are close, but different`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  repro:
				    runs-on: ubuntu-latest
				    steps:
				      - id: params
				        run: 'true'
				      - id: pages
				        run: 'true'
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when multiple ids are similar`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Example"
				        id: test1
				      - run: echo "Example"
				        id: test2
				      - run: echo "Example"
				        id: test3
			""".trimIndent()
		)

		results shouldHave exactFindings(
			aFinding(
				"SimilarStepId",
				"Job[test] has similar step identifiers: `test1` and `test2`.",
			),
			aFinding(
				"SimilarStepId",
				"Job[test] has similar step identifiers: `test1` and `test3`.",
			),
			aFinding(
				"SimilarStepId",
				"Job[test] has similar step identifiers: `test2` and `test3`.",
			),
		)
	}

	@Test fun `reports when ids are the same`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: echo "Example"
				        id: test
				      - run: echo "Example"
				        id: test
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"DuplicateStepId",
			"Job[test] has the `test` step identifier multiple times.",
		)
	}

	@Timeout(10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD) // separate == preemptive.
	@Test fun `reports when myriad of ids are similar`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  test:
				    steps:${
						"\n" + (0..1000).joinToString(separator = "\n") {
							"""
								|      - run: echo "Example"
								|        id: step-id-${it}
							""".trimMargin()
						}.prependIndent("\t\t\t\t")
					}
			""".trimIndent()
		)

		results should haveSize(159_931)
		val messageRegex = """Job\[test] has similar step identifiers: `step-id-\d+` and `step-id-\d+`.""".toRegex()
		results.forEach { finding ->
			finding.issue.id shouldBe "SimilarStepId"
			finding.message shouldMatch messageRegex
		}
	}

	@Timeout(2, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD) // separate == preemptive.
	@Test fun `reports when myriad of ids are the same`() {
		val results = check<DuplicateStepIdRule>(
			"""
				jobs:
				  test:
				    steps:${
						"\n" + (0..100).joinToString(separator = "\n") {
							"""
								|      - run: echo "Example"
								|        id: step-id
							""".trimMargin()
						}.prependIndent("\t\t\t\t")
					}
			""".trimIndent()
		)

		results should haveSize(5050)
		results.forEach { finding ->
			finding.issue.id shouldBe "DuplicateStepId"
			finding.message shouldBe "Job[test] has the `step-id` step identifier multiple times."
		}
	}
}
