package net.twisterrob.ghlint.test

import io.kotest.matchers.shouldBe

public infix fun String?.shouldEqualIgnoringEOL(expected: String) {
	this?.normalizeLineEndings() shouldBe expected.normalizeLineEndings()
}
