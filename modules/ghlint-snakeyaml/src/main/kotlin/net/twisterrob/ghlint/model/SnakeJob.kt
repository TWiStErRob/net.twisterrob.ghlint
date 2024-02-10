package net.twisterrob.ghlint.model

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

	override val name: String?
		get() = node.getOptionalText("name")

	override val env: Map<String, String>?
		get() = node.getOptional("env")?.run { map.toTextMap() }

	override val permissions: Map<String, String>?
		get() = node.getOptional("permissions")?.run { map.toTextMap() }

	@Suppress("detekt.VariableNaming")
	override val `if`: String?
		get() = node.getOptionalText("if")

	public class SnakeNormalJob internal constructor(
		override val parent: Workflow,
		override val id: String,
		override val node: MappingNode,
	) : Job.NormalJob, SnakeJob() {

		override val steps: List<Step>
			get() = node.getRequired("steps").array.mapIndexed { index, node ->
				Step.from(this, index, node as MappingNode)
			}

		override val defaults: Job.NormalJob.Defaults?
			get() = node.getOptional("defaults")?.let { Job.NormalJob.Defaults.from(it as MappingNode) }

		override val timeoutMinutes: Int?
			get() = node.getOptionalText("timeout-minutes")?.toIntOrNull()

		public class SnakeDefaults internal constructor(
			private val node: MappingNode,
		) : Job.NormalJob.Defaults {

			override val run: Job.NormalJob.Defaults.Run?
				get() = node.getOptional("run")?.let { Job.NormalJob.Defaults.Run.from(it as MappingNode) }

			public class SnakeRun internal constructor(
				private val node: MappingNode,
			) : Job.NormalJob.Defaults.Run {

				override val shell: String?
					get() = node.getOptionalText("shell")
			}
		}
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
