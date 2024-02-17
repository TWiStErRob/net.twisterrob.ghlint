package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.testing.validate
import org.junit.jupiter.api.Test

class JsonSchemaValidationRuleTest {

	@Test fun metadata() {
		validate(JsonSchemaValidationRule(), JsonSchemaValidationRule.JsonSchemaValidation)
	}
}
