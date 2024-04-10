package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class PreferGitHubTokenRuleTest {

	@TestFactory fun metadata() = test(PreferGitHubTokenRule::class)

	@Test fun `passes when token is used in workflow env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				env:
				  MY_ENV: ${'$'}{{ github.token }}
				jobs: {}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in workflow env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				env:
				  MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
				jobs: {}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in workflow env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				env:
				  MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
				jobs: {}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			"`MY_ENV` environment variable in Workflow[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}

	@Test fun `passes when token is used in job env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    env:
				      MY_ENV: ${'$'}{{ github.token }}
				    steps: []
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    env:
				      MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
				    steps: []
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    env:
				      MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
				    steps: []
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			"`MY_ENV` environment variable in Job[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}

	@Test fun `passes when token is used in job input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    uses: test
				    with:
				      input: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    uses: test
				    with:
				      input: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    uses: test
				    with:
				      test-input: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			"`test-input` input in Job[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}

	@Test fun `passes when token is used in job secret`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    uses: test
				    secrets:
				      input: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes job secret inherit`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    uses: test
				    secrets: inherit
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job secret`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    uses: test
				    secrets:
				      input: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job secret`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    uses: test
				    secrets:
				      test-input: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			"`test-input` secret in Job[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}

	@Test fun `passes when token is used in step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: "Test"
				        env:
				          MY_ENV: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: "Test"
				        env:
				          MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - run: "Test"
				        env:
				          MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			@Suppress("detekt.MaxLineLength")
			"`MY_ENV` environment variable in Step[#0] in Job[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}

	@Test fun `passes when token is used in step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - uses: test/action@v0
				        with:
				          test-input: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - uses: test/action@v0
				        with:
				          test-input: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				jobs:
				  test:
				    steps:
				      - uses: test/action@v0
				        with:
				          test-input: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			@Suppress("detekt.MaxLineLength")
			"`test-input` input in Step[test/action@v0] in Job[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}
}
