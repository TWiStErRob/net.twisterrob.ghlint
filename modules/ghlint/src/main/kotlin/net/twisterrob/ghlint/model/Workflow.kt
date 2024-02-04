package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.array
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.text
import org.snakeyaml.engine.v2.nodes.MappingNode

public class Workflow internal constructor(
	private val node: MappingNode,
) {

	public val jobs: Map<String, Job>
		get() = node.getRequired("jobs").map
			.mapKeys { (key, _) -> key.text }
			.mapValues { (_, value) -> Job.from(value) }

	public companion object
}

public class Job internal constructor(
	private val node: MappingNode,
) {

	public val steps: List<Step>
		get() = node.getRequired("steps").array.map { Step.from(it as MappingNode) }

	public companion object
}

public sealed class Step protected constructor(
	private val node: MappingNode,
) {

	public val name: String?
		get() = node.getOptionalText("name")

	public data class Run internal constructor(
		val node: MappingNode,
	) : Step(node) {

		public val run: String
			get() = node.getRequiredText("run")

		public val shell: String?
			get() = node.getOptionalText("shell")
	}

	public data class Uses internal constructor(
		val node: MappingNode,
	) : Step(node) {

		public val uses: String
			get() = node.getRequiredText("uses")
	}

	public companion object
}
