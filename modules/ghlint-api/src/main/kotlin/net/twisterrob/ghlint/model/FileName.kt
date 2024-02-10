package net.twisterrob.ghlint.model

import java.nio.file.Path
import kotlin.io.path.name

@JvmInline
public value class FileName(
	public val path: String,
) {

	public companion object
}

public val FileName.name: String
	get() = Path.of(path).name
