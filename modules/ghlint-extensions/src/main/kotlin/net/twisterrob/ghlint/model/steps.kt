package net.twisterrob.ghlint.model

public val Step.stepsBefore: List<Step>
	get() = parent.steps.subList(0, index.value)

public val Step.stepsAfter: List<Step>
	get() = parent.steps.subList(index.value + 1, parent.steps.size)
