package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.Validator
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YamlTest {

	@Nested
	inner class Mapping {

		@Test fun `can re-serialize plain string`() {
			val yaml = """
				key: value
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize single quoted string`() {
			val yaml = """
				key: 'value'
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize double quoted string`() {
			val yaml = """
				key: "value"
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize literal string`() {
			val yaml = """
				key: |
				  value
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize folded string`() {
			val yaml = """
				key: >
				  value
				
			""".trimIndent()
			reserialize(yaml)
		}
	}

	@Nested
	inner class Array {

		@Test fun `can re-serialize plain string`() {
			val yaml = """
				- value1
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize double quoted string`() {
			val yaml = """
				- "value1"
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize single quoted string`() {
			val yaml = """
				- 'value1'
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize literal string`() {
			val yaml = """
				- |
				  value1
				
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun `can re-serialize folded string`() {
			val yaml = """
				- >
				  value1
				
			""".trimIndent()
			reserialize(yaml)
		}
	}

	@Nested
	inner class Complex {

		@Test fun empty() {
			val yaml = """
			""".trimIndent()
			reserialize(yaml)
		}

		@Test fun test() {
			val yaml = """
				- foo: bar
				- baz: "qux"
				  slam: |
				    doo
				
			""".trimIndent()
			reserialize(yaml)
		}
	}

	@Nested
	inner class Validation {

		private fun validate(yaml: String, expectedValid: Boolean): Validator.Result {
			val result = Yaml.validate(yaml)
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

	private fun reserialize(yaml: String) {
		val result = Yaml.load(yaml)
		val dumped = Yaml.save(result)
		dumped shouldBe yaml
	}
}
