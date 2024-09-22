package net.twisterrob.ghlint.yaml

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.NodeTuple
import org.snakeyaml.engine.v2.nodes.ScalarNode

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

	@Nested
	inner class EdgeCases {
		@Test fun empty() {
			val node = SnakeYaml.loadRaw(yaml("", "test.yml"))
			assertInstanceOf<ScalarNode>(node)
			assertEquals("", node.value)
		}

		@Test fun newline() {
			val node = SnakeYaml.loadRaw(yaml("\n", "test.yml"))
			assertInstanceOf<MappingNode>(node)
			assertEquals(emptyList<NodeTuple>(), node.value)
		}

		@Test fun tab() {
			val ex = assertThrows<IllegalArgumentException> {
				SnakeYaml.loadRaw(yaml("\t", "test.yml"))
			}
			val error = """
				Failed to parse YAML: while scanning for the next token
				found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
				 in reader, line 1, column 1:
				    	
				    ^
				
			""".trimIndent()
			assertEquals(error, ex.message)
		}

		@Test fun tabs() {
			val ex = assertThrows<IllegalArgumentException> {
				SnakeYaml.loadRaw(yaml("\t\t", "test.yml"))
			}
			val error = """
				Failed to parse YAML: while scanning for the next token
				found character '\t(TAB)' that cannot start any token. (Do not use \t(TAB) for indentation)
				 in reader, line 1, column 1:
				    		
				    ^
				
			""".trimIndent()
			assertEquals(error, ex.message)
		}
	}

	private fun reserialize(yaml: String) {
		val result = SnakeYaml.loadRaw(yaml(yaml))
		val dumped = SnakeYaml.save(result)
		dumped shouldBe yaml
	}
}
