package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MissingShellRuleTest {

	@AcceptFailingDynamicTest(
		displayName = "Issue MissingShell non-compliant example #2 has findings",
		reason = "Shell is mandatory for Actions, JSON-schema validation catches it.",
		acceptableFailure = "^\\QCould not find exclusively `MissingShell`s among findings:\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=JsonSchemaValidation,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-6:50,\n"
				+ "\tmessage=Object does not have some of the required properties [[jobs, on]] ()\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=YamlLoadError,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-6:50,\n"
				+ "\tmessage=File non-compliant/example.yml could not be loaded:\n"
				+ "```\n"
				+ "java.lang.IllegalStateException: Missing required key: jobs in [name, description, runs]\n"
				+ "```\n"
				+ ")\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun metadata() = test(MissingShellRule::class)

	@Test fun `reports when step is missing a shell`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingShellRule>(file)

		results shouldHave singleFinding(
			issue = "MissingShell",
			message = "Step[#0] in Job[test] is missing a shell, specify `bash` for better error handling.",
			location = file("-", 2),
		)
	}

	@Test fun `passes when shell is declared on step`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent(),
		)

		val results = check<MissingShellRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when shell is declared on the job`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				        shell: bash
			""".trimIndent(),
		)

		val results = check<MissingShellRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when shell is declared on the workflow`() {
		val file = workflow(
			"""
				on: push
				defaults:
				  run:
				    shell: bash
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingShellRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `reports when step is declared on another job`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  other:
				    runs-on: test
				    defaults:
				      run:
				        shell: bash
				    steps:
				      - run: echo "Test"
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingShellRule>(file)

		results shouldHave singleFinding(
			issue = "MissingShell",
			message = "Step[#0] in Job[test] is missing a shell, specify `bash` for better error handling.",
			location = file("-", 4),
		)
	}
}
