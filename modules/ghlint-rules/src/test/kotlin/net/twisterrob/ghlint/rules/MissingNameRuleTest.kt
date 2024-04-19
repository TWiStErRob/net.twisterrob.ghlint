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
				on: push
				#name: Missing
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
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
				on: push
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when job is missing a name`() {
		val results = check<MissingNameRule>(
			"""
				name: Irrelevant
				on: push
				jobs:
				  test:
				    #name: Missing
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
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
				on: push
				jobs:
				  test:
				    name: Test
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when step is missing a name in job`() {
		val results = check<MissingNameRule>(
			"""
				name: Irrelevant
				on: push
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        #name: Missing
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingStepName",
			"Step[#0] in Job[test] is missing a name, add one to improve developer experience."
		)
	}

	@Test fun `passes when step has a name in job`() {
		val results = check<MissingNameRule>(
			"""
				name: Irrelevant
				on: push
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - name: Test
				        run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when step is missing a name in action`() {
		val results = check<MissingNameRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: echo "Test"
				      shell: bash
				      #name: Missing
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"MissingStepName",
			"""Step[#0] in Action["Test"] is missing a name, add one to improve developer experience."""
		)
	}

	@Test fun `passes when step has a name in action`() {
		val results = check<MissingNameRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - name: Test
				      run: echo "Test"
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}
}
