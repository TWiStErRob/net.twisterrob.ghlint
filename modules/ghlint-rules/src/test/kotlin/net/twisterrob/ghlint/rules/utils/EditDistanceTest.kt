package net.twisterrob.ghlint.rules.utils

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EditDistanceTest {

	@ParameterizedTest
	@CsvSource(
		"kitten, sitting, 3",
		"nojze, noise, 2",
		"intention, execution, 5",
		"rosettacode, raisethysword, 8",
	)
	fun `editDistance complex`(str1: String, str2: String, expected: Int) {
		test(str1, str2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"'', '', 0",
		"a, a, 0",
		"ab, ab, 0",
		"abc, abc, 0",
	)
	fun `editDistance same`(str1: String, str2: String, expected: Int) {
		test(str1, str2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"'', d, 1",
		"a, ad, 1",
		"a, da, 1",
		"ab, adb, 1",
		"abc, abcd, 1",
		"abc, abdc, 1",
		"abc, adbc, 1",
		"abc, dabc, 1",
	)
	fun `editDistance insertion`(str1: String, str2: String, expected: Int) {
		test(str1, str2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"a, '', 1",
		"ab, a, 1",
		"ab, b, 1",
		"ab, '', 2",
		"abc, ab, 1",
		"abc, ac, 1",
		"abc, bc, 1",
		"abc, a, 2",
		"abc, b, 2",
		"abc, c, 2",
		"abc, '', 3",
	)
	fun `editDistance deletion`(str1: String, str2: String, expected: Int) {
		test(str1, str2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"ab, ac, 1",
		"ab, cb, 1",
		"ab, cd, 2",
		"abc, dbc, 1",
		"abc, adc, 1",
		"abc, abd, 1",
		"abc, ade, 2",
		"abc, def, 3",
	)
	fun `editDistance replace`(str1: String, str2: String, expected: Int) {
		test(str1, str2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"ab, ba, 1",
		"abc, bac, 1",
		"abc, acb, 1",
		"abc, cba, 2",
		"abcd, badc, 2",
		"abcde, ebcda, 2", // 2 replacements wins over transposition (which is not swap).
	)
	fun `editDistance transpose`(str1: String, str2: String, expected: Int) {
		test(str1, str2, expected)
	}

	private fun test(str1: String, str2: String, expected: Int) {
		val distance = editDistance(str1, str2)

		distance shouldBe expected

		withClue("symmetry") {
			editDistance(str2, str1) shouldBe expected
		}
	}
}
