package net.twisterrob.ghlint.cli

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.ParameterFormatter
import com.github.ajalt.clikt.parsers.CommandLineParser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.Configuration
import net.twisterrob.ghlint.GHLINT_VERSION
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.writeText

class CLIConfigurationTest {

	@TempDir
	lateinit var temp: Path

	private val workflow: Path by lazy { file("workflow.yml") }
	private val action: Path by lazy { file("action.yml") }

	@Nested
	inner class Files {

		@Test fun `files are required`() {
			val error = shouldThrow<UsageError> { parse("--verbose") }

			error.plainMessage shouldBe "missing argument FILES"
		}

		@Test fun `accepts one file`() {
			val input = workflow

			parse(input.toString()).files shouldBe listOf(input)
		}

		@Test fun `accepts multiple files in order`() {
			val first = workflow
			val second = action

			parse(first.toString(), second.toString()).files shouldBe listOf(first, second)
		}

		@Test fun `accepts a file with spaces in its path`() {
			val input = file("workflow with spaces.yml")

			parse(input.toString()).files shouldBe listOf(input)
		}

		@Test fun `rejects a missing file`() {
			val error = shouldThrow<UsageError> { parse(temp.resolve("missing.yml").toString()) }

			error.plainMessage shouldBe "invalid value for FILES: file \"${temp.resolve("missing.yml")}\" does not exist."
		}

		@Test fun `rejects a directory`() {
			val error = shouldThrow<UsageError> { parse(temp.toString()) }

			error.plainMessage shouldBe "invalid value for FILES: file \"$temp\" is a directory."
		}

		@Test fun `accepts options after files`() {
			val input = workflow
			val configuration = parse(input.toString(), "--verbose")

			configuration.files shouldBe listOf(input)
			configuration.isVerbose shouldBe true
		}

		@Test fun `accepts files after option terminator`() {
			val input = file("--verbose")

			parse("--", input.toString()).files shouldBe listOf(input)
		}
	}

	@Nested
	inner class Root {

		@Test fun `defaults to current directory`() {
			parse(workflow.toString()).root shouldBe Path.of(".")
		}

		@Test fun `accepts space separated value`() {
			parse(workflow.toString(), "--root", temp.toString()).root shouldBe temp
		}

		@Test fun `accepts equals separated value`() {
			parse(workflow.toString(), "--root=$temp").root shouldBe temp
		}

		@Test fun `accepts directory with spaces in its path`() {
			val root = temp.resolve("repository with spaces").createDirectory()

			parse(workflow.toString(), "--root", root.toString()).root shouldBe root
		}

		@Test fun `rejects a missing directory`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--root", temp.resolve("missing").toString()) }

			error.plainMessage shouldBe "invalid value for --root: directory \"${temp.resolve("missing")}\" does not exist."
		}

		@Test fun `rejects a file`() {
			val root = file("not-a-directory")

			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--root", root.toString()) }

			error.plainMessage shouldBe "invalid value for --root: directory \"$root\" is a file."
		}

