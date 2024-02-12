package net.twisterrob.ghlint.model

public val Workflow.defaultShell: String?
	get() = defaults?.run?.shell

public val Job.NormalJob.defaultShell: String?
	get() = defaults?.run?.shell
