package net.twisterrob.ghlint.yaml

import io.kotest.assertions.withClue
import io.kotest.matchers.optional.bePresent
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeType

class NodeExtensionsTest {

	@Nested
	inner class getDash {

		private fun validateDash(dash: Node, startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
			dash.toString() shouldBe "-"
			dash.nodeType shouldBe NodeType.SEQUENCE
			dash.startMark shouldBePresent {
				withClue("start line") { line shouldBe startLine }
				withClue("start column on ${line}") { column shouldBe startColumn }
			}
			dash.endMark shouldBePresent {
				withClue("end line") { line shouldBe endLine }
				withClue("end column on ${line}") { column shouldBe endColumn }
			}
			dash.anchor shouldNot bePresent()
		}

		@Test fun `scalar fails`() {
			val root = SnakeYaml.load(
				"""
					item
				""".trimIndent()
			)
			assertThrows<IllegalArgumentException> { root.getDash() }
		}

		@Test fun `array fails`() {
			val root = SnakeYaml.load(
				"""
					- item
					- item
				""".trimIndent()
			)
			assertThrows<IllegalArgumentException> { root.getDash() }
		}

		@Test fun `array item 1`() {
			val root = SnakeYaml.load(
				"""
					- item
					- item
				""".trimIndent()
			)
			val dash = root.array[0].getDash()
			validateDash(dash = dash, startLine = 0, startColumn = 0, endLine = 0, endColumn = 1)
		}

		@Test fun `array item 2`() {
			val root = SnakeYaml.load(
				"""
					- item
					- item
				""".trimIndent()
			)
			val dash = root.array[1].getDash()
			validateDash(dash = dash, startLine = 1, startColumn = 0, endLine = 1, endColumn = 1)
		}

		@Test fun `mapping fails`() {
			val root = SnakeYaml.load(
				"""
					foo: bar
				""".trimIndent()
			)
			assertThrows<IllegalArgumentException> { root.getDash() }
		}

		@Test fun `mapping array item 1`() {
			val root = SnakeYaml.load(
				"""
					array:
					  - item
					  - item
				""".trimIndent()
			)
			val dash = root.mapping.getRequired("array").array[0].getDash()
			validateDash(dash = dash, startLine = 1, startColumn = 2, endLine = 1, endColumn = 3)
		}

		@Test fun `mapping array item 2`() {
			val root = SnakeYaml.load(
				"""
					array:
					  - item
					  - item
				""".trimIndent()
			)
			val dash = root.mapping.getRequired("array").array[1].getDash()
			validateDash(dash = dash, startLine = 2, startColumn = 2, endLine = 2, endColumn = 3)
		}

		@Test fun `nested mapping array item 2`() {
			val root = SnakeYaml.load(
				"""
					map:
					  array:
					  - item
					  - item
				""".trimIndent()
			)
			val map = root.mapping.getRequired("map")
			val array = map.mapping.getRequired("array")
			val dash = array.array[1].getDash()
			validateDash(dash = dash, startLine = 3, startColumn = 2, endLine = 3, endColumn = 3)
		}

		@Test fun `nested indented mapping array item 2`() {
			val root = SnakeYaml.load(
				"""
					map:
					  array:
					    - item
					    - item
				""".trimIndent()
			)
			val map = root.mapping.getRequired("map")
			val array = map.mapping.getRequired("array")
			val dash = array.array[1].getDash()
			validateDash(dash = dash, startLine = 3, startColumn = 4, endLine = 3, endColumn = 5)
		}

		@Test fun `array mapping item`() {
			val root = SnakeYaml.load(
				"""
					array:
					  - name: item
					    value: item1
					  - name: item
					    value: item2
				""".trimIndent()
			)
			val dash = root.mapping.getRequired("array").array[1].getDash()
			validateDash(dash = dash, startLine = 3, startColumn = 2, endLine = 3, endColumn = 3)
		}

		@Test fun `mapping array`() {
			val root = SnakeYaml.load(
				"""
					array:
					  - item
					  - item
				""".trimIndent()
			)
			val array = root.mapping.getRequired("array")
			assertThrows<IllegalArgumentException> { array.getDash() }
		}

		@Test fun `array mapping item value`() {
			val root = SnakeYaml.load(
				"""
					array:
					  - name: item
					    value: item1
					  - name: item
					    value: item2
				""".trimIndent()
			)
			val value = root.mapping.getRequired("array").array[1].mapping.getRequired("value")

			assertThrows<IllegalArgumentException> { value.getDash() }
		}

		/**
		 * Fails with the default buffer size.
		 * From https://github.com/TWiStErRob/net.twisterrob.gradle/security/code-scanning/2087
		 */
		@Test fun `buffer problem`() {
			val root = SnakeYaml.load(
				"""
					x: ${"x".repeat(2035)}
					y:
					  - z:
				""".trimIndent()
			)
			val z = root.mapping.getRequired("y").array.single()
			validateDash(dash = z.getDash(), startLine = 2, startColumn = 2, endLine = 2, endColumn = 3)
		}
	}
}

private val Node.mapping: MappingNode
	get() = this as MappingNode
