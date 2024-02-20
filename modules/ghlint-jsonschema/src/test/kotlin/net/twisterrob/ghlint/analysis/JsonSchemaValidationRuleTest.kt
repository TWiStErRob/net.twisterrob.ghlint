package net.twisterrob.ghlint.analysis

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.validate
import org.junit.jupiter.api.Test

class JsonSchemaValidationRuleTest {

	@Test fun metadata() {
		validate(JsonSchemaValidationRule(), JsonSchemaValidationRule.JsonSchemaValidation)
	}

	@Test fun `syntax error`() {
		val findings = check<JsonSchemaValidationRule>("<invalid json />")
		findings shouldHaveSize 1
		findings.single().message shouldBe "Invalid JSON"
	}
}
