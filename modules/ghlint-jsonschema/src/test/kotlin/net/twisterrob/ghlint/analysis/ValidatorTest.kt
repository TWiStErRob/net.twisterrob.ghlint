package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import org.junit.jupiter.api.Test

class ValidatorTest {

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
