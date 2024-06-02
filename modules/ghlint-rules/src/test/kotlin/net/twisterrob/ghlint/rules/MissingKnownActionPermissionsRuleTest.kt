package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MissingKnownActionPermissionsRuleTest {

	@TestFactory fun metadata() = test(MissingKnownActionPermissionsRule::class)

	@Test fun `should report when missing a known required permission for checkout action`() {
		val results = net.twisterrob.ghlint.testing.check<MissingKnownActionPermissionsRule>(
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
				"Step[actions/checkout@v4] in Job[test] requires READ permission for actions/checkout to work."
		)
	}

	@Test fun `passes when a known required permission for checkout action is specified`() {
		val results = net.twisterrob.ghlint.testing.check<MissingKnownActionPermissionsRule>(
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
		val results = net.twisterrob.ghlint.testing.check<MissingKnownActionPermissionsRule>(
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
		val results = net.twisterrob.ghlint.testing.check<MissingKnownActionPermissionsRule>(
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
}
