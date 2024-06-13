package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class MissingNameRuleTest {

	@TestFactory fun metadata() = test(MissingNameRule::class)

	@Test fun `reports when workflow is missing a name`() {
		val file = workflow(
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
			issue = "MissingWorkflowName",
			message = """Workflow[test] is missing a name, add one to improve developer experience.""",
			location = file("jobs"),
		)
	}

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when workflow has empty name`(name: String) {
		val file = workflow(
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
			issue = "MissingWorkflowName",
			message = """Workflow[test] is missing a name, add one to improve developer experience.""",
			location = file("jobs"),
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when workflow has blank name`(name: String, @Suppress("UNUSED_PARAMETER") value: String) {
		val file = workflow(
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
			issue = "MissingWorkflowName",
			message = """Workflow[test] is missing a name, add one to improve developer experience.""",
			location = file("jobs"),
		)
	}

	@Test fun `passes when workflow has a name`() {
		val file = workflow(
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
		val file = workflow(
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
			issue = "MissingJobName",
			message = """Job[test] is missing a name, add one to improve developer experience.""",
			location = file("test"),
		)
	}

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when job has empty name`(name: String) {
		val file = workflow(
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
			issue = "MissingJobName",
			message = """Job[test] is missing a name, add one to improve developer experience.""",
			location = file("test"),
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when job has blank name`(name: String, @Suppress("UNUSED_PARAMETER") value: String) {
		val file = workflow(
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
			issue = "MissingJobName",
			message = """Job[test] is missing a name, add one to improve developer experience.""",
			location = file("test"),
		)
	}

	@Test fun `passes when job has a name`() {
		val file = workflow(
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
		val file = workflow(
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
			issue = "MissingStepName",
			message = """Step[#0] in Job[test] is missing a name, add one to improve developer experience.""",
			location = file("-", 2),
		)
	}

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when step has empty name in job`(name: String) {
		val file = workflow(
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
			issue = "MissingStepName",
			message = """Step[""] in Job[test] is missing a name, add one to improve developer experience.""",
			location = file("-", 2),
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when step has blank name in job`(name: String, value: String) {
		val file = workflow(
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
			issue = "MissingStepName",
			message = """Step["${value}"] in Job[test] is missing a name, add one to improve developer experience.""",
			location = file("-", 2),
		)
	}

	@Test fun `passes when step has a name in job`() {
		val file = workflow(
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
		val file = action(
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
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			issue = "MissingStepName",
			message = """Step[#0] in Action["Test"] is missing a name, add one to improve developer experience.""",
			location = file("-"),
		)
	}

	@MethodSource("getEmptyNames")
	@ParameterizedTest
	fun `reports when step has empty name in action`(name: String) {
		val file = action(
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
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			issue = "MissingStepName",
			message = """Step[""] in Action["Test"] is missing a name, add one to improve developer experience.""",
			location = file("-"),
		)
	}

	@MethodSource("getBlankNames")
	@ParameterizedTest
	fun `reports when step has blank name in action`(name: String, value: String) {
		val file = action(
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
		)

		val results = check<MissingNameRule>(file)

		results shouldHave singleFinding(
			issue = "MissingStepName",
			message = """Step["${value}"] in Action["Test"] is missing a name, add one to improve developer experience.""",
			location = file("-"),
		)
	}

	@Test fun `passes when step has a name in action`() {
		val file = action(
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
		)

		val results = check<MissingNameRule>(file)

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
