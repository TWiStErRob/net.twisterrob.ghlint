package net.twisterrob.ghlint.model

public val Step.stepsBefore: List<Step>
	get() = this.parent.steps.subList(0, index.value)

public val Step.stepsAfter: List<Step>
	get() = this.parent.steps.let { it.subList(index.value + 1, it.size) }
