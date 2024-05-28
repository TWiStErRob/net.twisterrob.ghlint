package net.twisterrob.ghlint.model

private val GH_CLI_START_OF_LINE = Regex("""^\s*gh\s+""", RegexOption.MULTILINE)
private val GH_CLI_PIPE_CONDITIONAL = Regex("""(&&|\|\||\|)\s*gh\s+""")
private val GH_CLI_EMBEDDED = Regex("""\$\(\s*gh\s+""")

public fun Step.usesGhCli(): Boolean =
	this is Step.Run && this.usesGhCli()

public fun Step.Run.usesGhCli(): Boolean =
	GH_CLI_START_OF_LINE in this.run
			|| GH_CLI_EMBEDDED in this.run
			|| GH_CLI_PIPE_CONDITIONAL in this.run
