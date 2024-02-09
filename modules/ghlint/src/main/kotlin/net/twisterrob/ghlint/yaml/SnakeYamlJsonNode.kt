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

public class SnakeYamlJsonNode private constructor(
	private val factory: Factory,
	private val node: Node,
	private val jsonPointer: String,
	private val nodeType: SimpleType = Factory.computeNodeType(node),
) : JsonNode {

	public constructor(factory: Factory, node: Node) : this(factory, node, "")

	public override fun getJsonPointer(): String = jsonPointer
	public override fun getNodeType(): SimpleType = nodeType
	public override fun asBoolean(): Boolean = (node as ScalarNode).value.toBoolean()
	public override fun asString(): String = (node as ScalarNode).value

	public override fun asInteger(): BigInteger =
		BigInteger((node as ScalarNode).value)

	public override fun asNumber(): BigDecimal =
		BigDecimal((node as ScalarNode).value)

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

	public class Factory(
		private val parse: (String) -> Node,
	) : JsonNodeFactory {

		public override fun wrap(node: Any): JsonNode =
			when {
				node is SnakeYamlJsonNode -> node

				node is Node && (isLiteral(node) || isArray(node) || isObject(node)) ->
					SnakeYamlJsonNode(this, node)

				else -> error("Cannot wrap object which is not a Sequence, Mapping or Scalar.")
			}

		public override fun create(rawJson: String): JsonNode =
			wrap(parse(rawJson))

		internal companion object {

			private fun isNull(node: Node): Boolean =
				node.tag == Tag.NULL

			private fun isArray(node: Node): Boolean =
				node.nodeType == NodeType.SEQUENCE

			private fun isObject(node: Node): Boolean =
				node.nodeType == NodeType.MAPPING

			private fun isLiteral(node: Node): Boolean =
				isNull(node) || isBoolean(node) || isString(node) || isInteger(node) || isDecimal(node)

			private fun isBoolean(node: Node): Boolean =
				node.tag == Tag.BOOL

			private fun isString(node: Node): Boolean =
				node.tag == Tag.STR

			private fun isInteger(node: Node): Boolean =
				node.tag == Tag.INT

			private fun isDecimal(node: Node): Boolean =
				node.tag == Tag.FLOAT

			internal fun computeNodeType(node: Node): SimpleType =
				if (isNull(node)) {
					SimpleType.NULL
				} else if (isBoolean(node)) {
					SimpleType.BOOLEAN
				} else if (isString(node)) {
					SimpleType.STRING
				} else if (isDecimal(node)) {
					if (BigDecimal((node as ScalarNode).value).stripTrailingZeros().scale() <= 0) {
						SimpleType.INTEGER
					} else {
						SimpleType.NUMBER
					}
				} else if (isInteger(node)) {
					SimpleType.INTEGER
				} else if (isArray(node)) {
					SimpleType.ARRAY
				} else if (isObject(node)) {
					SimpleType.OBJECT
				} else {
					error("Cannot assign type to node ${node}")
				}
		}
	}
}
