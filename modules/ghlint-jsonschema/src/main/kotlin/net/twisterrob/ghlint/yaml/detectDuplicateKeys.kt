package net.twisterrob.ghlint.yaml

import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeType
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode

internal fun detectDuplicateKeys(node: Node): List<YamlValidationProblem> {
	val problems = mutableListOf<YamlValidationProblem>()
	detectDuplicateKeys(node, "", problems)
	return problems
}

private fun detectDuplicateKeys(node: Node, path: String, result: MutableList<YamlValidationProblem>) {
	when (node.nodeType) {
		null -> {
			error("Invalid node: ${node} at ${path}")
		}

		NodeType.SCALAR -> {
			// Leaf node, cannot be duplicate.
		}

		NodeType.ANCHOR -> {
			// Leaf node, cannot be duplicate?
		}

		NodeType.SEQUENCE -> {
			node as SequenceNode
			node.value.forEachIndexed { index, child ->
				detectDuplicateKeys(child, "${path}/${index}", result)
			}
		}

		NodeType.MAPPING -> {
			node as MappingNode
			val keys = mutableSetOf<String>()
			node.value.forEach { entry ->
				val keyNode = entry.keyNode as? ScalarNode ?: error("Key is not a scalar: ${entry.keyNode}")
				val key = keyNode.value
				val childPath = "${path}/${key}"
				if (!keys.add(key)) {
					result.add(YamlValidationProblem("Duplicate key: ${key}", childPath))
				}
				detectDuplicateKeys(entry.valueNode, childPath, result)
			}
		}
	}
}
