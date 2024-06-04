package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location

/**
 * Represents a piece of a [Component] in GitHub.
 *
 * Essentially abstractions for each key [Workflow] and [Action].
 */
public interface Element {
	public val location: Location
}
