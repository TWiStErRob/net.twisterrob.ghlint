package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import org.snakeyaml.engine.v2.nodes.MappingNode

public class SnakeDefaults internal constructor(
	private val factory: SnakeComponentFactory,
	private val node: MappingNode,
) : Defaults {

	override val run: Defaults.Run?
		get() = node.getOptional("run")?.let { factory.createDefaultsRun(it) }

	public class SnakeRun internal constructor(
		private val node: MappingNode,
	) : Defaults.Run {

		override val shell: String?
			get() = node.getOptionalText("shell")
	}
}
