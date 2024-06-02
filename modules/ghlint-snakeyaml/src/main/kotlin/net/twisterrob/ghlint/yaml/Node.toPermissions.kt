package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import org.snakeyaml.engine.v2.nodes.Node

public fun Map<Node, Node>.toPermissions(): Set<Permission> {
	return this.map { (key, value) ->
		Access.fromString(value.text).let {
			Permission.fromString(key.text, it)
		}
	}.toSet()
}
