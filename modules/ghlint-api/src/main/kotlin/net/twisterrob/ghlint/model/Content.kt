package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import org.intellij.lang.annotations.Language

// STOPSHIP swap order?
public sealed interface Content : Yaml {

	public val location: Location
}

public interface InvalidContent : Content {

	@get:Language("yaml")
	public val raw: String

	public val error: Throwable
}
