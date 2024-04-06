package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.testIssue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.mockito.Mockito.mock

class ValidatorTest {

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
	@TestFactory fun syntaxErrorMetadata() =
		testIssue(mock(), Validator.SyntaxError)

	@Test fun `no files validates`() {
		val results = Validator().validateWorkflows(emptyList())

		results shouldHave noFindings()
	}

	@Test fun `one valid file validates`() {
		val results = Validator().validateWorkflows(listOf(validFile1))

		results shouldHave noFindings()
	}

	@Test fun `two valid files validates`() {
		val results = Validator().validateWorkflows(listOf(validFile1, validFile2))

		results shouldHave noFindings()
	}

	@Test fun `one invalid file is flagged`() {
		val results = Validator().validateWorkflows(listOf(invalidFile1))

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Object does not have some of the required properties [[jobs]] ()"
		)
	}

	@Test fun `other invalid file is flagged`() {
		val results = Validator().validateWorkflows(listOf(invalidFile2))

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Object has less than 1 properties (/jobs)"
		)
	}

	@Test fun `two invalid files are flagged in order`() {
		val results = Validator().validateWorkflows(listOf(invalidFile1, invalidFile2))

		results shouldHave exactFindings(
			aFinding(
				issue = "JsonSchemaValidation",
				message = "Object does not have some of the required properties [[jobs]] ()"
			),
			aFinding(
				issue = "JsonSchemaValidation",
				message = "Object has less than 1 properties (/jobs)"
			),
		)
	}

	@Test fun `empty file is flagged`() {
		val testFile = File(
			location = FileLocation("empty.yml"),
			content = ""
		)

		val results = Validator().validateWorkflows(listOf(testFile))

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Value is [null] but should be [object] ()"
		)
	}

	@Test fun `newline file is flagged`() {
		val testFile = File(
			location = FileLocation("newline.yml"),
			content = "\n"
		)

		val results = Validator().validateWorkflows(listOf(testFile))

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Value is [null] but should be [object] ()"
		)
	}

	@Test fun `syntax error is flagged`() {
		val testFile = File(
			location = FileLocation("tabs.yml"),
			content = "\t\t"
		)

		val results = Validator().validateWorkflows(listOf(testFile))

		results shouldHave singleFinding(
			issue = "SyntaxError",
			message = """
				Failed to parse YAML: while scanning for the next token
				found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
				 in reader, line 1, column 1:
				    		
				    ^
				
				Full input:
						
			""".trimIndent()
		)
	}

	companion object {

		val validFile1 = File(
			location = FileLocation("test-valid1.yml"),
			content = """
				on: push
				jobs:
				  job:
				    uses: reusable/workflow.yml
			""".trimIndent()
		)

		val validFile2 = File(
			location = FileLocation("test-valid2.yml"),
			content = """
				on: push
				jobs:
				  job:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		val invalidFile1 = File(
			location = FileLocation("test-invalid1.yml"),
			content = """
				on: push
			""".trimIndent()
		)

		val invalidFile2 = File(
			location = FileLocation("test-invalid2.yml"),
			content = """
				on: push
				jobs: {}
			""".trimIndent()
		)
	}
}
