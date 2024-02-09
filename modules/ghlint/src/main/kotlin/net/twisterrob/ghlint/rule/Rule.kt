package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding

public interface Rule {

	public fun check(workflow: Workflow): List<Finding>
}
