package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.test
import net.twisterrob.ghlint.testing.testRulesPackage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class DefaultRuleSetTest {

	@AcceptFailingDynamicTest(
		displayName = "Issue WorkflowIdNaming non-compliant example #1 has findings",
		reason = "Workflow ID (i.e. yml file name) is not part of the file content, so it cannot be validated in an example.",
		acceptableFailure = """^Could not find exclusively `WorkflowIdNaming`s among findings:\nNo findings.$""",
	)
	@AcceptFailingDynamicTest(
		displayName = "Issue MissingJobPermissions compliant example #2 has no findings",
		reason = "Rule triggers another another finding, but it's acceptable for this issue.",
		acceptableFailure = "^\\Q"
				+ "Collection should have size 0 but has size 1. Values: ["
				+ "Finding(\n"
				+ "	rule=net.twisterrob.ghlint.rules.ExplicitJobPermissionsRule@\\E[0-9a-f]+\\Q,\n"
				+ "	issue=ExplicitJobPermissions,\n"
				+ "	location=compliant/example.yml/5:3-5:10,\n"
				+ "	message=Job[example] should have explicit permissions.\n"
				+ ")"
				+ "]"
				+ "\\E$"
	)
	@Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
	@TestFactory fun test() = test(DefaultRuleSet::class)

	@Test
	fun `includes all rules in the package`() {
		testRulesPackage(DefaultRuleSet::class)
	}
}
