package net.twisterrob.ghlint

import java.nio.file.Path

@Suppress("detekt.LongParameterList")
class FakeConfiguration(
	override val root: Path,
	override val files: List<Path> = emptyList(),
	override val isVerbose: Boolean = false,
	override val isReportConsole: Boolean = false,
	override val sarifReportLocation: Path? = null,
	override val isReportGitHubCommands: Boolean = false,
	override val isReportExitCode: Boolean = false,
) : Configuration
