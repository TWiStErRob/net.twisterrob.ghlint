package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class MissingNameRuleTest {

	@TestFactory fun metadata() = test(MissingNameRule::class)

	@Test fun `reports when workflow is missing a name`() {
		val file = yaml(
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
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingWorkflowName",
			"""Workflow[test] is missing a name, add one to improve developer experience."""
		)
	}

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when workflow has empty name`(name: String) {
		val file = yaml(
			"""
				on: push
				name: ${name}
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingWorkflowName",
			"""Workflow[test] is missing a name, add one to improve developer experience."""
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when workflow has blank name`(name: String, @Suppress("UNUSED_PARAMETER") value: String) {
		val file = yaml(
			"""
				on: push
				name: ${name}
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingWorkflowName",
			"""Workflow[test] is missing a name, add one to improve developer experience."""
		)
	}

	@Test fun `passes when workflow has a name`() {
		val file = yaml(
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
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when job is missing a name`() {
		val file = yaml(
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
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingJobName",
			"""Job[test] is missing a name, add one to improve developer experience."""
		)
	}

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when job has empty name`(name: String) {
		val file = yaml(
			"""
				name: Irrelevant
				on: push
				jobs:
				  test:
				    name: ${name}
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingJobName",
			"""Job[test] is missing a name, add one to improve developer experience."""
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when job has blank name`(name: String, @Suppress("UNUSED_PARAMETER") value: String) {
		val file = yaml(
			"""
				name: Irrelevant
				on: push
				jobs:
				  test:
				    name: ${name}
				    runs-on: test
				    steps:
				      - name: Irrelevant
				        run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingJobName",
			"""Job[test] is missing a name, add one to improve developer experience."""
		)
	}

	@Test fun `passes when job has a name`() {
		val file = yaml(
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
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when step is missing a name in job`() {
		val file = yaml(
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
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingStepName",
			"""Step[#0] in Job[test] is missing a name, add one to improve developer experience."""
		)
	}

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when step has empty name in job`(name: String) {
		val file = yaml(
			"""
				name: Irrelevant
				on: push
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        name: ${name}
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingStepName",
			"""Step[""] in Job[test] is missing a name, add one to improve developer experience."""
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when step has blank name in job`(name: String, value: String) {
		val file = yaml(
			"""
				name: Irrelevant
				on: push
				jobs:
				  test:
				    name: Irrelevant
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        name: ${name}
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			"MissingStepName",
			"""Step["${value}"] in Job[test] is missing a name, add one to improve developer experience."""
		)
	}

	@Test fun `passes when step has a name in job`() {
		val file = yaml(
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
			""".trimIndent(),
		)

		val results = check<MissingNameRule>(file)

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

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when step has empty name in action`(name: String) {
		val results = check<MissingNameRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: echo "Test"
				      shell: bash
				      name: ${name}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"MissingStepName",
			"""Step[""] in Action["Test"] is missing a name, add one to improve developer experience."""
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when step has blank name in action`(name: String, value: String) {
		val results = check<MissingNameRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: echo "Test"
				      shell: bash
				      name: ${name}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"MissingStepName",
			"""Step["${value}"] in Action["Test"] is missing a name, add one to improve developer experience."""
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

	companion object {

		@JvmStatic
		val emptyNames: List<String> = listOf(
			"!!str",
			"|",
			">",
			">-",
			">+",
			"''",
			"\"\"",
		)

		@Suppress("detekt.StringShouldBeRawString") // Significant complex whitespaces + escapes.
		@JvmStatic
		val blankNames: List<Arguments> = listOf(
			arguments("\" \"", " "),
			arguments("' '", " "),
			arguments("\"    \"", "    "),
			arguments("'     '", "     "),
			arguments("\"\t\"", "\t"),
			arguments("'\t'", "\t"),
			arguments("\"\\n\"", "\n"),
		)
	}
}
