package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.testing.jupiter.DisableFailingDynamicTest
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.TestFactory

class DefaultRuleSetTest {

	@DisableFailingDynamicTest(
		displayName = "Issue WorkflowIdNaming non-compliant example #1 has findings",
		reason = "Workflow ID (i.e. yml file name) is not part of the file content, so it cannot be validated in an example.",
		acceptableFailure = """(?s).*Could not find WorkflowIdNaming among findings:\nNo findings.$""",
	)
	@TestFactory fun test() = test(DefaultRuleSet::class)
}
