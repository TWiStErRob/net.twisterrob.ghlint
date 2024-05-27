package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue

public fun List<Finding>.testString(): String =
	if (this.isEmpty()) {
		"No findings."
	} else {
		this.joinToString(separator = "\n", transform = Finding::testString)
	}

public fun Finding.testString(): String =
	"""
		|Finding(
		|	rule=${rule},
		|	issue=${issue.id},
		|	location=${location.testString()},
		|	message=${message}
		|)
	""".trimMargin()

public fun Issue.testString(): String =
	"""Issue(id=${this.id}, title=${this.title})"""
