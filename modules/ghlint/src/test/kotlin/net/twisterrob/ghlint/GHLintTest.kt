package net.twisterrob.ghlint

import io.kotest.matchers.paths.shouldContainFile
import io.kotest.matchers.paths.shouldContainNFiles
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.GHLintTest.Fixtures.errorFile
import net.twisterrob.ghlint.GHLintTest.Fixtures.errorFileMessage
import net.twisterrob.ghlint.GHLintTest.Fixtures.invalidFile1
import net.twisterrob.ghlint.GHLintTest.Fixtures.invalidFile2
import net.twisterrob.ghlint.GHLintTest.Fixtures.validFile1
import net.twisterrob.ghlint.GHLintTest.Fixtures.validFile2
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.nio.file.Path
import kotlin.io.path.writeText

class GHLintTest {

	@Test
	fun `no files`() {
		val configuration: Configuration = mock()
		whenever(configuration.files).thenReturn(emptyList())

		val result = GHLint().run(configuration)

		result shouldBe 0
	}

	@Test
	fun `single valid file`(@TempDir tempDir: Path) {
		val test = tempDir.resolve("test.yml")
		test.writeText(
			"""
				name: "Test"
				on: push
				jobs:
				  test:
				    name: "Test"
				    runs-on: ubuntu-latest
				    timeout-minutes: 1
				    permissions: {}
				    steps:
				      - name: "Test"
				        shell: bash
				        run: echo "Test"
			""".trimIndent()
		)
		val configuration: Configuration = mock()
		whenever(configuration.root).thenReturn(tempDir)
		whenever(configuration.files).thenReturn(listOf(test))
		whenever(configuration.isReportExitCode).thenReturn(true)

		val result = GHLint().run(configuration)

		result shouldBe 0
		tempDir shouldContainFile "test.yml"
		tempDir shouldContainNFiles 1
	}

	@Test
	fun `single invalid file`(@TempDir tempDir: Path) {
		val test = tempDir.resolve("test.yml")
		test.writeText(
			"""
			""".trimIndent()
		)
		val configuration: Configuration = mock()
		whenever(configuration.root).thenReturn(tempDir)
		whenever(configuration.files).thenReturn(listOf(test))
		whenever(configuration.isReportExitCode).thenReturn(true)

		val result = GHLint().run(configuration)

		result shouldBe 1
		tempDir shouldContainFile "test.yml"
		tempDir shouldContainNFiles 1
	}

	@Nested
	inner class AnalyzeTest {

		private fun analyze(vararg files: RawFile): List<Finding> =
			GHLint().analyze(files.toList(), listOf(BuiltInRuleSet()))

		@Test fun `no files validates`() {
			val results = analyze(/* nothing */)

			results shouldHave noFindings()
		}

		@Test fun `one valid file validates`() {
			val results = analyze(validFile1)

			results shouldHave noFindings()
		}

		@Test fun `two valid files validates`() {
			val results = analyze(validFile1, validFile2)

			results shouldHave noFindings()
		}

		@Test fun `one invalid file is flagged`() {
			val results = analyze(invalidFile1)

			results shouldHave exactFindings(
				aFinding(
					issue = "JsonSchemaValidation",
					location = "test-invalid1.yml/1:1-1:9",
					message = "Object does not have some of the required properties [[jobs]] ()"
				),
				aFinding(
					issue = "YamlLoadError",
					location = "test-invalid1.yml/1:1-1:8",
					message = "File test-invalid1.yml could not be parsed: "
							+ "java.lang.IllegalStateException: Missing required key: jobs in [on]"
				),
			)
		}

		@Test fun `other invalid file is flagged`() {
			val results = analyze(invalidFile2)

			results shouldHave singleFinding(
				issue = "JsonSchemaValidation",
				location = "test-invalid2.yml/2:7-2:9",
				message = "Object has less than 1 properties (/jobs)"
			)
		}

		@Test fun `two invalid files are flagged in order`() {
			val results = analyze(invalidFile1, invalidFile2)

			results shouldHave exactFindings(
				aFinding(
					issue = "JsonSchemaValidation",
					location = "test-invalid1.yml/1:1-1:9",
					message = "Object does not have some of the required properties [[jobs]] ()"
				),
				aFinding(
					issue = "YamlLoadError",
					location = "test-invalid1.yml/1:1-1:8",
					message = "File test-invalid1.yml could not be parsed: "
							+ "java.lang.IllegalStateException: Missing required key: jobs in [on]"
				),
				aFinding(
					issue = "JsonSchemaValidation",
					location = "test-invalid2.yml/2:7-2:9",
					message = "Object has less than 1 properties (/jobs)"
				),
			)
		}

		@Test fun `empty file is flagged`() {
			val testFile = RawFile(
				location = FileLocation("empty.yml"),
				content = ""
			)

			val results = analyze(testFile)

			results shouldHave exactFindings(
				aFinding(
					issue = "JsonSchemaValidation",
					location = "empty.yml/1:1-1:1",
					message = "Value is [null] but should be [object] ()"
				),
				aFinding(
					issue = "YamlLoadError",
					location = "empty.yml/1:1-1:0",
					message = "File empty.yml could not be parsed: "
							+ "java.lang.IllegalArgumentException: Root node is not a mapping: ScalarNode."
				),
			)
		}

		@Test fun `newline file is flagged`() {
			val testFile = RawFile(
				location = FileLocation("newline.yml"),
				content = "\n"
			)

			val results = analyze(testFile)

			results shouldHave exactFindings(
				aFinding(
					issue = "JsonSchemaValidation",
					location = "newline.yml/1:1-1:1",
					message = "Value is [null] but should be [object] ()"
				),
				aFinding(
					issue = "YamlLoadError",
					location = "newline.yml/1:1-2:0",
					message = "File newline.yml could not be parsed: "
							+ "java.lang.IllegalArgumentException: Root node is not a mapping: ScalarNode."
				),
			)
		}

		@Test fun `syntax error is flagged`() {
			val results = analyze(errorFile)

			results shouldHave singleFinding(
				issue = "YamlSyntaxError",
				location = "tabs.yml/1:1-1:2",
				message = errorFileMessage
			)
		}

		@Test fun `invalid file after syntax error is still flagged`() {
			val results = analyze(validFile1, errorFile, invalidFile1)

			results shouldHave exactFindings(
				aFinding(
					issue = "YamlSyntaxError",
					location = "tabs.yml/1:1-1:2",
					message = errorFileMessage
				),
				aFinding(
					issue = "JsonSchemaValidation",
					location = "test-invalid1.yml/1:1-1:9",
					message = "Object does not have some of the required properties [[jobs]] ()"
				),
				aFinding(
					issue = "YamlLoadError",
					location = "test-invalid1.yml/1:1-1:8",
					message = "File test-invalid1.yml could not be parsed: "
							+ "java.lang.IllegalStateException: Missing required key: jobs in [on]"
				),
			)
		}
	}

	object Fixtures {

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
			File tabs.yml could not be parsed: java.lang.IllegalArgumentException: Failed to parse YAML: while scanning for the next token
			found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
			 in reader, line 1, column 1:
			    ${errorFile.content}
			    ^
			
			Full input (tabs.yml):
			${errorFile.content}
		""".trimIndent()
	}
}
