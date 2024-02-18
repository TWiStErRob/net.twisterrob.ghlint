package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

internal interface HasSnakeNode {

	/**
	 * The node that represents this object in the YAML document.
	 *
	 * This is used to drill down and extract more information from the document.
	 */
	val node: MappingNode

	/**
	 * The node that represents the location of this object in reporting.
	 *
	 * This might be the key, or parent of [node].
	 */
	val target: Node

	/**
	 * The location of this object in the document for reporting.
	 *
	 * This is the external interface used by the engine to report issues.
	 *
	 * ---
	 *
	 * Implementors of [HasSnakeNode] should call this default property:
	 * ```kotlin
	 * override val location: Location
	 *     get() = super.location
	 * ```
	 * this indirection is required because [Component] is a sealed interface.
	 *
	 * @see Component.location
	 */
	val location: Location
		get() = target.toLocation((this as Component).file)
}
