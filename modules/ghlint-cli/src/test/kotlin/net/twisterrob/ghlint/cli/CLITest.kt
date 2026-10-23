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
import org.junit.jupiter.api.extension.AnnotatedElementContext
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.io.TempDirFactory
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.exists

class CLITest {

	@TempDir
	lateinit var temp: Path

	private val workflow: Path by lazy { file("workflow.yml") }
	private val action: Path by lazy { file("action.yml") }
	private val files: Array<String> by lazy { arrayOf(workflow.toString(), action.toString()) }

	@Test fun defaults() {
		val configuration = parse(workflow.toString())

		configuration.files shouldBe listOf(workflow)
		configuration.root shouldBe Path.of(".")
		configuration.isVerbose shouldBe false
		configuration.isReportExitCode shouldBe false
		configuration.isReportConsole shouldBe true
		configuration.isReportGitHubCommands shouldBe false
		configuration.sarifReportLocation shouldBe null
	}

	@Test fun `reporting options are independent`() {
		val report = temp.resolve("report.sarif.json")

		val configuration = parse(*files, "--exit", "--silent", "--verbose", "--ghcommands", "--sarif", report.toString())

		configuration.isReportExitCode shouldBe true
		configuration.isReportConsole shouldBe false
		configuration.isVerbose shouldBe true
		configuration.isReportGitHubCommands shouldBe true
		configuration.sarifReportLocation shouldBe report
	}

	@Test fun `rejects unknown options`() {
		val error = shouldThrow<UsageError> {
			parse(*files, "--unknown")
		}

		error.plainMessage shouldBe """no such option --unknown"""
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

			result.message shouldBe """GH-Lint (ghlint) version ${GHLINT_VERSION}"""
		}

