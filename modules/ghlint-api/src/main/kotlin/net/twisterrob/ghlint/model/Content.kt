package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location

// STOPSHIP swap order?
public sealed interface Content : Yaml {

	public val location: Location
}

public interface InvalidContent : Content {

	public val error: Throwable
}
