package net.twisterrob.ghlint.rules.utils

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.concurrent.TimeUnit

@Timeout(1, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD) // separate == preemptive.
class EditDistanceTest {

	@ParameterizedTest
	@CsvSource(
		"kitten, sitting, 3",
		"nojze, noise, 2",
		"intention, execution, 5",
		"rosettacode, raisethysword, 8",
	)
	fun `editDistance complex`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"set-release-status, set-release-notes, 4",
		"get-version-number, set-release-status, 13",
		"get-version, check-for-approved-release, 21",
	)
	fun `editDistance realistic`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"'', '', 0",
		"a, a, 0",
		"ab, ab, 0",
		"abc, abc, 0",
	)
	fun `editDistance same`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
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
	fun `editDistance insertion`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
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
	fun `editDistance deletion`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
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
	fun `editDistance replace`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
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
	fun `editDistance transpose`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"a, b, 1",
		"ab, cd, 2",
		"abc, def, 3",
		"abcd, efgh, 4",
		"abcde, fghij, 5",
		"abcdef, ghijkl, 6",
		"abcdefg, hijklmn, 7",
		"abcdefgh, ijklmnop, 8",
		"abcdefghi, jklmnopqr, 9",
		"abcdefghij, klmnopqrst, 10",
		"abcdefghijk, lmnopqrstuv, 11",
		"abcdefghijkl, mnopqrstuvwx, 12",
		"abcdefghijklm, nopqrstuvwxyz, 13",
	)
	fun `editDistance far`(string1: String, string2: String, expected: Int) {
		test(string1, string2, expected)
	}

	@ParameterizedTest
	@CsvSource(
		"25, abcdefghijklmnopqrstuvwxy, z",
		"25, a, bcdefghijklmnopqrstuvwxyz",
		"23, abc, defghijklmnopqrstuvwxyz",
		"23, abcdefghijklmnopqrstuvw, xyz",
		"0"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
		"1"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab",
		"1"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ ", baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
		"2"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ ", baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab",
		"100"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ ", bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
		"2"
				+ ", abababababababababababababababababababababababababababababababababababababababababababababababababab"
				+ ", babababababababababababababababababababababababababababababababababababababababababababababababababa",
		"90"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ ", aaaaaaaaaa",
		"90"
				+ ", aaaaaaaaaa"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
		"100"
				+ ", a"
				+ ", bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
		"100"
				+ ", aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ ", b",
	)
	fun `editDistance edge cases`(expected: Int, string1: String, string2: String) {
		test(string1, string2, expected)
	}

	private fun test(string1: String, string2: String, expected: Int) {
		val distance = editDistance(string1, string2)

		distance shouldBe expected

		withClue("symmetry") {
			editDistance(string2, string1) shouldBe expected
		}
	}
}
