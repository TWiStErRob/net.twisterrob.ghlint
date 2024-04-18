package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class MissingGhTokenRuleTest {

	@TestFactory fun metadata() = test(MissingGhTokenRule::class)

	@Test fun `passes when token is defined on step`() {
		val results = check<MissingGhTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: gh pr view
				        env:
				          GH_TOKEN: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on job`() {
		val results = check<MissingGhTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      GH_TOKEN: ${'$'}{{ github.token }}
				    steps:
				      - run: gh pr view
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on workflow`() {
		val results = check<MissingGhTokenRule>(
			"""
				on: push
				env:
				  GH_TOKEN: ${'$'}{{ github.token }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: gh pr view
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on step as secret`() {
		val results = check<MissingGhTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: gh pr view
				        env:
				          GH_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@ParameterizedTest
	fun `reports when gh is used in different shell contexts`(script: String) {
		val results = check<MissingGhTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingGhToken",
			"Step[#0] in Job[test] should see `GH_TOKEN` environment variable."
		)
	}

	@MethodSource("getInvalidGhCommands")
	@ParameterizedTest
	fun `passes when gh command is not in the right context`(script: String) {
		val results = check<MissingGhTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	companion object {

		@JvmStatic
		val invalidGhCommands = listOf(
			"""
				# gh pr view
			""".trimIndent(),
		)

		@JvmStatic
		val validGhCommands = listOf(
			"""
				gh pr view
			""".trimIndent(),
			"""
				result = $(gh pr view)
			""".trimIndent(),
			"""
				echo "foo" | gh pr view
			""".trimIndent(),
			"""
				git commit && gh pr create
			""".trimIndent(),
			"""
				git status || gh pr create
			""".trimIndent(),
			"""
				result = $( gh pr view )
			""".trimIndent(),
			"""
				result = $(
				    gh pr view
				)
			""".trimIndent(),
			"""
				git commit \
				&& gh pr create
			""".trimIndent(),
			"""
				git commit && \
				gh pr create
			""".trimIndent(),
			"""
				git commit && \
				    gh pr create
			""".trimIndent(),
		)
	}
}
