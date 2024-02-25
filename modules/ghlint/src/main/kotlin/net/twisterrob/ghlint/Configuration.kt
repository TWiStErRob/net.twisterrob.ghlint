package net.twisterrob.ghlint

import java.nio.file.Path

public interface Configuration {

	public val files: List<Path>
	public val verbose: Boolean
	public val reportConsole: Boolean
	public val reportSarif: Path?
	public val reportGitHubCommands: Boolean
}
