package net.twisterrob.ghlint.docs

import net.twisterrob.ghlint.rule.Rule

internal val Rule.id: String
	get() = this::class.simpleName ?: error("Invalid rule")
