package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.Node

internal interface SnakeElement : Element {

	/**
	 * The factory that created this object.
	 *
	 * This is used to access the file and other context information.
	 */
	val factory: SnakeComponentFactory

	/**
	 * The file that contains this object.
	 *
	 * Note: this is not used anywhere else except [location] below.
	 */
	val file: File
		get() = factory.file

	/**
	 * The node that represents the location of this object in reporting.
	 *
	 * This might be the key, or parent of [node].
	 *
	 * Note: this is not used anywhere else except [location] below.
	 */
	val target: Node

	/**
	 * The location of this object in the document for reporting.
	 *
	 * This is the external interface used by the engine to report issues.
	 *
	 * @see Element.location
	 */
	override val location: Location
		get() = target.toLocation(file)
}
