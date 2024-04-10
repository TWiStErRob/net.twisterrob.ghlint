package net.twisterrob.ghlint.analysis

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.check
import net.twisterrob.ghlint.testing.test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class JsonSchemaValidationRuleTest {

	@TestFactory fun metadata() = test(JsonSchemaValidationRule::class)

	@Test fun `syntax error`() {
		val findings = check<JsonSchemaValidationRule>("<invalid json />")
		findings shouldHaveSize 1
		findings.single().message shouldBe "File could not be parsed: java.lang.IllegalArgumentException: " +
				"Root node is not a mapping: ScalarNode.\n<invalid json />"
	}

	@Test fun `wrong yaml contents`() {
		val findings = check<JsonSchemaValidationRule>("foo: bar")
		findings shouldHaveSize 1
		findings.single().message shouldBe "File could not be parsed: java.lang.IllegalStateException: " +
				"Missing required key: jobs in [foo]\nfoo: bar"
	}
}
