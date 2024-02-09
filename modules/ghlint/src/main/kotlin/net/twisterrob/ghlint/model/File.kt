package net.twisterrob.ghlint.model

public class File internal constructor(
	public val file: FileName,
) {

	internal fun readText(): String =
		java.io.File(file.path).readText()

	public companion object
}
