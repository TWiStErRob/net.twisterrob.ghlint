package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.aFinding
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.exactFindings
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.workflow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class RequiredPermissionsRuleTest {

	@TestFactory fun metadata() = test(RequiredPermissionsRule::class)

	@Nested
	inner class Checkout {
		@Test fun `reports when missing contents permission`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: ubuntu-latest
					    permissions:
					      pull-requests: write
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave singleFinding(
				"MissingRequiredActionPermissions",
				"Step[actions/checkout@v4] in Job[test] requires `contents: read` permission for `actions/checkout` to work: " +
						"To read the repository contents during git clone/fetch.",
			)
		}

		@Test fun `passes when contents permission is specified`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: ubuntu-latest
					    permissions:
					      contents: read
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when contents permission is specified at workflow level`() {
			val file = workflow(
				"""
					on: push
					permissions:
					  contents: read
					jobs:
					  test:
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when contents permission is specified at higher access level`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    runs-on: ubuntu-latest
					    permissions:
					      contents: write
					    steps:
					      - uses: actions/checkout@v4
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when contents permission is satisfied via an external token`() {
			val file = workflow(
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
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@MethodSource("net.twisterrob.ghlint.rules.RequiredPermissionsRuleTest#gitHubTokens")
		@ParameterizedTest
		fun `reports when missing contents permission for github token`(
			githubToken: String,
		) {
			val file = workflow(
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
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave singleFinding(
				"MissingRequiredActionPermissions",
				"Step[actions/checkout@v4] in Job[test] requires `contents: read` permission for `actions/checkout` to work: " +
						"To read the repository contents during git clone/fetch.",
			)
		}
	}

	@Nested
	inner class Stale {
		@Test fun `passes when basic permissions are specified`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    permissions:
					      issues: write
					      pull-requests: write
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/stale@v4
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `passes when permissions are specified for delete-branch`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    permissions:
					      issues: write
					      pull-requests: write
					      contents: write
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/stale@v4
					        with:
					          delete-branch: true
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave noFindings()
		}

		@Test fun `reports when missing issues and pr permissions`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    permissions:
					      packages: read
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/stale@v4
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave exactFindings(
				aFinding(
					"MissingRequiredActionPermissions",
					"Step[actions/stale@v4] in Job[test] requires `issues: write` permission for `actions/stale` to work: " +
							"To comment or close stale issues.",
				),
				aFinding(
					"MissingRequiredActionPermissions",
					"Step[actions/stale@v4] in Job[test] requires `pull-requests: write` permission for `actions/stale` to work: " +
							"To comment or close stale PRs.",
				),
			)
		}

		@MethodSource("net.twisterrob.ghlint.rules.RequiredPermissionsRuleTest#gitHubTokens")
		@ParameterizedTest
		fun `reports when missing issues and pr permissions for github token`(
			githubToken: String,
		) {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    permissions:
					      packages: read
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/stale@v4
					        with:
					          token: ${githubToken}
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave exactFindings(
				aFinding(
					"MissingRequiredActionPermissions",
					"Step[actions/stale@v4] in Job[test] requires `issues: write` permission for `actions/stale` to work: " +
							"To comment or close stale issues.",
				),
				aFinding(
					"MissingRequiredActionPermissions",
					"Step[actions/stale@v4] in Job[test] requires `pull-requests: write` permission for `actions/stale` to work: " +
							"To comment or close stale PRs.",
				),
			)
		}

		@Test fun `reports when missing basic and delete-branch permissions`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    permissions:
					      packages: read
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/stale@v4
					        with:
					          delete-branch: true
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave exactFindings(
				aFinding(
					"MissingRequiredActionPermissions",
					"Step[actions/stale@v4] in Job[test] requires `issues: write` permission for `actions/stale` to work: " +
							"To comment or close stale issues.",
				),
				aFinding(
					"MissingRequiredActionPermissions",
					"Step[actions/stale@v4] in Job[test] requires `pull-requests: write` permission for `actions/stale` to work: " +
							"To comment or close stale PRs.",
				),
				aFinding(
					"MissingRequiredActionPermissions",
					"Step[actions/stale@v4] in Job[test] requires `contents: write` permission for `actions/stale` to work: " +
							"To delete HEAD branches when closing PRs.",
				),
			)
		}

		@Test fun `reports when missing delete-branch permission`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    permissions:
					      issues: write
					      pull-requests: write
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/stale@v4
					        with:
					          delete-branch: true
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave singleFinding(
				"MissingRequiredActionPermissions",
				"Step[actions/stale@v4] in Job[test] requires `contents: write` permission for `actions/stale` to work: " +
						"To delete HEAD branches when closing PRs.",
			)
		}

		@Test fun `reports when missing delete-branch contents permission required level`() {
			val file = workflow(
				"""
					on: push
					jobs:
					  test:
					    permissions:
					      contents: read
					      issues: write
					      pull-requests: write
					    runs-on: ubuntu-latest
					    steps:
					      - uses: actions/stale@v4
					        with:
					          delete-branch: true
				""".trimIndent(),
			)

			val results = check<RequiredPermissionsRule>(file)

			results shouldHave singleFinding(
				"MissingRequiredActionPermissions",
				"Step[actions/stale@v4] in Job[test] requires `contents: write` permission for `actions/stale` to work: " +
						"To delete HEAD branches when closing PRs.",
			)
		}
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
