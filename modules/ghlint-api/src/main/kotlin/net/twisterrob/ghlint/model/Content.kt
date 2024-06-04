package net.twisterrob.ghlint.model

public sealed interface Content {

	public val parent: File

	public companion object
}

public interface InvalidContent : Content {

	public val error: Throwable

	public companion object
}
