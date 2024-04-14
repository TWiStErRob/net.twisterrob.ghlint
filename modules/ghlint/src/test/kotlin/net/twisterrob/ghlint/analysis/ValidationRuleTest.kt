package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
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
				+ "\tlocation=test.yml/1:1-1:9,\n"
				+ "\tmessage=Object does not have some of the required properties [[jobs]] ()\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=YamlLoadError,\n"
				+ "\tlocation=test.yml/1:1-1:8,\n"
				+ "\tmessage=File test.yml could not be loaded:\n"
				+ "```\n"
				+ "java.lang.IllegalStateException: Missing required key: jobs in [on]\n"
				+ "```\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=JsonSchemaValidation,\n"
				+ "\tlocation=test.yml/1:1-1:9,\n"
				+ "\tmessage=Object does not have some of the required properties [[jobs]] ()\n"
				+ ")\n"
				+ "Finding(\n"
				+ "\trule=net.twisterrob.ghlint.analysis.ValidationRule@\\E[0-9a-f]+\\Q,\n"
				+ "\tissue=YamlLoadError,\n"
				+ "\tlocation=test.yml/1:1-1:8,\n"
				+ "\tmessage=File test.yml could not be loaded:\n"
				+ "```\n"
				+ "java.lang.IllegalStateException: Missing required key: jobs in [on]\n"
				+ "```\n"
				+ ")"
				+ "\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun metadata() = test(ValidationRule::class)

	@Test fun `syntax error`() {
		val findings = check<ValidationRule>("mapping: *")

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
			""".trimIndent()
		)
	}

	@Test fun `wrong yaml contents`() {
		val findings = check<ValidationRule>("foo: bar")

		findings shouldHave exactFindings(
			aFinding(
				issue = "JsonSchemaValidation",
				message = """
					Object does not have some of the required properties [[jobs, on]] ()
				""".trimIndent()
			),
			aFinding(
				issue = "YamlLoadError",
				message = """
					File test.yml could not be loaded:
					```
					java.lang.IllegalStateException: Missing required key: jobs in [foo]
					```
				""".trimIndent()
			),
		)
	}

	@Test fun `valid workflow contents`() {
		val findings = check<ValidationRule>(
			"""
				on: push
				jobs:
				  example:
				    uses: reusable/workflow.yml
			""".trimIndent(),
		)

		findings shouldHave noFindings()
	}
}
