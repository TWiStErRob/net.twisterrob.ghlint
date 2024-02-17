package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

internal interface HasSnakeNode {

	val node: MappingNode

	val target: Node

	val location: Location
		get() = target.toLocation((this as Component).file)
}
