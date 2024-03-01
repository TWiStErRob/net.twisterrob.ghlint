package net.twisterrob.ghlint.model

public class File(
	public val location: FileLocation,
	public val content: Content,
) {

	public companion object
}

public class RawFile(
	public val location: FileLocation,
	public val content: String,
) {

	public companion object
}
