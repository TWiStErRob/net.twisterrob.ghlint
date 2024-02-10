package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Validator
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class YamlValidationTest {

	private fun validate(yaml: String, expectedValid: Boolean): Validator.Result {
		val result = YamlValidation.validate(yaml)
		if (expectedValid) {
			@Suppress("detekt.ForbiddenMethodCall") // Required to diagnose.
			result.errors.forEach { println(it.toDisplayString()) }
			result.errors shouldHaveSize 0
		}
		result.isValid shouldBe expectedValid
		return result
	}

	@Test
	fun `empty file fails validation`() {
		val yaml = """
		""".trimIndent()
		validate(yaml, false)
	}

	@Test
	fun `minimal workflow passes validation`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
			  build:
			    runs-on: ubuntu-latest
			    steps:
			      - uses: actions/checkout@v4
		""".trimIndent()
		validate(yaml, true)
	}

	@Test
	fun `empty jobs fails validation`() {
		val yaml = """
			on:
			  workflow_dispatch:
			jobs:
		""".trimIndent()
		validate(yaml, false)
	}

	@Test
	fun `missing jobs fails validation`() {
		val yaml = """
			on:
			  workflow_dispatch:
		""".trimIndent()
		validate(yaml, false)
	}
}
