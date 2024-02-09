package net.twisterrob.ghlint.reporting

import net.twisterrob.ghlint.model.Finding

public interface Reporter {

	public fun report(findings: List<Finding>)
}
