package net.twisterrob.ghlint.results

import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule

public class Finding(
	public val rule: Rule,
	public val issue: Issue,
	public val location: Location,
) {

	public companion object
}
