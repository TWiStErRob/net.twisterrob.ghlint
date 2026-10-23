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

			val configuration = parse(input.toString())

			configuration.files shouldBe listOf(input)
		}

		@Test fun `accepts multiple files in order`() {
			val first = workflow
			val second = action

			val configuration = parse(first.toString(), second.toString())

			configuration.files shouldBe listOf(first, second)
		}

		@Test fun `accepts a file with spaces in its path`() {
			val input = file("workflow with spaces.yml")

			val configuration = parse(input.toString())

			configuration.files shouldBe listOf(input)
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

			val configuration = parse("--", input.toString())

			configuration.files shouldBe listOf(input)
		}
	}

	@Nested
	inner class Root {

		@Test fun `defaults to current directory`() {
			val configuration = parse(workflow.toString())

			configuration.root shouldBe Path.of(".")
		}

		@Test fun `accepts space separated value`() {
			val configuration = parse(workflow.toString(), "--root", temp.toString())

			configuration.root shouldBe temp
		}

		@Test fun `accepts equals separated value`() {
			val configuration = parse(workflow.toString(), "--root=$temp")

			configuration.root shouldBe temp
		}

		@Test fun `accepts directory with spaces in its path`() {
			val root = temp.resolve("repository with spaces").createDirectory()

			val configuration = parse(workflow.toString(), "--root", root.toString())

			configuration.root shouldBe root
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
			val configuration = parse(workflow.toString())

			configuration.isVerbose shouldBe false
		}

		@Test fun `accepts --verbose`() {
			val configuration = parse(workflow.toString(), "--verbose")

			configuration.isVerbose shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--verbose=true") }

			error.plainMessage shouldBe "option --verbose does not take a value"
		}
	}

	@Nested
	inner class ExitCode {

		@Test fun `defaults to false`() {
			val configuration = parse(workflow.toString())

			configuration.isReportExitCode shouldBe false
		}

		@Test fun `accepts --exit`() {
			val configuration = parse(workflow.toString(), "--exit")

			configuration.isReportExitCode shouldBe true
		}

		@Test fun `accepts --ignore-failures`() {
			val configuration = parse(workflow.toString(), "--ignore-failures")

			configuration.isReportExitCode shouldBe false
		}

		@Test fun `ignore failures overrides exit`() {
			val configuration = parse(workflow.toString(), "--exit", "--ignore-failures")

			configuration.isReportExitCode shouldBe false
		}

		@Test fun `exit overrides ignore failures`() {
			val configuration = parse(workflow.toString(), "--ignore-failures", "--exit")

			configuration.isReportExitCode shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--exit=true") }

			error.plainMessage shouldBe "option --exit does not take a value"
		}
	}

	@Nested
	inner class Console {

		@Test fun `defaults to true`() {
			val configuration = parse(workflow.toString())

			configuration.isReportConsole shouldBe true
		}

		@Test fun `accepts --console`() {
			val configuration = parse(workflow.toString(), "--console")

			configuration.isReportConsole shouldBe true
		}

		@Test fun `accepts --silent`() {
			val configuration = parse(workflow.toString(), "--silent")

			configuration.isReportConsole shouldBe false
		}

		@Test fun `silent overrides console`() {
			val configuration = parse(workflow.toString(), "--console", "--silent")

			configuration.isReportConsole shouldBe false
		}

		@Test fun `console overrides silent`() {
			val configuration = parse(workflow.toString(), "--silent", "--console")

			configuration.isReportConsole shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--console=true") }

			error.plainMessage shouldBe "option --console does not take a value"
		}
	}

	@Nested
	inner class GitHubCommands {

		@Test fun `defaults to false`() {
			val configuration = parse(workflow.toString())

			configuration.isReportGitHubCommands shouldBe false
		}

		@Test fun `accepts --ghcommands`() {
			val configuration = parse(workflow.toString(), "--ghcommands")

			configuration.isReportGitHubCommands shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> { parse(workflow.toString(), "--ghcommands=true") }

			error.plainMessage shouldBe "option --ghcommands does not take a value"
		}
	}

	@Nested
	inner class Sarif {

		@Test fun `defaults to no report`() {
			val configuration = parse(workflow.toString())

			configuration.sarifReportLocation shouldBe null
		}

		@Test fun `accepts space separated value`() {
			val report = temp.resolve("report.sarif.json")

			val configuration = parse(workflow.toString(), "--sarif", report.toString())

			configuration.sarifReportLocation shouldBe report

			report.exists() shouldBe false
		}

		@Test fun `accepts equals separated value`() {
			val report = temp.resolve("report.sarif.json")

			val configuration = parse(workflow.toString(), "--sarif=$report")

			configuration.sarifReportLocation shouldBe report
		}

		@Test fun `accepts an existing file`() {
			val report = file("report.sarif.json")

			val configuration = parse(workflow.toString(), "--sarif", report.toString())

			configuration.sarifReportLocation shouldBe report
		}

		@Test fun `accepts path with spaces`() {
			val report = temp.resolve("report with spaces.sarif.json")

			val configuration = parse(workflow.toString(), "--sarif", report.toString())

			configuration.sarifReportLocation shouldBe report
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
