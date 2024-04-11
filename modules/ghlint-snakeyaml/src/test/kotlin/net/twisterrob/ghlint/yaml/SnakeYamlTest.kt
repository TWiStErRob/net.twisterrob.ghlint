package net.twisterrob.ghlint.yaml

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.analysis.JsonSchemaRuleSet
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.ruleset.RuleSet
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import org.junit.jupiter.api.Test

class SnakeYamlTest {

	private val standardRuleSets: List<RuleSet> = listOf(JsonSchemaRuleSet())

	@Test fun `no files validates`() {
		val results = SnakeYaml.analyze(emptyList(), standardRuleSets)

		results shouldHave noFindings()
	}

	@Test fun `one valid file validates`() {
		val results = SnakeYaml.analyze(listOf(validFile1), standardRuleSets)

		results shouldHave noFindings()
	}

	@Test fun `two valid files validates`() {
		val results = SnakeYaml.analyze(listOf(validFile1, validFile2), standardRuleSets)

		results shouldHave noFindings()
	}

	@Test fun `one invalid file is flagged`() {
		val results = SnakeYaml.analyze(listOf(invalidFile1), standardRuleSets)

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Object does not have some of the required properties [[jobs]] ()"
		)
	}

	@Test fun `other invalid file is flagged`() {
		val results = SnakeYaml.analyze(listOf(invalidFile2), standardRuleSets)

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = "Object has less than 1 properties (/jobs)"
		)
	}

	@Test fun `two invalid files are flagged in order`() {
		val results = SnakeYaml.analyze(listOf(invalidFile1, invalidFile2), standardRuleSets)

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
		val testFile = RawFile(
			location = FileLocation("empty.yml"),
			content = ""
		)

		val results = SnakeYaml.analyze(listOf(testFile), standardRuleSets)

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			// JSON Schema Error would be: "Value is [null] but should be [object] ()"
			message = "File could not be parsed: java.lang.IllegalArgumentException: " +
					"Root node is not a mapping: ScalarNode.\n${testFile.content}"
		)
	}

	@Test fun `newline file is flagged`() {
		val testFile = RawFile(
			location = FileLocation("newline.yml"),
			content = "\n"
		)

		val results = SnakeYaml.analyze(listOf(testFile), standardRuleSets)

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			// JSON Schema Error would be: "Value is [null] but should be [object] ()"
			message = "File could not be parsed: java.lang.IllegalArgumentException: " +
					"Root node is not a mapping: ScalarNode.\n${testFile.content}"
		)
	}

	@Test fun `syntax error is flagged`() {
		val results = SnakeYaml.analyze(listOf(errorFile), standardRuleSets)

		results shouldHave singleFinding(
			issue = "JsonSchemaValidation",
			message = errorFileMessage
		)
	}

	@Test fun `invalid file after syntax error is still flagged`() {
		val results = SnakeYaml.analyze(listOf(validFile1, errorFile, invalidFile1), standardRuleSets)

		results shouldHave exactFindings(
			aFinding(
				issue = "JsonSchemaValidation",
				message = errorFileMessage
			),
			aFinding(
				issue = "JsonSchemaValidation",
				message = "Object does not have some of the required properties [[jobs]] ()"
			),
		)
	}

	companion object {

		val validFile1 = RawFile(
			location = FileLocation("test-valid1.yml"),
			content = """
				on: push
				jobs:
				  job:
				    uses: reusable/workflow.yml
			""".trimIndent()
		)

		val validFile2 = RawFile(
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

		val invalidFile1 = RawFile(
			location = FileLocation("test-invalid1.yml"),
			content = """
				on: push
			""".trimIndent()
		)

		val invalidFile2 = RawFile(
			location = FileLocation("test-invalid2.yml"),
			content = """
				on: push
				jobs: {}
			""".trimIndent()
		)

		val errorFile = RawFile(
			location = FileLocation("tabs.yml"),
			content = "\t\t"
		)

		val errorFileMessage = """
			File could not be parsed: java.lang.IllegalArgumentException: Failed to parse YAML: while scanning for the next token
			found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
			 in reader, line 1, column 1:
			    ${errorFile.content}
			    ^
			
			Full input (tabs.yml):
			${errorFile.content}
			${errorFile.content}
		""".trimIndent()
	}
}
