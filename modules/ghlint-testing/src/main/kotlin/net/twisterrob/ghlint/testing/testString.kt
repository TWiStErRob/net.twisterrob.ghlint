package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.results.Finding

public fun List<Finding>.testString(): String =
	joinToString(separator = "\n", transform = Finding::testString)

public fun Finding.testString(): String {
	val coordinates = with(location) {
		"${start.line.number}:${start.column.number}-${end.line.number}:${end.column.number}"
	}
	return """
		|Finding(
		|	rule=${rule},
		|	issue=${issue.id},
		|	location=${location.file.path}/${coordinates},
		|	message=${message}
		|)
	""".trimMargin()
}
