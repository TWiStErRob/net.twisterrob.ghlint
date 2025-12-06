package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.action
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.checkUnsafe
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.invoke
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import net.twisterrob.ghlint.testing.yaml
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
				+ "\tmessage=Object does not have some of the required properties [jobs] ()\n"
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
				+ "\tmessage=Object does not have some of the required properties [jobs] ()\n"
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
		val file = yaml("mapping: *")

		val findings = checkUnsafe<ValidationRule>(file)

		findings shouldHave singleFinding(
			issue = "YamlSyntaxError",
			message = """
				File test.yml could not be parsed:
				```
				java.lang.IllegalArgumentException: Failed to parse YAML: while scanning an alias
				 in reader, line 1, column 10:
				    mapping: *
				             ^
				unexpected character found ${'\u0000'}(0)
				 in reader, line 1, column 11:
				    mapping: *
				              ^
				
				```
			""".trimIndent(),
			location = file(file.content),
		)
	}

	@Test fun `wrong workflow yaml contents`() {
		val file = workflow(
			"""
				foo: bar
			""".trimIndent(),
		)

		val findings = checkUnsafe<ValidationRule>(file)

		findings shouldHave exactFindings(
			aFinding(
				issue = "JsonSchemaValidation",
				message = """
					Object does not have some of the required properties [jobs, on] ()
				""".trimIndent(),
				location = file(file.content),
			),
			aFinding(
				issue = "YamlLoadError",
				message = """
					File test.yml could not be loaded:
					```
					java.lang.IllegalStateException: Missing required key: jobs in [foo]
					```
				""".trimIndent(),
				location = file(file.content),
			),
		)
	}

	@Test fun `wrong action yaml contents`() {
		val file = action(
			"""
				foo: bar
			""".trimIndent(),
		)

		val findings = checkUnsafe<ValidationRule>(file)

		findings shouldHave exactFindings(
			aFinding(
				issue = "JsonSchemaValidation",
				message = """
					Object does not have some of the required properties [name, description, runs] ()
				""".trimIndent(),
				location = file(file.content),
			),
			aFinding(
				issue = "YamlLoadError",
				message = """
					File action.yml could not be loaded:
					```
					java.lang.IllegalStateException: Missing required key: name in [foo]
					```
				""".trimIndent(),
				location = file(file.content),
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
		val file = workflow(
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

		val findings = checkUnsafe<ValidationRule>(file)

		findings shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Duplicate key: a-job (/jobs/a-job)",
			location = "test.yml/7:5-10:34",
		)
	}

	@Test fun `valid action contents`() {
		val file = action(
			"""
				name: ""
				description: ""
				runs:
				  using: node20
				  main: index.js
			""".trimIndent(),
		)

		val findings = check<ValidationRule>(file)

		findings shouldHave noFindings()
	}
}
