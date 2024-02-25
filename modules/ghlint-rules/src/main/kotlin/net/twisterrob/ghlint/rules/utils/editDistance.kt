package net.twisterrob.ghlint.rules.utils

internal fun editDistance(string1: String, string2: String): Int =
	editDistance(string1 = string1, length1 = string1.length, string2 = string2, length2 = string2.length)

@Suppress("detekt.ReturnCount", "detekt.NamedArguments")
private fun editDistance(string1: String, length1: Int, string2: String, length2: Int): Int {
	if (length1 == 0) {
		// If the first string is empty, need to insert all characters.
		return length2
	}
	if (length2 == 0) {
		// If the second string is empty, need to remove all characters.
		return length1
	}

	if (string1[length1 - 1] == string2[length2 - 1]) {
		// If the characters are the same, that counts as no operation, just ignore them and count other operations.
		return editDistance(string1, length1 - 1, string2, length2 - 1)
	}

	// If characters as different, need to try each operation.

	val insertion = editDistance(string1, length1, string2, length2 - 1)
	val deletion = editDistance(string1, length1 - 1, string2, length2)
	val replace = editDistance(string1, length1 - 1, string2, length2 - 1)
	val transposition =
		@Suppress("detekt.ComplexCondition")
		if (
			length1 >= 2 && length2 >= 2
			&& string1[length1 - 1] == string2[length2 - 2]
			&& string1[length1 - 2] == string2[length2 - 1]
		) {
			editDistance(string1, length1 - 2, string2, length2 - 2)
		} else {
			Int.MAX_VALUE - 1 // `- 1` to avoid overflow on `1 +` below.
		}

	// Take the best of the operations calculated recursively.
	// Add one for the operation itself.
	return 1 + minOf(insertion, deletion, replace, transposition)
}
