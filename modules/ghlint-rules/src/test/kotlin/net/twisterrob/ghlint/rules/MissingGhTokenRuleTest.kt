package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class MissingGhTokenRuleTest {

	@TestFactory fun metadata() = test(MissingGhTokenRule::class)

	@Test fun `passes when token is defined on step`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
				        env:
				          GH_TOKEN: ${'$'}{{ github.token }}
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token and host are defined on step`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
				        env:
				          GH_HOST: github.example.com
				          GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on action step`() {
		val results = check<MissingGhTokenRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: actions/checkout@v4
				    - run: gh pr list
				      shell: bash
				      env:
				        GH_TOKEN: ${'$'}{{ github.token }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token and host are defined on action step`() {
		val results = check<MissingGhTokenRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: actions/checkout@v4
				    - run: gh pr list
				      shell: bash
				      env:
				        GH_HOST: github.example.com
				        GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on job`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      GH_TOKEN: ${'$'}{{ github.token }}
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token and host are defined on job`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      GH_HOST: github.example.com
				      GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on workflow`() {
		val file = workflow(
			"""
				on: push
				env:
				  GH_TOKEN: ${'$'}{{ github.token }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token and host are defined on workflow`() {
		val file = workflow(
			"""
				on: push
				env:
				  GH_HOST: github.example.com
				  GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token and host are defined at different levels`() {
		val file = workflow(
			"""
				on: push
				env:
				  GH_HOST: github.example.com
				jobs:
				  test:
				    env:
				      GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on step as secret`() {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
				        env:
				          GH_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is defined on action step as secret`() {
		val results = check<MissingGhTokenRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: actions/checkout@v4
				    - run: gh pr list
				      shell: bash
				      env:
				        GH_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@MethodSource("getValidGhCommands")
	@ParameterizedTest
	fun `reports when gh is used in different shell contexts`(script: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave singleFinding(
			"MissingGhToken",
			"Step[#1] in Job[test] should see `GH_TOKEN` environment variable."
		)
	}

	@MethodSource("getValidGhCommands")
	@ParameterizedTest
	fun `reports missing host when gh is used in different shell contexts`(script: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
				        env:
				          GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave singleFinding(
			"MissingGhHost",
			"Step[#1] in Job[test] should see `GH_HOST` environment variable when using `GH_ENTERPRISE_TOKEN`."
		)
	}

	@MethodSource("getValidGhCommands")
	@ParameterizedTest
	fun `reports missing enterprise token when gh is used in different shell contexts`(script: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
				        env:
				          GH_HOST: github.example.com
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave singleFinding(
			"MissingGhToken",
			"Step[#1] in Job[test] should see `GH_ENTERPRISE_TOKEN` environment variable when using `GH_HOST`."
		)
	}

	@MethodSource("getValidGhCommands")
	@ParameterizedTest
	fun `reports when gh is used in different shell contexts in actions`(script: String) {
		val results = check<MissingGhTokenRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: actions/checkout@v4
				    - run: |${'\n'}${script.prependIndent("\t\t\t\t        ")}
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"MissingGhToken",
			"""Step[#1] in Action["Test"] should see `GH_TOKEN` environment variable."""
		)
	}

	@MethodSource("getInvalidGhCommands")
	@ParameterizedTest
	fun `passes when gh command is not in the right context`(script: String) {
		val file = workflow(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave noFindings()
	}

	@MethodSource("getInvalidGhCommands")
	@ParameterizedTest
	fun `passes when gh command is not in the right context in actions`(script: String) {
		val results = check<MissingGhTokenRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: actions/checkout@v4
				    - run: |${'\n'}${script.prependIndent("\t\t\t\t        ")}
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `reports dynamic env, even though it's inconclusive`() {
		val file = workflow(
			"""
				on: push
				env: ${'$'}{{ {} }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: gh pr view
			""".trimIndent(),
		)

		val results = check<MissingGhTokenRule>(file)

		results shouldHave singleFinding(
			"MissingGhToken",
			"Step[#0] in Job[test] should see `GH_TOKEN` environment variable."
		)
	}

	companion object {

		@JvmStatic
		val invalidGhCommands = listOf(
			"""
				# gh pr list
			""".trimIndent(),
		)

		@JvmStatic
		val validGhCommands = listOf(
			"""
				gh pr list
			""".trimIndent(),
			"""
				result = $(gh pr list)
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
				result = $( gh pr list )
			""".trimIndent(),
			"""
				result = $(
				    gh pr list
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
