package net.twisterrob.ghlint.rules.permissions

import net.twisterrob.ghlint.model.Scope

internal data class RequiredScope(
	val scope: Scope,
	val reason: String,
)
