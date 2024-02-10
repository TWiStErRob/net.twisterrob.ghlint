package net.twisterrob.ghlint.yaml

import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.SequenceNode

public fun Node.resolve(instanceLocation: String): Node {
	require(instanceLocation.startsWith("/")) { "Instance location (${instanceLocation}) must start with /." }
	val path = instanceLocation.split("/").drop(1)
	return path.fold(this) { node, key ->
		when (node) {
			is MappingNode -> node.getRequired(key)
			is SequenceNode -> node.array[key.toInt()]
			else -> error("Cannot resolve $instanceLocation in $this")
		}
	}
}
