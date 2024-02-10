package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.MappingNode

internal interface HasSnakeNode {

	val node: MappingNode

	val location: Location
		get() = node.toLocation((this as Component).file)
}
