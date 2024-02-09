package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.Model

public interface Reporting {

	public fun report(issue: Issue, context: Model)
}
