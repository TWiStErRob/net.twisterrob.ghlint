package net.twisterrob.ghlint.yaml

import dev.harrel.jsonschema.JsonNode
import dev.harrel.jsonschema.JsonNodeFactory
import dev.harrel.jsonschema.SimpleType
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeType
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.math.BigDecimal
import java.math.BigInteger

@Suppress("RedundantVisibilityModifier", "detekt.NestedClassesVisibility")
internal class SnakeYamlJsonNode private constructor(
	private val factory: Factory,
	private val node: Node,
	private val jsonPointer: String,
	private val nodeType: SimpleType = Factory.computeNodeType(node),
) : JsonNode {

	public constructor(factory: Factory, node: Node) : this(factory, node, "")

	public override fun getJsonPointer(): String = jsonPointer
	public override fun getNodeType(): SimpleType = nodeType

	public override fun asBoolean(): Boolean = asString().toBoolean()
	public override fun asInteger(): BigInteger = asString().toBigInteger()
	public override fun asNumber(): BigDecimal = asString().toBigDecimal()

	public override fun asString(): String =
		(node as ScalarNode).value

	public override fun asArray(): List<JsonNode> =
		(node as SequenceNode).value.mapIndexed { index, node ->
			SnakeYamlJsonNode(factory, node, "${jsonPointer}/${index}")
		}

	public override fun asObject(): Map<String, JsonNode> =
		(node as MappingNode).value.associate { entry ->
			val key = (entry.keyNode!! as ScalarNode).value
			val jsonPointer = "${jsonPointer}/${JsonNode.encodeJsonPointer(key)}"
			val value = SnakeYamlJsonNode(factory, entry.valueNode, jsonPointer)
			key to value
		}

	override fun equals(other: Any?): Boolean {
		if (this === other) {
			return true
		}
		if (other !is SnakeYamlJsonNode) {
			return false
		}
		if (getNodeType() != other.getNodeType()) {
			return false
		}
		return when (getNodeType()) {
			SimpleType.OBJECT -> asObject() == other.asObject()
			SimpleType.STRING -> asString() == other.asString()
			SimpleType.ARRAY -> asArray() == other.asArray()
			SimpleType.NULL -> true
			SimpleType.BOOLEAN -> asBoolean() == other.asBoolean()
			SimpleType.INTEGER -> asInteger() == other.asInteger()
			SimpleType.NUMBER -> asNumber() == other.asNumber()
		}
	}

	override fun hashCode(): Int {
		var result = getNodeType().hashCode()
		result = 31 * result + when (getNodeType()) {
			SimpleType.ARRAY -> asArray().hashCode()
			SimpleType.OBJECT -> asObject().hashCode()
			SimpleType.STRING -> asString().hashCode()
			SimpleType.NULL -> 0
			SimpleType.BOOLEAN -> asBoolean().hashCode()
			SimpleType.INTEGER -> asInteger().hashCode()
			SimpleType.NUMBER -> asNumber().hashCode()
		}
		return result
	}

	public class Factory(
		private val parse: (String) -> Node,
	) : JsonNodeFactory {

		public override fun wrap(node: Any): JsonNode =
			when {
				node is SnakeYamlJsonNode -> node

				node is Node && (isLiteral(node) || isArray(node) || isObject(node)) ->
					SnakeYamlJsonNode(this, node)

				else -> error("Cannot wrap object (${node}) which is not a Sequence, Mapping or Scalar.")
			}

		public override fun create(rawJson: String): JsonNode =
			wrap(parse(rawJson))

		internal companion object {

			private fun isLiteral(node: Node): Boolean =
				isNull(node) || isBoolean(node) || isString(node) || isInteger(node) || isDecimal(node)

			private fun isNull(node: Node): Boolean = node.tag == Tag.NULL
			private fun isArray(node: Node): Boolean = node.nodeType == NodeType.SEQUENCE
			private fun isObject(node: Node): Boolean = node.nodeType == NodeType.MAPPING

			private fun isBoolean(node: Node): Boolean = node.tag == Tag.BOOL
			private fun isString(node: Node): Boolean = node.tag == Tag.STR
			private fun isInteger(node: Node): Boolean = node.tag == Tag.INT
			private fun isDecimal(node: Node): Boolean = node.tag == Tag.FLOAT

			internal fun computeNodeType(node: Node): SimpleType =
				when {
					isNull(node) -> SimpleType.NULL
					isBoolean(node) -> SimpleType.BOOLEAN
					isString(node) -> SimpleType.STRING
					isDecimal(node) && (node as ScalarNode).value.isInteger() -> SimpleType.INTEGER
					isDecimal(node) -> SimpleType.NUMBER
					isInteger(node) -> SimpleType.INTEGER
					isArray(node) -> SimpleType.ARRAY
					isObject(node) -> SimpleType.OBJECT
					else -> error("Cannot assign type to node ${node}")
				}

			private fun String.isInteger(): Boolean =
				BigDecimal(this).stripTrailingZeros().scale() <= 0
		}
	}
}
