package net.twisterrob.ghlint

import io.kotest.matchers.paths.shouldContainFile
import io.kotest.matchers.paths.shouldContainNFiles
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import io.kotest.matchers.string.beEmpty
import io.kotest.matchers.string.match
import net.twisterrob.ghlint.GHLintTest.Fixtures.errorFile
import net.twisterrob.ghlint.GHLintTest.Fixtures.errorFileMessage
import net.twisterrob.ghlint.GHLintTest.Fixtures.invalidFile1
import net.twisterrob.ghlint.GHLintTest.Fixtures.invalidFile2
import net.twisterrob.ghlint.GHLintTest.Fixtures.validFile1
import net.twisterrob.ghlint.GHLintTest.Fixtures.validFile2
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.model.name
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.test.captureSystemStreams
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.writeText

class GHLintTest {

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `no files`(@TempDir tempDir: Path) {
		val result = captureSystemStreams {
			GHLint().run(FakeConfiguration(tempDir, emptyList(), isReportExitCode = true))
		}

		result.out should beEmpty()
		result.err should beEmpty()
		result.result shouldBe 0
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `single valid file`(@TempDir tempDir: Path) {
		val test = validFile1.writeTo(tempDir)

		val result = captureSystemStreams {
			GHLint().run(FakeConfiguration(tempDir, listOf(test), isReportExitCode = true))
		}

		result.out should beEmpty()
		result.err should beEmpty()
		result.result shouldBe 0
		tempDir shouldContainFile validFile1.location.name
		tempDir shouldContainNFiles 1
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `single invalid file`(@TempDir tempDir: Path) {
		val test = invalidFile1.writeTo(tempDir)

		val result = captureSystemStreams {
			GHLint().run(FakeConfiguration(tempDir, listOf(test), isReportExitCode = true))
		}

		result.out should beEmpty()
		result.err should beEmpty()
		result.result shouldBe 1
		tempDir shouldContainFile invalidFile1.location.name
		tempDir shouldContainNFiles 1
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `single valid file in verbose mode`(@TempDir tempDir: Path) {
		val test = validFile1.writeTo(tempDir)

		val result = captureSystemStreams {
			GHLint().run(
				FakeConfiguration(
					root = tempDir,
					files = listOf(test),
					isReportExitCode = true,
					isVerbose = true
				)
			)
		}

		result.result shouldBe 0
		result.err should beEmpty()
		result.out shouldMatchEntire """
			Received the following files for analysis against JSON-schema and rules:
			 * ${test.absolute()}
			Analyzing ${test.absolute()}... found 0 findings in ${timing}.
			Total: 0 findings in ${timing}.
			There are 0 findings.
			Exiting with code 0.
			
		""".trimIndent()
		tempDir shouldContainFile test.fileName.toString()
		tempDir shouldContainNFiles 1
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `some invalid files in verbose mode`(@TempDir tempDir: Path) {
		val test1 = validFile1.writeTo(tempDir)
		val test2 = invalidFile1.writeTo(tempDir)
		val test3 = validFile2.writeTo(tempDir)
		val test4 = invalidFile2.writeTo(tempDir)

		val result = captureSystemStreams {
			GHLint().run(
				FakeConfiguration(
					root = tempDir,
					files = listOf(test1, test2, test3, test4),
					isReportExitCode = true,
					isVerbose = true,
				)
			)
		}

		result.result shouldBe 1
		result.err should beEmpty()
		result.out shouldMatchEntire """
			Received the following files for analysis against JSON-schema and rules:
			 * ${test1.absolute()}
			 * ${test2.absolute()}
			 * ${test3.absolute()}
			 * ${test4.absolute()}
			Analyzing ${test1.absolute()}... found 0 findings in ${timing}.
			Analyzing ${test2.absolute()}... found 2 findings in ${timing}.
			Analyzing ${test3.absolute()}... found 0 findings in ${timing}.
			Analyzing ${test4.absolute()}... found 2 findings in ${timing}.
			Total: 4 findings in ${timing}.
			There are 4 findings.
			Exiting with code 1.
			
		""".trimIndent()
		tempDir shouldContainFile test1.fileName.toString()
		tempDir shouldContainFile test2.fileName.toString()
		tempDir shouldContainFile test3.fileName.toString()
		tempDir shouldContainFile test4.fileName.toString()
		tempDir shouldContainNFiles 4
	}

	@Nested
	inner class AnalyzeTest {

		private fun analyze(vararg files: RawFile): List<Finding> =
			GHLint().analyze(files.toList(), listOf(BuiltInRuleSet()), false)

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
					message = """
						Object does not have some of the required properties [[jobs]] ()
					""".trimIndent()
				),
				aFinding(
					issue = "YamlLoadError",
					location = "test-invalid1.yml/1:1-1:9",
					message = """
						File test-invalid1.yml could not be loaded:
						```
						java.lang.IllegalStateException: Missing required key: jobs in [on]
						```
					""".trimIndent()
				),
			)
		}

		@Test fun `other invalid file is flagged`() {
			val results = analyze(invalidFile2)

			results shouldHave singleFinding(
				issue = "JsonSchemaValidation",
				location = "test-invalid2.yml/2:7-2:9",
				message = """
					Object has less than 1 properties (/jobs)
				""".trimIndent()
			)
		}

		@Test fun `two invalid files are flagged in order`() {
			val results = analyze(invalidFile1, invalidFile2)

			results shouldHave exactFindings(
				aFinding(
					issue = "JsonSchemaValidation",
					location = "test-invalid1.yml/1:1-1:9",
					message = """
						Object does not have some of the required properties [[jobs]] ()
					""".trimIndent()
				),
				aFinding(
					issue = "YamlLoadError",
					location = "test-invalid1.yml/1:1-1:9",
					message = """
						File test-invalid1.yml could not be loaded:
						```
						java.lang.IllegalStateException: Missing required key: jobs in [on]
						```
					""".trimIndent()
				),
				aFinding(
					issue = "JsonSchemaValidation",
					location = "test-invalid2.yml/2:7-2:9",
					message = """
						Object has less than 1 properties (/jobs)
					""".trimIndent()
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
					message = """
						Value is [null] but should be [object] ()
					""".trimIndent()
				),
				aFinding(
					issue = "YamlLoadError",
					location = "empty.yml/1:1-1:1",
					message = """
						File empty.yml could not be loaded:
						```
						java.lang.IllegalArgumentException: Root node is not a mapping: ScalarNode.
						```
					""".trimIndent()
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
					message = """
						Value is [null] but should be [object] ()
					""".trimIndent()
				),
				aFinding(
					issue = "YamlLoadError",
					location = "newline.yml/1:1-2:1",
					message = """
						File newline.yml could not be loaded:
						```
						java.lang.IllegalArgumentException: Root node is not a mapping: ScalarNode.
						```
					""".trimIndent()
				),
			)
		}

		@Test fun `syntax error is flagged`() {
			val results = analyze(errorFile)

			results shouldHave singleFinding(
				issue = "YamlSyntaxError",
				location = "tabs.yml/1:1-1:3",
				message = errorFileMessage
			)
		}

		@Test fun `invalid file after syntax error is still flagged`() {
			val results = analyze(validFile1, errorFile, invalidFile1)

			results shouldHave exactFindings(
				aFinding(
					issue = "YamlSyntaxError",
					location = "tabs.yml/1:1-1:3",
					message = errorFileMessage
				),
				aFinding(
					issue = "JsonSchemaValidation",
					location = "test-invalid1.yml/1:1-1:9",
					message = """
						Object does not have some of the required properties [[jobs]] ()
					""".trimIndent()
				),
				aFinding(
					issue = "YamlLoadError",
					location = "test-invalid1.yml/1:1-1:9",
					message = """
						File test-invalid1.yml could not be loaded:
						```
						java.lang.IllegalStateException: Missing required key: jobs in [on]
						```
					""".trimIndent()
				),
			)
		}
	}

	companion object {

		@Language("RegExp")
		private val timing = """\E\d+(\.\d+)?m?s\Q"""

		private fun RawFile.writeTo(tempDir: Path): Path {
			val file = tempDir.resolve(location.path)
			file.writeText(content)
			return file
		}
	}

	object Fixtures {

		val validFile1 = RawFile(
			location = FileLocation("test-valid1.yml"),
			content = """
				name: "Valid workflow #1"
				on: push
				jobs:
				  test:
				    name: "Test"
				    uses: reusable/workflow.yml
				    permissions: {}
			""".trimIndent()
		)

		val validFile2 = RawFile(
			location = FileLocation("test-valid2.yml"),
			content = """
				name: "Valid workflow #2"
				on: push
				jobs:
				  test:
				    name: "Test"
				    timeout-minutes: 10
				    runs-on: ubuntu-latest
				    permissions: {}
				    steps:
				      - name: "Checkout"
				        uses: actions/checkout@v4
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
			File tabs.yml could not be parsed:
			```
			java.lang.IllegalArgumentException: Failed to parse YAML: while scanning for the next token
			found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
			 in reader, line 1, column 1:
			    ${errorFile.content}
			    ^
			
			```
		""".trimIndent()
	}
}

private infix fun <A : CharSequence> A?.shouldMatchEntire(regex: String): A {
	this should match("""\Q${regex.replace("\n", System.lineSeparator())}\E""")
	return this!!
}
