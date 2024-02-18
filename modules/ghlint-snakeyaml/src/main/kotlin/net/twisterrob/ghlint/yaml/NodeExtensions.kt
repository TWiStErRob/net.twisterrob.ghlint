package net.twisterrob.ghlint.yaml

import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeType
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode

internal fun MappingNode.getOptionalText(key: String): String? =
	this.value.singleOrNull { it.keyNode.text == key }?.valueNode?.text

internal fun MappingNode.getOptional(key: String): Node? =
	this.value.singleOrNull { it.keyNode.text == key }?.valueNode

internal fun MappingNode.getRequiredText(key: String): String =
	this.getOptionalText(key) ?: throwMissingKey(key)

internal fun MappingNode.getOptionalKey(key: String): Node? =
	this.value.singleOrNull { it.keyNode.text == key }?.keyNode

internal fun MappingNode.getRequiredKey(key: String): Node =
	this.getOptionalKey(key) ?: throwMissingKey(key)

internal fun MappingNode.getRequired(key: String): Node =
	this.getOptional(key) ?: throwMissingKey(key)

internal fun Node.getDash(): Node {
	startMark.ifPresent {
		check(it.buffer[it.pointer - 1] == ' '.code && it.buffer[it.pointer - 2] == '-'.code) {
			"Invalid context: expected a space and a '-' character before pointer in \n${it.createSnippet()}"
		}
	}
	return object : Node(
		this.tag,
		this.startMark.map { Mark(it.name, it.index - 2, it.line, it.column - 2, it.buffer, it.pointer - 2) },
		this.startMark.map { Mark(it.name, it.index - 1, it.line, it.column - 1, it.buffer, it.pointer - 1) },
	) {
		override fun getNodeType(): NodeType = NodeType.ANCHOR
		override fun toString(): String = "-"
	}
}

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

private fun MappingNode.throwMissingKey(key: String): Nothing {
	error("Missing required key: ${key} in ${this.value.map { it.keyNode.text }}")
}
