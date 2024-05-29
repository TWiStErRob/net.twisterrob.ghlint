package net.twisterrob.ghlint.model

@Suppress("detekt.MaxChainedCallsOnSameLine") // Keep it simple.
public fun Step.seesEnvVar(name: String): Boolean =
	when (this) {
		is WorkflowStep ->
			this.env.map.containsKey(name)
					|| this.parent.env.map.containsKey(name)
					|| this.parent.parent.env.map.containsKey(name)

		is ActionStep ->
			this.env.map.containsKey(name)

		is Step.BaseStep -> error("Unknown step type: ${this}")
	}
