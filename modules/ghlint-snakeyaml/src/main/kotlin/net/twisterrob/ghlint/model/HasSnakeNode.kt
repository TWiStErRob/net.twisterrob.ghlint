package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.MappingNode

internal interface HasSnakeNode {

	val node: MappingNode
}
