package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.array
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode

public sealed class SnakeJob protected constructor(
) : Job.BaseJob, HasSnakeNode {

	override val location: Location
		get() = super.location

	override val name: String?
		get() = node.getOptionalText("name")

	override val env: Map<String, String>?
		get() = node.getOptional("env")?.run { map.toTextMap() }

	override val permissions: Map<String, String>?
		get() = node.getOptional("permissions")?.run { map.toTextMap() }

	override val `if`: String?
		get() = node.getOptionalText("if")

	public companion object {

		public fun from(parent: Workflow, id: String, node: MappingNode): Job =
			when {

				node.getOptionalText("uses") != null ->
					SnakeReusableWorkflowCallJob(parent, id, node)

				node.getOptional("steps") != null ->
					SnakeNormalJob(parent, id, node)

				else ->
					error("Unknown job type: ${node}")
			}
	}

	public class SnakeNormalJob internal constructor(
		override val parent: Workflow,
		override val id: String,
		override val node: MappingNode,
	) : Job.NormalJob, SnakeJob() {

		override val steps: List<Step>
			get() = node.getRequired("steps").array
				.mapIndexed { index, node -> SnakeStep.from(this, index, node as MappingNode) }

		override val defaults: Defaults?
			get() = node.getOptional("defaults")?.let { SnakeDefaults(it as MappingNode) }

		override val timeoutMinutes: Int?
			get() = node.getOptionalText("timeout-minutes")?.toIntOrNull()
	}

	public class SnakeReusableWorkflowCallJob internal constructor(
		override val parent: Workflow,
		override val id: String,
		override val node: MappingNode,
	) : Job.ReusableWorkflowCallJob, SnakeJob() {

		override val uses: String
			get() = node.getRequiredText("uses")
	}
}
