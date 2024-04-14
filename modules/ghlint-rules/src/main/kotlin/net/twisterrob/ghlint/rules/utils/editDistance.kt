package net.twisterrob.ghlint.rules.utils

@Suppress("detekt.CognitiveComplexMethod", "detekt.NestedBlockDepth") // Yes.
internal fun editDistance(string1: String, string2: String): Int {
	val editDistance = Array(1 + string1.length) { IntArray(1 + string2.length) }
	for (length2 in 0..string2.length) {
		// If the first string is empty, need to insert all characters.
		editDistance[0][length2] = length2
	}
	for (length1 in 0..string1.length) {
		// If the second string is empty, need to remove all characters.
		editDistance[length1][0] = length1
	}
	for (length1 in 1..string1.length) {
		for (length2 in 1..string2.length) {
			editDistance[length1][length2] =
				if (string1[length1 - 1] == string2[length2 - 1]) {
					// Characters are the same, that counts as no operation, just ignore them.
					editDistance[length1 - 1][length2 - 1]
				} else {
					// Characters are different, need to try each operation.
					val insertion = editDistance[length1][length2 - 1]
					val deletion = editDistance[length1 - 1][length2]
					val replace = editDistance[length1 - 1][length2 - 1]
					val transposition =
						@Suppress("detekt.ComplexCondition")
						if (
							length1 >= 2 && length2 >= 2
							&& string1[length1 - 1] == string2[length2 - 2]
							&& string1[length1 - 2] == string2[length2 - 1]
						) {
							editDistance[length1 - 2][length2 - 2]
						} else {
							Int.MAX_VALUE - 1 // `- 1` to avoid overflow on `1 +` below.
						}
					// Take the best of the operations calculated "recursively".
					// Add one for the operation itself.
					1 + minOf(insertion, deletion, replace, transposition)
				}
		}
	}
	return editDistance[string1.length][string2.length]
}
