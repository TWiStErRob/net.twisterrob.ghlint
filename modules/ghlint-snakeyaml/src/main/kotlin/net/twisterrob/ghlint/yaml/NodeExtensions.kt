package net.twisterrob.ghlint.yaml

import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode

internal fun MappingNode.getOptionalText(key: String): String? =
	this.value.singleOrNull { it.keyNode.text == key }?.valueNode?.text

internal fun MappingNode.getOptional(key: String): Node? =
	this.value.singleOrNull { it.keyNode.text == key }?.valueNode

internal fun MappingNode.getRequiredText(key: String): String =
	this.getOptionalText(key)
		?: error("Missing required key: ${key} in ${this.value.map { it.keyNode.text }}")

internal fun MappingNode.getRequired(key: String): Node =
	this.getOptional(key)
		?: error("Missing required key: ${key} in ${this.value.map { it.keyNode.text }}")

internal val Node.text: String
	get() = (this as ScalarNode).value

internal val Node.array: List<Node>
	get() = (this as SequenceNode).value

internal val Node.map: Map<Node, Node>
	get() = when (this) {
		is ScalarNode -> if (this.value.isEmpty()) emptyMap() else error("Not a map")
		is MappingNode -> this.value.associateBy({ it.keyNode }, { it.valueNode })
		else -> error("Not a map")
	}

internal fun Map<Node, Node>.toTextMap(): Map<String, String> =
	this.entries.associate { (key, value) -> key.text to value.text }
