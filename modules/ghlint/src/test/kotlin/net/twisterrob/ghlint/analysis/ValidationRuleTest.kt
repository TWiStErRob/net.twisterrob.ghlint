package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.checkUnsafe
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ValidationRuleTest {

	@AcceptFailingDynamicTest(
		displayName = "Issue YamlLoadError non-compliant example #1 has findings",
		reason = "Cannot have a load error without a validation error.",
		acceptableFailure = "^\\Q"
				+ "Could not find exclusively `YamlLoadError`s among findings:\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=JsonSchemaValidation,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-1:9,\n"
				+ "\tmessage=Object does not have some of the required properties [[jobs]] ()\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=YamlLoadError,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-1:9,\n"
				+ "\tmessage=File non-compliant/example.yml could not be loaded:\n"
				+ "```\n"
				+ "java.lang.IllegalStateException: Missing required key: jobs in [on]\n"
				+ "```\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=JsonSchemaValidation,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-1:9,\n"
				+ "\tmessage=Object does not have some of the required properties [[jobs]] ()\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=YamlLoadError,\n"
				+ "\tlocation=non-compliant/example.yml/1:1-1:9,\n"
				+ "\tmessage=File non-compliant/example.yml could not be loaded:\n"
				+ "```\n"
				+ "java.lang.IllegalStateException: Missing required key: jobs in [on]\n"
				+ "```\n"
				+ ")"
				+ "\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun metadata() = test(ValidationRule::class)

	@Test fun `syntax error`() {
		val findings = checkUnsafe<ValidationRule>("mapping: *")

		findings shouldHave singleFinding(
			issue = "YamlSyntaxError",
			message = """
				File test.yml could not be parsed:
				```
				java.lang.IllegalArgumentException: Failed to parse YAML: while scanning an alias
				 in reader, line 1, column 10:
				     *
				     ^
				unexpected character found ${'\u0000'}(0)
				 in reader, line 1, column 11:
				     *
				      ^
				
				```
			""".trimIndent(),
		)
	}

	@Test fun `wrong workflow yaml contents`() {
		val findings = checkUnsafe<ValidationRule>(
			"""
				foo: bar
			""".trimIndent()
		)

		findings shouldHave exactFindings(
			aFinding(
				issue = "JsonSchemaValidation",
				message = """
					Object does not have some of the required properties [[jobs, on]] ()
				""".trimIndent(),
			),
			aFinding(
				issue = "YamlLoadError",
				message = """
					File test.yml could not be loaded:
					```
					java.lang.IllegalStateException: Missing required key: jobs in [foo]
					```
				""".trimIndent(),
			),
		)
	}

	@Test fun `wrong action yaml contents`() {
		val findings = checkUnsafe<ValidationRule>(
			"""
				foo: bar
			""".trimIndent(),
			fileName = "action.yml",
		)

		findings shouldHave exactFindings(
			aFinding(
				issue = "JsonSchemaValidation",
				message = """
					Object does not have some of the required properties [[name, description, runs]] ()
				""".trimIndent(),
			),
			aFinding(
				issue = "YamlLoadError",
				message = """
					File action.yml could not be loaded:
					```
					java.lang.IllegalStateException: Missing required key: name in [foo]
					```
				""".trimIndent(),
			),
		)
	}

	@Test fun `valid workflow contents`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  example:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		val findings = check<ValidationRule>(file)

		findings shouldHave noFindings()
	}

	@Test fun `duplicate key is reported`() {
		@Suppress("YAMLDuplicatedKeys")
		val findings = checkUnsafe<ValidationRule>(
			"""
				on: push
				jobs:
				  a-job:
				    uses: reusable/workflow.yml
				
				  a-job:
				    name: "Job"
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent(),
		)

		findings shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Duplicate key: a-job (/jobs/a-job)",
		)
	}

	@Test fun `valid action contents`() {
		val findings = check<ValidationRule>(
			"""
				name: ""
				description: ""
				runs:
				  using: node20
				  main: index.js
			""".trimIndent(),
			fileName = "action.yml",
		)

		findings shouldHave noFindings()
	}
}
