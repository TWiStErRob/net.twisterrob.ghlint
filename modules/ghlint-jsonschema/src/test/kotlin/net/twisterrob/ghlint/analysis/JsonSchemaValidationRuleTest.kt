package net.twisterrob.ghlint.analysis

import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.noFindings
import net.twisterrob.ghlint.testing.singleFinding
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class JsonSchemaValidationRuleTest {

	@TestFactory fun metadata() = test(JsonSchemaValidationRule::class)

	@Test fun `syntax error`() {
		val findings = check<JsonSchemaValidationRule>("<invalid json />")
		findings shouldHave noFindings()
	}

	@Test fun `wrong yaml contents`() {
		val findings = check<JsonSchemaValidationRule>("foo: bar")
		findings shouldHave singleFinding(
			"JsonSchemaValidation",
			"Object does not have some of the required properties [[jobs, on]] ()"
		)
	}
}