		@Test fun `requires a value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--root") }

			error.plainMessage shouldBe "option --root requires a value"
		}
	}

	@Nested
	inner class Verbose {

		@Test fun `defaults to false`() {
			parse(workflow.toString()).isVerbose shouldBe false
		}

		@Test fun `accepts --verbose`() {
			parse(workflow.toString(), "--verbose").isVerbose shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--verbose=true") }

			error.plainMessage shouldBe "option --verbose does not take a value"
		}
	}

	@Nested
	inner class ExitCode {

		@Test fun `defaults to false`() {
			parse(workflow.toString()).isReportExitCode shouldBe false
		}

		@Test fun `accepts --exit`() {
			parse(workflow.toString(), "--exit").isReportExitCode shouldBe true
		}

		@Test fun `accepts --ignore-failures`() {
			parse(workflow.toString(), "--ignore-failures").isReportExitCode shouldBe false
		}

		@Test fun `last flag wins`() {
			parse(workflow.toString(), "--exit", "--ignore-failures").isReportExitCode shouldBe false
			parse(workflow.toString(), "--ignore-failures", "--exit").isReportExitCode shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--exit=true") }

			error.plainMessage shouldBe "option --exit does not take a value"
		}
	}

	@Nested
	inner class Console {

		@Test fun `defaults to true`() {
			parse(workflow.toString()).isReportConsole shouldBe true
		}

		@Test fun `accepts --console`() {
			parse(workflow.toString(), "--console").isReportConsole shouldBe true
		}

		@Test fun `accepts --silent`() {
			parse(workflow.toString(), "--silent").isReportConsole shouldBe false
		}

		@Test fun `last flag wins`() {
			parse(workflow.toString(), "--console", "--silent").isReportConsole shouldBe false
			parse(workflow.toString(), "--silent", "--console").isReportConsole shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--console=true") }

			error.plainMessage shouldBe "option --console does not take a value"
		}
	}

	@Nested
	inner class GitHubCommands {

		@Test fun `defaults to false`() {
			parse(workflow.toString()).isReportGitHubCommands shouldBe false
		}

		@Test fun `accepts --ghcommands`() {
			parse(workflow.toString(), "--ghcommands").isReportGitHubCommands shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--ghcommands=true") }

			error.plainMessage shouldBe "option --ghcommands does not take a value"
		}
	}

	@Nested
	inner class Sarif {

		@Test fun `defaults to no report`() {
			parse(workflow.toString()).sarifReportLocation shouldBe null
		}

		@Test fun `accepts space separated value`() {
			val report = temp.resolve("report.sarif.json")

			parse(workflow.toString(), "--sarif", report.toString()).sarifReportLocation shouldBe report

			report.exists() shouldBe false
		}

		@Test fun `accepts equals separated value`() {
			val report = temp.resolve("report.sarif.json")

			parse(workflow.toString(), "--sarif=$report").sarifReportLocation shouldBe report
		}

		@Test fun `accepts an existing file`() {
			val report = file("report.sarif.json")

			parse(workflow.toString(), "--sarif", report.toString()).sarifReportLocation shouldBe report
		}

		@Test fun `accepts path with spaces`() {
			val report = temp.resolve("report with spaces.sarif.json")

			parse(workflow.toString(), "--sarif", report.toString()).sarifReportLocation shouldBe report
		}

		@Test fun `rejects a directory`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--sarif", temp.toString()) }

			error.plainMessage shouldBe "invalid value for --sarif: file \"$temp\" is a directory."
		}

		@Test fun `requires a value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--sarif") }

			error.plainMessage shouldBe "option --sarif requires a value"
		}
	}

	@Nested
	inner class Help {

		@Test fun `accepts --help without files`() {
			shouldThrow<PrintHelpMessage> { parse("--help") }
		}

		@Test fun `accepts -h without files`() {
			shouldThrow<PrintHelpMessage> { parse("-h") }
		}

		@Test fun `empty arguments print help`() {
			shouldThrow<PrintHelpMessage> { parse() }
		}
	}

	@Nested
	inner class Version {

		@Test fun `accepts --version without files`() {
			val result = shouldThrow<PrintMessage> { parse("--version") }

			result.message shouldBe "GH-Lint (ghlint) version $GHLINT_VERSION"
		}

		@Test fun `accepts -v without files`() {
			val result = shouldThrow<PrintMessage> { parse("-v") }

			result.message shouldBe "GH-Lint (ghlint) version $GHLINT_VERSION"
		}
	}

	private val UsageError.plainMessage: String
		get() = formatMessage(checkNotNull(context).localization, ParameterFormatter.Plain)

	private fun file(name: String): Path = temp.resolve(name).apply { writeText("{}") }

	private fun parse(vararg argv: String): Configuration {
		val cli = CLI()
		// Finalize the real CLI parameters without running the linter.
		CommandLineParser.parseAndRun(cli, argv.toList()) { }
		return cli
	}
}
