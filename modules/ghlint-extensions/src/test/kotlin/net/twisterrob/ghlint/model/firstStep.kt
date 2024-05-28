package net.twisterrob.ghlint.model

internal val File.firstStep: Step
	get() =
		when (val content = content) {
			is Workflow -> (content.jobs["test"]!! as Job.NormalJob).steps[0]
			is Action -> (content.runs as Action.Runs.CompositeRuns).steps[0]
			is InvalidContent -> error("Invalid content: ${content.error}")
		}
