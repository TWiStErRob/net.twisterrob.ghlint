package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.TestFactory

class JsonSchemaValidationRuleTest {

	@TestFactory fun metadata() = test(JsonSchemaValidationRule::class)
}
