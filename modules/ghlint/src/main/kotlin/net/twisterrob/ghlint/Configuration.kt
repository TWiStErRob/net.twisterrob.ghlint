package net.twisterrob.ghlint

import java.nio.file.Path

public interface Configuration {

	public val root: Path
	public val files: List<Path>

	public val isVerbose: Boolean

	public val isReportConsole: Boolean
	public val sarifReportLocation: Path?
	public val isReportGitHubCommands: Boolean
	public val isReportExitCode: Boolean
}
