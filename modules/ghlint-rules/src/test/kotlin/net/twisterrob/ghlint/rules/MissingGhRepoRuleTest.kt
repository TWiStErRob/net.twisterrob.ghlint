package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class MissingGhRepoRuleTest {

	@TestFactory fun metadata() = test(MissingGhRepoRule::class)

	@Test fun `passes when checkout creates context for gh`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when repository is explicitly declared`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: gh pr list
				        env:
				          GH_REPO: ${'$'}{{ github.repository }}
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when repository is explicitly declared globally on the workflow`() {
		val file = yaml(
			"""
				on: push
				env:
				  GH_REPO: ${'$'}{{ github.repository }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when repository is explicitly declared globally on the job`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      GH_REPO: ${'$'}{{ github.repository }}
				    steps:
				      - run: gh pr list
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `passes when checkout and repository are both declared`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - run: gh pr list
				        env:
				          GH_REPO: ${'$'}{{ github.repository }}
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave noFindings()
	}

	@Test fun `ignores composite actions`() {
		val results = check<MissingGhRepoRule>(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: gh pr list
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `ignores unrelated steps`() {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: |
				          # gh pr list
				          echo "Test"
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave noFindings()
	}

	@MethodSource("net.twisterrob.ghlint.rules.MissingGhTokenRuleTest#getValidGhCommands")
	@ParameterizedTest
	fun `reports when gh is used but there's no repository context`(script: String) {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave singleFinding(
			"MissingGhRepo",
			"Step[#0] in Job[test] should see `GH_REPO` environment variable or have a repository cloned.",
		)
	}

	@MethodSource("net.twisterrob.ghlint.rules.MissingGhTokenRuleTest#getValidGhCommands")
	@ParameterizedTest
	fun `reports when gh is used but there's no repository context - steps before and after`(script: String) {
		val file = yaml(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/download-artifact@v4
				      - run: |${'\n'}${script.prependIndent("\t\t\t\t          ")}
				      - uses: actions/upload-artifact@v4
			""".trimIndent(),
		)

		val results = check<MissingGhRepoRule>(file)

		results shouldHave singleFinding(
			"MissingGhRepo",
			"Step[#1] in Job[test] should see `GH_REPO` environment variable or have a repository cloned.",
		)
	}
}