		@Test fun `accepts -v without files`() {
			val result = shouldThrow<PrintMessage> { parse("-v") }

			result.message shouldBe """GH-Lint (ghlint) version ${GHLINT_VERSION}"""
		}
	}

	@Nested
	inner class Files {

		@Test fun `files are required`() {
			val error = shouldThrow<UsageError> {
				parse("--verbose")
			}

			error.plainMessage shouldBe """missing argument FILES"""
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
			val file = file("workflow with spaces.yml")

			val configuration = parse(file.toString())

			configuration.files shouldBe listOf(file)
		}

		@Test fun `rejects a missing file`() {
			val file = temp.resolve("missing.yml")

			val error = shouldThrow<UsageError> {
				parse(file.toString())
			}

			error.plainMessage shouldBe """invalid value for FILES: file "${file}" does not exist."""
		}

		@Test fun `rejects a directory`() {
			val folder = temp

			val error = shouldThrow<UsageError> {
				parse(folder.toString())
			}

			error.plainMessage shouldBe """invalid value for FILES: file "${folder}" is a directory."""
		}

		@Test fun `accepts options before files`() {
			val input = workflow
			val configuration = parse("--verbose", input.toString())

			configuration.files shouldBe listOf(input)
			configuration.isVerbose shouldBe true
		}

		@Test fun `accepts options after files`() {
			val input = workflow
			val configuration = parse(input.toString(), "--verbose")

			configuration.files shouldBe listOf(input)
			configuration.isVerbose shouldBe true
		}

		@Test fun `accepts options between files`() {
			val configuration = parse(workflow.toString(), "--verbose", action.toString())

			configuration.files shouldBe listOf(workflow, action)
			configuration.isVerbose shouldBe true
		}

		@Test fun `accepts files after option terminator`(
			@TempDir(factory = OptionLikeTempDirFactory::class) directory: Path,
		) {
			val file = directory.resolve("workflow.yml").createFile()
			val input = Path.of("").toAbsolutePath().relativize(file)

			val configuration = parse("--", input.toString())

			configuration.files shouldBe listOf(input)
		}
	}

	@Nested
	inner class Root {

		@Test fun `accepts space separated value`() {
			val configuration = parse(*files, "--root", temp.toString())

			configuration.root shouldBe temp
		}

		@Test fun `accepts equals separated value`() {
			val configuration = parse(*files, "--root=${temp}")

			configuration.root shouldBe temp
		}

		@Test fun `accepts directory with spaces in its path`() {
			val root = temp.resolve("repository with spaces").createDirectory()

			val configuration = parse(*files, "--root", root.toString())

			configuration.root shouldBe root
		}

		@Test fun `rejects a missing directory`() {
			val root = temp.resolve("missing")

			val error = shouldThrow<UsageError> {
				parse(*files, "--root", root.toString())
			}

			error.plainMessage shouldBe """invalid value for --root: directory "${root}" does not exist."""
		}

		@Test fun `rejects a file`() {
			val root = file("not-a-directory")

			val error = shouldThrow<UsageError> {
				parse(*files, "--root", root.toString())
			}

			error.plainMessage shouldBe """invalid value for --root: directory "${root}" is a file."""
		}

		@Test fun `requires a value`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--root")
			}

			error.plainMessage shouldBe """option --root requires a value"""
		}

		@Test fun `rejects repeated values`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--root", temp.toString(), "--root=${temp}")
			}

			error.plainMessage shouldBe """invalid value for --root: cannot be repeated"""
		}
	}

	@Nested
	inner class Verbose {

		@Test fun `accepts --verbose`() {
			val configuration = parse(*files, "--verbose")

			configuration.isVerbose shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--verbose=true")
			}

			error.plainMessage shouldBe """option --verbose does not take a value"""
		}
	}

	@Nested
	inner class ExitCode {

		@Test fun `accepts --exit`() {
			val configuration = parse(*files, "--exit")

			configuration.isReportExitCode shouldBe true
		}

		@Test fun `accepts --ignore-failures`() {
			val configuration = parse(*files, "--ignore-failures")

			configuration.isReportExitCode shouldBe false
		}

		@Test fun `ignore failures overrides exit`() {
			val configuration = parse(*files, "--exit", "--ignore-failures")

			configuration.isReportExitCode shouldBe false
		}

		@Test fun `exit overrides ignore failures`() {
			val configuration = parse(*files, "--ignore-failures", "--exit")

			configuration.isReportExitCode shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--exit=true")
			}

			error.plainMessage shouldBe """option --exit does not take a value"""
		}
	}

	@Nested
	inner class Console {

		@Test fun `accepts --console`() {
			val configuration = parse(*files, "--console")

			configuration.isReportConsole shouldBe true
		}

		@Test fun `accepts --silent`() {
			val configuration = parse(*files, "--silent")

			configuration.isReportConsole shouldBe false
		}

		@Test fun `silent overrides console`() {
			val configuration = parse(*files, "--console", "--silent")

			configuration.isReportConsole shouldBe false
		}

		@Test fun `console overrides silent`() {
			val configuration = parse(*files, "--silent", "--console")

			configuration.isReportConsole shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--console=true")
			}

			error.plainMessage shouldBe """option --console does not take a value"""
		}
	}

	@Nested
	inner class GitHubCommands {

		@Test fun `accepts --ghcommands`() {
			val configuration = parse(*files, "--ghcommands")

			configuration.isReportGitHubCommands shouldBe true
		}

		@Test fun `rejects an attached value`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--ghcommands=true")
			}

			error.plainMessage shouldBe """option --ghcommands does not take a value"""
		}
	}

	@Nested
	inner class Sarif {

		@Test fun `accepts space separated value`() {
			val report = temp.resolve("report.sarif.json")

			val configuration = parse(*files, "--sarif", report.toString())

			configuration.sarifReportLocation shouldBe report

			report.exists() shouldBe false
		}

		@Test fun `accepts equals separated value`() {
			val report = temp.resolve("report.sarif.json")

			val configuration = parse(*files, "--sarif=${report}")

			configuration.sarifReportLocation shouldBe report
		}

		@Test fun `accepts an existing file`() {
			val report = file("report.sarif.json")

			val configuration = parse(*files, "--sarif", report.toString())

			configuration.sarifReportLocation shouldBe report
		}

		@Test fun `accepts path with spaces`() {
			val report = temp.resolve("report with spaces.sarif.json")

			val configuration = parse(*files, "--sarif", report.toString())

			configuration.sarifReportLocation shouldBe report
		}

		@Test fun `rejects a directory`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--sarif", temp.toString())
			}

			error.plainMessage shouldBe """invalid value for --sarif: file "${temp}" is a directory."""
		}

		@Test fun `requires a value`() {
			val error = shouldThrow<UsageError> {
				parse(*files, "--sarif")
			}

			error.plainMessage shouldBe """option --sarif requires a value"""
		}

		@Test fun `rejects repeated values`() {
			val report = temp.resolve("report.sarif.json")

			val error = shouldThrow<UsageError> {
				parse(*files, "--sarif", report.toString(), "--sarif=${report}")
			}

			error.plainMessage shouldBe """invalid value for --sarif: cannot be repeated"""
		}
	}

	private class OptionLikeTempDirFactory : TempDirFactory {
		override fun createTempDirectory(
			elementContext: AnnotatedElementContext,
			extensionContext: ExtensionContext,
		): Path =
			// Keep the fixture on the working directory's drive so its relative path can start with --.
			java.nio.file.Files.createTempDirectory(Path.of("").toAbsolutePath(), "--ghlint-")
	}

	private fun file(name: String): Path =
		temp.resolve(name).apply { createFile() }

	companion object {

		private val UsageError.plainMessage: String
			get() = formatMessage(checkNotNull(context).localization, ParameterFormatter.Plain)

		private fun parse(vararg argv: String): Configuration {
			val cli = CLI()
			CommandLineParser.parseAndRun(cli, argv.toList()) { /* no it.run() */ }
			return cli
		}
	}
}
