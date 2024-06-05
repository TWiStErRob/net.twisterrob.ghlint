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

class RequiredPermissionsRuleTest {

	@TestFactory fun metadata() = test(RequiredPermissionsRule::class)

	@Test fun `should report when missing a known required permission for checkout action`() {
		val results = check<RequiredPermissionsRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    permissions:
				      pull-requests: write
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingRequiredActionPermissions",
			"Step[actions/checkout@v4] in Job[test] requires `contents: read` permission for `actions/checkout` to work: " +
					"To read the repository contents during git clone/fetch."
		)
	}

	@Test fun `passes when a known required permission for checkout action is specified`() {
		val results = check<RequiredPermissionsRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    permissions:
				      contents: read
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when a known required permission for checkout action is specified at workflow level`() {
		val results = check<RequiredPermissionsRule>(
			"""
				on: push
				permissions:
				  contents: read
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when a known required permission for checkout action is specified at higher access level`() {
		val results = check<RequiredPermissionsRule>(
			"""
				on: push
				jobs:
				  test:
				    runs-on: ubuntu-latest
				    permissions:
				      contents: write
				    steps:
				      - uses: actions/checkout@v4
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@Test fun `passes when a known required permission for checkout action is satisfied via a token`() {
		val results = check<RequiredPermissionsRule>(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      packages: read
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
				        with:
				          token: ${'$'}{{ secrets.some_token }}
			""".trimIndent()
		)

		results shouldHave noFindings()
	}

	@MethodSource("gitHubTokens")
	@ParameterizedTest
	fun `reports when a known required permission for github token in checkout action is not specified`(
		githubToken: String,
	) {
		val results = check<RequiredPermissionsRule>(
			"""
				on: push
				jobs:
				  test:
				    permissions:
				      packages: read
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
				        with:
				          token: ${githubToken}
			""".trimIndent()
		)

		results shouldHave singleFinding(
			"MissingRequiredActionPermissions",
			"Step[actions/checkout@v4] in Job[test] requires `contents: read` permission for `actions/checkout` to work: " +
					"To read the repository contents during git clone/fetch."
		)
	}

	companion object {
		@JvmStatic
		fun gitHubTokens(): List<String> = listOf(
			"\${{ github.token }}",
			"\${{github.token}}",
			"\${{   github.token}}",
			"\${{github.token   }}",
			"\${{ secrets.GITHUB_TOKEN }}",
			"\${{secrets.GITHUB_TOKEN}}",
			"\${{   secrets.GITHUB_TOKEN}}",
			"\${{   secrets.GITHUB_TOKEN   }}",
		)
	}
}
