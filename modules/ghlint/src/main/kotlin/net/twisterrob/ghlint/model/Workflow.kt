package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.text
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode

public class Workflow internal constructor(
	public val parent: File,
	override val node: MappingNode,
) : InternalModel {

	public val name: String?
		get() = node.getOptionalText("name")

	public val env: Map<String, String>?
		get() = node.getOptional("env")?.run { map.toTextMap() }

	public val jobs: Map<String, Job>
		get() = node.getRequired("jobs").map
			.mapKeys { (key, _) -> key.text }
			.mapValues { (key, value) -> Job.from(this, key, value as MappingNode) }

	public val permissions: Map<String, String>?
		get() = node.getOptional("permissions")?.run { map.toTextMap() }

	public companion object
}

public val Workflow.id: String
	get() = parent.file.name
