package net.twisterrob.ghlint.model

import org.intellij.lang.annotations.Language

public interface Yaml {

	// STOPSHIP is this used?
	@get:Language("yaml")
	public val raw: String

	public val parent: File

	public companion object
}
