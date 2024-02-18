package net.twisterrob.ghlint.testing.jupiter

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class DisableFailingDynamicTestTest {

	@DisableFailingDynamicTest(
		displayName = "Invalid location of annotation.",
		reason = "Testing disable logic.",
		acceptableFailure = "",
	)
	@Test fun `annotation on normal Test should have no effect`() {
		pass()
	}

	@DisableFailingDynamicTest(
		displayName = "Failing test",
		reason = "Testing disable logic.",
		acceptableFailure = "Fake failure",
	)
	@TestFactory fun `dynamic test can be disabled`(): List<DynamicNode> =
		listOf(
			dynamicTest("Passing test") { pass() },
			dynamicTest("Failing test") { fail("Fake failure") },
		)

	@DisableFailingDynamicTest(
		displayName = "Missing name",
		reason = "Testing disable logic.",
		acceptableFailure = "Fake failure",
	)
	@TestFactory fun `mismatched name does not cause a problem`(): List<DynamicNode> =
		listOf(
			dynamicTest("Passing test 1") { pass() },
			dynamicTest("Passing test 2") { pass() },
		)

	@Disabled("Don't know how to assert result on this.")
	@DisableFailingDynamicTest(
		displayName = "Failing test",
		reason = "Testing disable logic.",
		acceptableFailure = "Fake failure",
	)
	@TestFactory fun `changed message fails the test`(): List<DynamicNode> =
		listOf(
			dynamicTest("Passing test") { pass() },
			dynamicTest("Failing test") { fail("Changed failure") },
		)

	companion object {

		private fun pass() {
			// No-op to make the test pass.
		}
	}
}
