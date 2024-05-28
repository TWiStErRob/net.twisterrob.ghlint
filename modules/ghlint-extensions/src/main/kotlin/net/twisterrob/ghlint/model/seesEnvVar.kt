package net.twisterrob.ghlint.model

public fun Step.Run.seesEnvVar(name: String): Boolean =
	when (this) {
		is WorkflowStep.Run ->
			this.env.map.containsKey(name)
					|| this.parent.env.map.containsKey(name)
					|| this.parent.parent.env.map.containsKey(name)

		is ActionStep.Run ->
			this.env.map.containsKey(name)
	}
