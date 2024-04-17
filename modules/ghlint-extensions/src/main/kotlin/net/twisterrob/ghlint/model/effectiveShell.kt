package net.twisterrob.ghlint.model

public val Job.NormalJob.effectiveShell: String?
	get() = defaultShell ?: parent.defaultShell

public val WorkflowStep.Run.effectiveShell: String?
	get() = shell ?: parent.effectiveShell
