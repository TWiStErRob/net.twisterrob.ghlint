package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MissingGhTokenRuleTest {

	@TestFactory fun metadata() = test(MissingGhTokenRule::class)

	@Test fun `passes when token is defined on step`() {
		val result = check<MissingGhTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: gh pr view
				        env:
				          GH_TOKEN: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		result shouldHave noFindings()
	}

	@Test fun `passes when token is defined on job`() {
		val result = check<MissingGhTokenRule>(
			"""
				jobs:
				  test:
				    env:
				      GH_TOKEN: ${'$'}{{ github.token }}
				    steps:
				      - run: gh pr view
			""".trimIndent()
		)

		result shouldHave noFindings()
	}

	@Test fun `passes when token is defined on workflow`() {
		val result = check<MissingGhTokenRule>(
			"""
				env:
				  GH_TOKEN: ${'$'}{{ github.token }}
				jobs:
				  test:
				    steps:
				      - run: gh pr view
			""".trimIndent()
		)

		result shouldHave noFindings()
	}

	@Test fun `passes when token is defined on step as secret`() {
		val result = check<MissingGhTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: gh pr view
				        env:
				          GH_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		result shouldHave noFindings()
	}

	@ParameterizedTest
	@ValueSource(
		strings = [
			"""
				gh pr view
			""",
			"""
				result = $(gh pr view)
			""",
			"""
				echo "foo" | gh pr view
			""",
			"""
				git commit && gh pr create
			""",
			"""
				git status || gh pr create
			""",
			"""
				result = $( gh pr view )
			""",
			"""
				result = $(
				    gh pr view
				)
			""",
			"""
				git commit \
				&& gh pr create
			""",
			"""
				git commit && \
				gh pr create
			""",
			"""
				git commit && \
				    gh pr create
			""",
		]
	)
	@Suppress("detekt.TrimMultilineRawString") // Trimmed inside test, trimming here would make these non-constant.
	fun `reports when gh is used different contexts`(script: String) {
		val result = check<MissingGhTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: |${'\n'}${script.trimIndent().prependIndent("\t\t\t\t          ")}
			""".trimIndent()
		)

		result shouldHave singleFinding(
			"MissingGhToken",
			"Step[#0] in Job[test] should see `GH_TOKEN` environment variable."
		)
	}

	@ParameterizedTest
	@ValueSource(
		strings = [
			"""
				# gh pr view
			""",
		]
	)
	@Suppress("detekt.TrimMultilineRawString") // Trimmed inside test, trimming here would make these non-constant.
	fun `passes when gh command is not in the right context`(script: String) {
		val result = check<MissingGhTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: |${'\n'}${script.trimIndent().prependIndent("\t\t\t\t          ")}
			""".trimIndent()
		)

		result shouldHave noFindings()
	}
}
