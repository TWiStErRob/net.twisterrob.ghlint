package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.results.Finding

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
