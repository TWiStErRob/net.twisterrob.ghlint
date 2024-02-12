package net.twisterrob.ghlint.model

import java.nio.file.Path
import kotlin.io.path.name

@JvmInline
public value class FileLocation(
	public val path: String,
) {

	public companion object
}

public val FileLocation.name: String
	get() = Path.of(path).name
