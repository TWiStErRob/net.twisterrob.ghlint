package net.twisterrob.ghlint.rules.shell

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.deleteExisting
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

@Suppress("ForbiddenMethodCall")
internal class ShellCheck(
	private val workingDirectory: Path,
	private val tempDirectory: Path,
) {
	private val available = runCatching {
		arrayOf("shellcheck", "--version").runCommand(workingDirectory)
	}.also { result ->
		if (result.isFailure) {
			println("ShellCheck is not available:\n${result.exceptionOrNull()?.stackTraceToString() ?: "No exception"}")
		}
	}

	fun check(script: String, shell: String?): List<ShellCheckResult> {
		val scriptFile = Files.createTempFile(tempDirectory, "script", ".sh")
		try {
			scriptFile.writeText(script)
			if (available.isFailure) return emptyList()
			@Suppress("detekt.SpreadOperator")
			val json = arrayOf(
				"shellcheck",
				"--enable=all",
				"--severity=style",
				*if (shell != null) arrayOf("--shell=${shell}") else emptyArray(),
				"--format=json",
				scriptFile.toString(),
			).runCommand(workingDirectory)
			val jsonSerializer = Json {
				ignoreUnknownKeys = true
			}
			val result: List<ShellCheckResult> = jsonSerializer
				.decodeFromString<List<ShellCheckResult>>(json)
				.map { sc ->
					sc.copy(
						file = Path.of(sc.file).relativeTo(tempDirectory).toString(),
					)
				}
				.filterNot { it.code in ignoredCodes }
			return result
		} finally {
			scriptFile.deleteExisting()
		}
	}

	companion object {

		private val ignoredCodes = setOf(
			// [error] https://www.shellcheck.net/wiki/SC2148:
			// Tips depend on target shell and yours is unknown. Add a shebang or a 'shell' directive.
			2148,
			// [warning] https://www.shellcheck.net/wiki/SC2154:
			// FOO is referenced but not assigned.
			2154,
		)

		private fun Array<String>.runCommand(
			workingDirectory: Path,
			timeout: Long = 60,
			unit: TimeUnit = TimeUnit.SECONDS,
		): String {
			@Suppress("detekt.SpreadOperator")
			val process = ProcessBuilder(*this)
				.directory(workingDirectory.toFile())
				.redirectOutput(ProcessBuilder.Redirect.PIPE)
				.redirectError(ProcessBuilder.Redirect.PIPE)
				.start()
			var stderr: String? = null
			val errorThread = thread {
				stderr = process.errorStream.bufferedReader().readText()
			}
			val stdout = process.inputStream.bufferedReader().readText()
			process.waitFor(timeout, unit)
			errorThread.join()
			if (!stderr.isNullOrEmpty()) {
				error("Error running command: ${stderr ?: "No stderr"}")
			}
			return when (val exit = process.exitValue()) {
				0 -> stdout
				1 -> stdout
				else -> error("Error running command: exit code ${exit} and message: ${stderr ?: "No stderr"}")
			}
		}
	}
}

@Serializable
internal data class ShellCheckResult(
	val file: String,
	val line: Int,
	val endLine: Int,
	val column: Int,
	val endColumn: Int,
	val level: Level,
	val code: Int,
	val message: String,
) {
	@Suppress("EnumEntryName", "detekt.EnumNaming")
	@Serializable
	enum class Level {
		error, warning, info, style
	}

	val url: URI
		get() = URI.create("https://www.shellcheck.net/wiki/SC${code}")
}
