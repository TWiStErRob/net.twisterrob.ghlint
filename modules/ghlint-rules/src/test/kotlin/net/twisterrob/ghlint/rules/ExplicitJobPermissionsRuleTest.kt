package net.twisterrob.ghlint.rules

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ExplicitJobPermissionsRuleTest {

	@AcceptFailingDynamicTest(
		"Issue MissingJobPermissions compliant example #2 has no findings",
		"Rule triggers another another finding, but it's acceptable for this issue.",
		"^\\QCollection should be empty but contained " +
				"Finding(\n" +
				"	rule=net.twisterrob.ghlint.rules.ExplicitJobPermissionsRule@\\E[0-9a-f]+\\Q,\n" +
				"	issue=ExplicitJobPermissions,\n" +
				"	location=test.yml/5:3-5:10,\n" +
				"	message=Job[example] should have explicit permissions.\n" +
				")\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun metadata() = test(ExplicitJobPermissionsRule::class)

	@Nested
	inner class MissingJobPermissionsTest {

		@Test fun `reports when there are no permissions declared`() {
			val results = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"MissingJobPermissions",
				"Job[test] is missing permissions."
			)
		}

		@Test fun `passes explicit permissions on the job`() {
			val results = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave noFindings()
		}

		@Test fun `reports the job that has no permissions declared`() {
			val results = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  has-perms-1:
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
					  test:
					    steps:
					      - run: echo "Test"
					  has-perms-2:
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"MissingJobPermissions",
				"Job[test] is missing permissions."
			)
		}

		@Test fun `passes explicit no permissions on the job`() {
			val results = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    permissions: {}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave noFindings()
		}
	}

	@Nested
	inner class ExplicitJobPermissionsTest {

		@Test fun `reports when permissions are on the workflow level`() {
			val results = check<ExplicitJobPermissionsRule>(
				"""
					permissions:
					  contents: read
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave singleFinding(
				"ExplicitJobPermissions",
				"Job[test] should have explicit permissions."
			)
		}

		@Test fun `passes when permissions are on the job level`() {
			val results = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			results shouldHave noFindings()
		}
	}
}
