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

	@Test fun `passes when env is dynamic`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env: ${'$'}{{ format('{0}', env.GITHUB_TOKEN) }}
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is used in workflow env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				env:
				  MY_ENV: ${'$'}{{ github.token }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in workflow env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				env:
				  MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in workflow env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				env:
				  MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo "Test"
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
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_ENV: ${'$'}{{ github.token }}
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
				    steps:
				      - run: echo "Test"
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
				    steps:
				      - run: echo "Test"
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
				on: push
				jobs:
				  test:
				    uses: test/workflow.yml
				    with:
				      input: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    uses: test/workflow.yml
				    with:
				      input: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    uses: test/workflow.yml
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
				on: push
				jobs:
				  test:
				    uses: test/workflow.yml
				    secrets:
				      input: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes job secret inherit`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    uses: test/workflow.yml
				    secrets: inherit
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job secret`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    uses: test/workflow.yml
				    secrets:
				      input: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job secret`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    uses: test/workflow.yml
				    secrets:
				      test-input: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			"`test-input` secret in Job[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}

	@Test fun `passes when token is used in job step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: some/action@v1
				        env:
				          MY_ENV: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is used in action step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: some/action@v1
				      env:
				        MY_ENV: ${'$'}{{ github.token }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: some/action@v1
				        env:
				          MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in action step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: some/action@v1
				      env:
				        MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: some/action@v1
				        env:
				          MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			@Suppress("detekt.MaxLineLength")
			"`MY_ENV` environment variable in Step[some/action@v1] in Job[test] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."
		)
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in action step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: some/action@v1
				      env:
				        MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			@Suppress("detekt.MaxLineLength")
			"""`MY_ENV` environment variable in Step[some/action@v1] in Action["Test"] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."""
		)
	}

	@Test fun `passes when token is used in job run step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: "Test"
				        env:
				          MY_ENV: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is used in action run step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: "Test"
				      shell: bash
				      env:
				        MY_ENV: ${'$'}{{ github.token }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job run step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: "Test"
				        env:
				          MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in action run step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: "Test"
				      shell: bash
				      env:
				        MY_ENV: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job run step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
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

	@Test fun `reports when GITHUB_TOKEN secret is used in action run step env`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - run: "Test"
				      shell: bash
				      env:
				        MY_ENV: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			@Suppress("detekt.MaxLineLength")
			"""`MY_ENV` environment variable in Step[#0] in Action["Test"] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."""
		)
	}

	@Test fun `passes when token is used in job step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: test/action@v0
				        with:
				          test-input: ${'$'}{{ github.token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when token is used in action step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: test/action@v0
				      with:
				        test-input: ${'$'}{{ github.token }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in job step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: test/action@v0
				        with:
				          test-input: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when GITHUB_TOKEN variable is used in action step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: test/action@v0
				      with:
				        test-input: ${'$'}{{ env.GITHUB_TOKEN }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave noFindings()
	}

	@Test fun `reports when GITHUB_TOKEN secret is used in job step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
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

	@Test fun `reports when GITHUB_TOKEN secret is used in action step input`() {
		val results = check<PreferGitHubTokenRule>(
			"""
				name: "Test"
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: test/action@v0
				      with:
				        test-input: ${'$'}{{ secrets.GITHUB_TOKEN }}
			""".trimIndent(),
			fileName = "action.yml",
		)

		results shouldHave singleFinding(
			"PreferGitHubToken",
			@Suppress("detekt.MaxLineLength")
			"""`test-input` input in Step[test/action@v0] in Action["Test"] should use `github.token` in `${'$'}{{ secrets.GITHUB_TOKEN }}`."""
		)
	}
}
