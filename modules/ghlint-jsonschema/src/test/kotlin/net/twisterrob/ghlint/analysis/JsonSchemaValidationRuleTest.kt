package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.testIssue
import org.junit.jupiter.api.TestFactory

class JsonSchemaValidationRuleTest {

	@TestFactory fun metadata() = test(JsonSchemaValidationRule::class)

	@AcceptFailingDynamicTest(
		displayName = "Issue SyntaxError non-compliant example #1 has findings",
		reason = "Parsing will throw an error, so it never gets to validate the example, covered in ValidatorTest.",
		acceptableFailure = "^\\Q"
				+ "Failed to parse YAML: while scanning for the next token\n"
				+ "found character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) for indentation)\n"
				+ " in reader, line 3, column 1:\n"
				+ "    \texample:\n"
				+ "    ^\n"
				+ "\n"
				+ "Full input:\n"
				+ "on: push\n"
				+ "jobs:\n"
				+ "\texample:\n"
				+ "\t\tuses: reusable/workflow.yml"
				+ "\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun errorMetadata() =
		testIssue(JsonSchemaValidationRule(), JsonSchemaValidationRule.SyntaxError)
}
