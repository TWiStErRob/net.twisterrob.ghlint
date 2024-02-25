package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

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
}
