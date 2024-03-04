package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import org.intellij.lang.annotations.Language

public sealed interface Content {

	public val parent: File

	public val location: Location
}

public interface InvalidContent : Content {

	@get:Language("yaml")
	public val raw: String

	public val error: Throwable
}
