package net.twisterrob.ghlint.model

public sealed interface Model {
	public companion object
}

public val Model.file: File
	get() = when (this) {
		is Workflow -> parent
		is Job -> parent.parent
		is Step -> parent.parent.parent
	}
