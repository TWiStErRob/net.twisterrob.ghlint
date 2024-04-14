package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.ruleset.RuleSet

public class Analyzer {

	public fun analyze(files: List<File>, ruleSets: List<RuleSet>): List<Finding> {
		val findings = files.flatMap { file ->
			ruleSets
				.asSequence()
				.flatMap { it.createRules() }
				.map(::SafeRule)
				.flatMap { rule -> rule.check(file) }
		}
		return findings
	}
}
