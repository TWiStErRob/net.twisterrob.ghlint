package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding

public interface Rule {

	public val issues: List<Issue>

	public fun check(file: File): List<Finding>
}
