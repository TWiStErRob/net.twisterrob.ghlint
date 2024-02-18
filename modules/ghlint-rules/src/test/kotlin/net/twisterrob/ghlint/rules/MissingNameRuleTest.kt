package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MissingNameRuleTest {

	@TestFactory fun metadata() = test(MissingNameRule::class)

	@Test fun `reports when workflow is missing a name`() {
		val results = check<MissingNameRule>(
			"""
				jobs:
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingWorkflowName",
			"Workflow[test] is missing a name, add one to improve developer experience."
		)
	}

	@Test fun `passes when workflow has a name`() {
		val results = check<MissingNameRule>(
			"""
				name: Test
				jobs:
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when job is missing a name`() {
		val results = check<MissingNameRule>(
			"""
				name: Irrelevant
				jobs:
				  test:
				    steps:
				      - name: Irrelevant
				        run: true
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingJobName",
			"Job[test] is missing a name, add one to improve developer experience."
		)
	}

	@Test fun `passes when job has a name`() {
		val results = check<MissingNameRule>(
			"""
				name: Irrelevant
				jobs:
				  test:
				    name: Test
				    steps:
				      - name: Irrelevant
				        run: true
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when step is missing a name`() {
		val results = check<MissingNameRule>(
			"""
				name: Irrelevant
				jobs:
				  test:
				    name: Irrelevant
				    steps:
				      - run: true
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingStepName",
			"Step[#0] in Job[test] is missing a name, add one to improve developer experience."
		)
	}

	@Test fun `passes when step has a name`() {
		val results = check<MissingNameRule>(
			"""
				name: Irrelevant
				jobs:
				  test:
				    name: Irrelevant
				    steps:
				      - name: Test
				        run: true
			""".trimIndent()
		)

		results shouldHave noFindings()
	}
}
