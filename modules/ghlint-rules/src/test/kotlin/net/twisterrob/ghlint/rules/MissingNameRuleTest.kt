package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import org.junit.jupiter.api.Test

class MissingNameRuleTest {

	@Test fun `reports when workflow is missing a name`() {
		val result = check<MissingNameRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should haveFinding(
			"MissingWorkflowName",
			"Workflow[test.yml] is missing a name, add one to improve developer experience."
		)
	}

	@Test fun `passes when workflow has a name`() {
		val result = check<MissingNameRule>(
			"""
				name: Test
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `reports when job is missing a name`() {
		val result = check<MissingNameRule>(
			"""
				name: Irrelevant
				jobs:
				  test:
				    steps:
				      - name: Irrelevant
				        run: true
			""".trimIndent()
		)

		result should haveFinding(
			"MissingJobName",
			"Job[test] is missing a name, add one to improve developer experience."
		)
	}

	@Test fun `passes when job has a name`() {
		val result = check<MissingNameRule>(
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

		result should beEmpty()
	}

	@Test fun `reports when step is missing a name`() {
		val result = check<MissingNameRule>(
			"""
				name: Irrelevant
				jobs:
				  test:
				    name: Irrelevant
				    steps:
				      - run: true
			""".trimIndent()
		)

		result should haveFinding(
			"MissingStepName",
			"Step[#0] in Job[test] is missing a name, add one to improve developer experience."
		)
	}

	@Test fun `passes when step has a name`() {
		val result = check<MissingNameRule>(
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

		result should beEmpty()
	}
}
