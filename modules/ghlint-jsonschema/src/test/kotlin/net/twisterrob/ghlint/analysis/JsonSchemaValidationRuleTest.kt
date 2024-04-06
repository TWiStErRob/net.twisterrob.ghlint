package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.testing.testIssue
import org.junit.jupiter.api.TestFactory

class JsonSchemaValidationRuleTest {

	@TestFactory fun metadata() =
		testIssue(JsonSchemaValidationRule(), JsonSchemaValidationRule.JsonSchemaValidation)
}
