package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.Action

public interface ActionResolver {

	public fun resolveAction(owner: String, repo: String, path: String?, ref: String): Action
}
