package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location

public sealed interface Model {

	public val location: Location

	public companion object
}

public val Model.file: File
	get() = when (this) {
		is Workflow -> parent
		is Job -> parent.parent
		is Step -> parent.parent.parent
	}
