package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
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
				+ "\tmessage=File test.yml could not be parsed: "
				+ "java.lang.IllegalStateException: Missing required key: jobs in [on]\n"
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
				+ "\tmessage=File test.yml could not be parsed: "
				+ "java.lang.IllegalStateException: Missing required key: jobs in [on]\n"
				+ ")"
				+ "\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun metadata() = test(ValidationRule::class)

	@Test fun `syntax error`() {
		val findings = check<ValidationRule>("<invalid json />")

		findings shouldHave exactFindings(
			aFinding(
				"JsonSchemaValidation",
				"Value is [string] but should be [object] ()"
			),
			aFinding(
				"YamlLoadError",
				"File test.yml could not be parsed: " +
						"java.lang.IllegalArgumentException: Root node is not a mapping: ScalarNode."
			),
		)
	}

	@Test fun `wrong yaml contents`() {
		val findings = check<ValidationRule>("foo: bar")

		findings shouldHave exactFindings(
			aFinding(
				"JsonSchemaValidation",
				"Object does not have some of the required properties [[jobs, on]] ()"
			),
			aFinding(
				"YamlLoadError",
				"File test.yml could not be parsed: "
						+ "java.lang.IllegalStateException: Missing required key: jobs in [foo]"
			),
		)
	}
}
