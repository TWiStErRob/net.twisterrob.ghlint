package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.TestFactory

class DefaultRuleSetTest {

	@AcceptFailingDynamicTest(
		displayName = "Issue WorkflowIdNaming non-compliant example #1 has findings",
		reason = "Workflow ID (i.e. yml file name) is not part of the file content, so it cannot be validated in an example.",
		acceptableFailure = """(?s)^Could not find WorkflowIdNaming among findings:\nNo findings.$""",
	)
	@AcceptFailingDynamicTest(
		"Issue MissingJobPermissions compliant example #2 has no findings",
		"Rule triggers another another finding, but it's acceptable for this issue.",
		"^\\QFindings should be empty but contained:\n" +
				"Finding(\n" +
				"	rule=net.twisterrob.ghlint.rules.ExplicitJobPermissionsRule@\\E[0-9a-f]+\\Q,\n" +
				"	issue=ExplicitJobPermissions,\n" +
				"	location=test.yml/5:4-7:27,\n" +
				"	message=Job[example] should have explicit permissions.\n" +
				")\\E$"
	)
	@TestFactory fun test() = test(DefaultRuleSet::class)
}
