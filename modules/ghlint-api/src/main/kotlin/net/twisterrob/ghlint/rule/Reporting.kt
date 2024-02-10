package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.results.Finding

public interface Reporting {

	public fun report(finding: Finding)
}
