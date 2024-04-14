package net.twisterrob.ghlint.model

public interface File {

	public val origin: RawFile

	public val location: FileLocation
		get() = origin.location

	public val content: Content

	public companion object
}
