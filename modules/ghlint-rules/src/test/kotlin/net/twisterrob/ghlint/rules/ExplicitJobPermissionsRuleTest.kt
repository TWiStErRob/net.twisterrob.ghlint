package net.twisterrob.ghlint.rules

import io.kotest.matchers.should
import net.twisterrob.ghlint.testing.beEmpty
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.haveFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ExplicitJobPermissionsRuleTest {

	@TestFactory fun metadata() = test(ExplicitJobPermissionsRule::class)

	@Nested
	inner class MissingJobPermissionsTest {

		@Test fun `reports when there are no permissions declared`() {
			val result = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should haveFinding(
				"MissingJobPermissions",
				"Job[test] is missing permissions."
			)
		}

		@Test fun `passes explicit permissions on the job`() {
			val result = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should beEmpty()
		}

		@Test fun `reports the job that has no permissions declared`() {
			val result = check<ExplicitJobPermissionsRule>(
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

			result should haveFinding(
				"MissingJobPermissions",
				"Job[test] is missing permissions."
			)
		}

		@Test fun `passes explicit no permissions on the job`() {
			val result = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    permissions: {}
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should beEmpty()
		}
	}

	@Nested
	inner class ExplicitJobPermissionsTest {

		@Test fun `reports when permissions are on the workflow level`() {
			val result = check<ExplicitJobPermissionsRule>(
				"""
					permissions:
					  contents: read
					jobs:
					  test:
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should haveFinding(
				"ExplicitJobPermissions",
				"Job[test] should have explicit permissions."
			)
		}

		@Test fun `passes when permissions are on the job level`() {
			val result = check<ExplicitJobPermissionsRule>(
				"""
					jobs:
					  test:
					    permissions:
					      contents: read
					    steps:
					      - run: echo "Test"
				""".trimIndent()
			)

			result should beEmpty()
		}
	}
}
