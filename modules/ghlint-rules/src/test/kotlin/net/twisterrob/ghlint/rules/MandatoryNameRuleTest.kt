package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import org.junit.jupiter.api.Test

class MandatoryNameRuleTest {

	@Test fun `reports when workflow is missing a name`() {
		val result = check<MandatoryNameRule>(
			"""
				jobs:
			""".trimIndent()
		)

		result should haveFinding(
			"MandatoryWorkflowName",
			"Workflow[test.yml] must have a name."
		)
	}

	@Test fun `passes when workflow has a name`() {
		val result = check<MandatoryNameRule>(
			"""
				name: Test
				jobs:
			""".trimIndent()
		)

		result should beEmpty()
	}

	@Test fun `reports when job is missing a name`() {
		val result = check<MandatoryNameRule>(
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
			"MandatoryJobName",
			"Job[test] must have a name."
		)
	}

	@Test fun `passes when job has a name`() {
		val result = check<MandatoryNameRule>(
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
		val result = check<MandatoryNameRule>(
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
			"MandatoryStepName",
			"Step[#0] in Job[test] must have a name."
		)
	}

	@Test fun `passes when step has a name`() {
		val result = check<MandatoryNameRule>(
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
