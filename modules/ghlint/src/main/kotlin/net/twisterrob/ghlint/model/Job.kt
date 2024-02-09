package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.array
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode

public sealed class Job protected constructor(
) : InternalModel {

	public abstract val parent: Workflow
	public abstract val id: String
	public val name: String?
		get() = node.getOptionalText("name")

	public val env: Map<String, String>?
		get() = node.getOptional("env")?.run { map.toTextMap() }

	@Suppress("detekt.VariableNaming")
	public val `if`: String?
		get() = node.getOptionalText("if")

	public class NormalJob internal constructor(
		public override val parent: Workflow,
		override val id: String,
		override val node: MappingNode,
	) : Job() {

		public val steps: List<Step>
			get() = node.getRequired("steps").array.mapIndexed { index, node ->
				Step.from(this, index, node as MappingNode)
			}

		public val defaults: Defaults?
			get() = node.getOptional("defaults")?.let { Defaults.from(it as MappingNode) }

		public val timeoutMinutes: Int?
			get() = node.getOptionalText("timeout-minutes")?.toIntOrNull()

		public class Defaults internal constructor(
			private val node: MappingNode,
		) {

			public val run: Run?
				get() = node.getOptional("run")?.let { Run.from(it as MappingNode) }

			public class Run internal constructor(
				private val node: MappingNode,
			) {

				public val shell: String?
					get() = node.getOptionalText("shell")

				public companion object
			}

			public companion object
		}

		public companion object
	}

	public class ReusableWorkflowCallJob internal constructor(
		public override val parent: Workflow,
		override val id: String,
		override val node: MappingNode,
	) : Job() {

		public val uses: String
			get() = node.getRequiredText("uses")

		public companion object
	}

	public companion object
}
