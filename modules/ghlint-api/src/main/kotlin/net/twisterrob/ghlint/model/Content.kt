package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location

public sealed interface Content : Element {

	public val parent: File

	public companion object
}

public interface InvalidContent : Content {

	public val error: Throwable

	public override val location: Location
		get() = parent.wholeFile

	public companion object
}
