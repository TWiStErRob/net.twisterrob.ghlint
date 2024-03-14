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
import org.snakeyaml.engine.v2.nodes.Node

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

	public class SnakeNormalJob internal constructor(
		private val factory: SnakeComponentFactory,
		override val parent: Workflow,
		override val id: String,
		override val node: MappingNode,
		override val target: Node,
	) : Job.NormalJob, SnakeJob() {

		override val steps: List<Step>
			get() = node.getRequired("steps").array.mapIndexed { index, node ->
				factory.createStep(
					parent = this,
					index = index,
					node = node,
				)
			}

		override val defaults: Defaults?
			get() = node.getOptional("defaults")?.let { factory.createDefaults(it) }

		override val timeoutMinutes: String?
			get() = node.getOptionalText("timeout-minutes")
	}

	public class SnakeReusableWorkflowCallJob internal constructor(
		private val factory: SnakeComponentFactory,
		override val parent: Workflow,
		override val id: String,
		override val node: MappingNode,
		override val target: Node,
	) : Job.ReusableWorkflowCallJob, SnakeJob() {

		override val uses: String
			get() = node.getRequiredText("uses")

		override val with: Map<String, String>?
			get() = node.getOptional("with")?.run { map.toTextMap() }

		override val secrets: Job.Secrets?
			get() = node.getOptional("secrets")?.let { factory.createSecrets(it) }
	}

	public class SnakeSecretsExplicit internal constructor(
		override val node: MappingNode,
		override val target: Node,
		private val map: Map<String, String>
	) : Job.Secrets.Explicit, Map<String, String> by map, HasSnakeNode

	public class SnakeSecretsInherit internal constructor(
		override val node: MappingNode,
		override val target: Node,
	) : Job.Secrets.Inherit, HasSnakeNode
}
