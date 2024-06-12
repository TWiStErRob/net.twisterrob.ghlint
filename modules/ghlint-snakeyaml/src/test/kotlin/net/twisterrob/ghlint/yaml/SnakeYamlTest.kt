package net.twisterrob.ghlint.yaml

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SnakeYamlTest {

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

	private fun reserialize(yaml: String) {
		val result = SnakeYaml.loadRaw(yaml(yaml))
		val dumped = SnakeYaml.save(result)
		dumped shouldBe yaml
	}
}
