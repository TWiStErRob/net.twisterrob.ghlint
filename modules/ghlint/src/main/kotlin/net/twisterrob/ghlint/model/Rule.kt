package net.twisterrob.ghlint.model

public interface Rule {

	public fun check(workflow: Workflow): List<Finding>
}
